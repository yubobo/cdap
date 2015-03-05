/*
 * Copyright © 2015 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package co.cask.cdap.data.tools;

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.api.dataset.DatasetDefinition;
import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.api.dataset.table.Row;
import co.cask.cdap.api.dataset.table.Scanner;
import co.cask.cdap.api.dataset.table.Table;
import co.cask.cdap.app.ApplicationSpecification;
import co.cask.cdap.app.runtime.ProgramController;
import co.cask.cdap.app.store.Store;
import co.cask.cdap.common.conf.CConfiguration;
import co.cask.cdap.common.conf.Constants;
import co.cask.cdap.data2.datafabric.dataset.DatasetsUtil;
import co.cask.cdap.data2.dataset2.DatasetFramework;
import co.cask.cdap.data2.dataset2.lib.table.MDSKey;
import co.cask.cdap.data2.dataset2.tx.Transactional;
import co.cask.cdap.internal.app.store.AppMetadataStore;
import co.cask.cdap.internal.app.store.ApplicationMeta;
import co.cask.cdap.internal.app.store.DefaultStore;
import co.cask.cdap.internal.app.store.ProgramArgs;
import co.cask.cdap.notifications.feeds.service.MDSNotificationFeedStore;
import co.cask.cdap.proto.Id;
import co.cask.cdap.proto.ProgramRunStatus;
import co.cask.cdap.proto.ProgramType;
import co.cask.cdap.proto.RunRecord;
import co.cask.tephra.TransactionExecutor;
import co.cask.tephra.TransactionExecutorFactory;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterators;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.twill.filesystem.Location;
import org.apache.twill.filesystem.LocationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

/**
 * Upgraded the Meta Data for applications
 */
public class MDSUpgrader extends AbstractUpgrader {

  private static final Logger LOG = LoggerFactory.getLogger(MDSUpgrader.class);
  private final Transactional<UpgradeTable, Table> appMDS;
  private final CConfiguration cConf;
  private final Store store;

  @Inject
  private MDSUpgrader(LocationFactory locationFactory, TransactionExecutorFactory executorFactory,
                      @Named("namespacedDSFramework") final DatasetFramework namespacedFramework, CConfiguration cConf,
                      @Named("nonNamespacedStore") final Store store) {
    super(locationFactory);
    this.cConf = cConf;
    this.store = store;
    this.appMDS = Transactional.of(executorFactory, new Supplier<UpgradeTable>() {
      @Override
      public UpgradeTable get() {
        try {
          Table table = DatasetsUtil.getOrCreateDataset(namespacedFramework, Id.DatasetInstance.from
                                                          (Constants.SYSTEM_NAMESPACE_ID, DefaultStore.APP_META_TABLE),
                                                        "table", DatasetProperties.EMPTY,
                                                        DatasetDefinition.NO_ARGUMENTS, null);
          return new UpgradeTable(table);
        } catch (Exception e) {
          LOG.error("Failed to access {} table", DefaultStore.APP_META_TABLE, e);
          throw Throwables.propagate(e);
        }
      }
    });
  }

  @Override
  public void upgrade() throws Exception {
    appMDS.executeUnchecked(new TransactionExecutor.
      Function<UpgradeTable, Void>() {
      @Override
      public Void apply(UpgradeTable configTable) throws Exception {
        byte[] appMetaRecordPrefix = new MDSKey.Builder().add(AppMetadataStore.TYPE_APP_META).build().getKey();
        Scanner rows = configTable.table.scan(appMetaRecordPrefix, Bytes.stopKeyForPrefix(appMetaRecordPrefix));
        Row row;
        while ((row = rows.next()) != null) {
          ApplicationMeta appMeta = GSON.fromJson(Bytes.toString(row.get(COLUMN)), ApplicationMeta.class);
          appSpecHandler(appMeta.getId(), appMeta.getSpec());
          applicationMetadataHandler(row);
        }
        return null;
      }
    });
  }

  public void appSpecHandler(String appId, ApplicationSpecification appSpec) {
    programSpecHandler(appId, appSpec.getFlows().keySet(), ProgramType.FLOW);
    programSpecHandler(appId, appSpec.getMapReduce().keySet(), ProgramType.MAPREDUCE);
    programSpecHandler(appId, appSpec.getSpark().keySet(), ProgramType.SPARK);
    programSpecHandler(appId, appSpec.getWorkflows().keySet(), ProgramType.WORKFLOW);
    programSpecHandler(appId, appSpec.getServices().keySet(), ProgramType.SERVICE);
  }

  private void programSpecHandler(String appId, Set<String> programIds, ProgramType programType) {
    for (String programId : programIds) {
      runRecordStartedHandler(appId, programId, programType);
      runRecordCompletedHandler(appId, programId, programType);
      programArgsHandler(appId, programId, programType);
    }
  }

  /**
   *
   */

  /**
   * Handles the {@link AppMetadataStore#TYPE_PROGRAM_ARGS} meta data and writes it back with namespace
   *
   * @param appId       the application id to which this program belongs to
   * @param programId   the program id of the program
   * @param programType the {@link ProgramType} of the program
   */
  private void programArgsHandler(final String appId, final String programId, final ProgramType programType) {
    final byte[] partialKey = new MDSKey.Builder().add(AppMetadataStore.TYPE_RUN_RECORD_STARTED, DEVELOPER_ACCOUNT,
                                                       appId, programId).build().getKey();
    appMDS.executeUnchecked(new TransactionExecutor.Function<UpgradeTable, Void>() {
      @Override
      public Void apply(UpgradeTable input) throws Exception {
        Scanner rows = input.table.scan(partialKey, Bytes.stopKeyForPrefix(partialKey));
        Row row;
        while ((row = rows.next()) != null) {
          MDSKey.Splitter keyParts = new MDSKey(row.getRow()).split();

          ProgramArgs programArgs = GSON.fromJson(Bytes.toString(row.get(COLUMN)), ProgramArgs.class);

//          // skip runRecordStarted
//          keyParts.getString();
//          // skip accountId
//          keyParts.skipString();
//          String appId = keyParts.getString();
//          String programName = keyParts.getString();

          store.storeRunArguments(Id.Program.from(Id.Application.from(Constants.DEFAULT_NAMESPACE, appId), programType,
                                                  programId), programArgs.getArgs());
        }
        return null;
      }
    });
  }

  /**

   */
  /**
   * Handled the {@link AppMetadataStore#TYPE_RUN_RECORD_STARTED} meta data and writes it back with namespace
   *
   * @param appId       the application id to which this program belongs to
   * @param programId   the program id of the program
   * @param programType the {@link ProgramType} of the program
   */
  private void runRecordStartedHandler(final String appId, final String programId, final ProgramType programType) {
    final byte[] partialKey = new MDSKey.Builder().add(AppMetadataStore.TYPE_RUN_RECORD_STARTED, DEVELOPER_ACCOUNT,
                                                       appId, programId).build().getKey();
    appMDS.executeUnchecked(new TransactionExecutor.Function<UpgradeTable, Void>() {
      @Override
      public Void apply(UpgradeTable input) throws Exception {
        Scanner rows = input.table.scan(partialKey, Bytes.stopKeyForPrefix(partialKey));
        Row row;
        while ((row = rows.next()) != null) {
          MDSKey.Splitter keyParts = new MDSKey(row.getRow()).split();

          RunRecord runRecord = GSON.fromJson(Bytes.toString(row.get(COLUMN)), RunRecord.class);

          // skip runRecordStarted
          keyParts.getString();
          // skip default
          keyParts.skipString();
          // skip appId
          keyParts.skipString();
          // skip programId
          keyParts.skipString();
          String pId = keyParts.getString();

          //store.setStart(Id.Program.from(Constants.DEFAULT_NAMESPACE, appId, programId), pId, runRecord.getStartTs());
          store.setStart(Id.Program.from(Id.Application.from(Constants.DEFAULT_NAMESPACE, appId), programType,
                                         programId), pId, runRecord.getStartTs());
        }
        return null;
      }
    });
  }

  /**
   * Handles the {@link AppMetadataStore#TYPE_RUN_RECORD_COMPLETED} meta data and writes it back with namespace
   *
   * @param appId       the application id to which this program belongs to
   * @param programId   the program id of the program
   * @param programType the {@link ProgramType} of the program
   */
  private void runRecordCompletedHandler(final String appId, final String programId, final ProgramType programType) {

    final byte[] partialKey = new MDSKey.Builder().add(AppMetadataStore.TYPE_RUN_RECORD_STARTED, DEVELOPER_ACCOUNT,
                                                       appId, programId).build().getKey();
    appMDS.executeUnchecked(new TransactionExecutor.Function<UpgradeTable, Void>() {
      @Override
      public Void apply(UpgradeTable input) throws Exception {
        Scanner rows = input.table.scan(partialKey, Bytes.stopKeyForPrefix(partialKey));
        Row row;
        while ((row = rows.next()) != null) {
          RunRecord runRecord = GSON.fromJson(Bytes.toString(row.get(COLUMN)), RunRecord.class);

          MDSKey.Splitter keyParts = new MDSKey(row.getRow()).split();
          // skip runRecordStarted
          keyParts.getString();
          // skip default
          keyParts.skipString();
          // skip appId
          keyParts.skipString();
          // skip programId
          keyParts.skipString();
          long startTs = keyParts.getLong();
          String pId = keyParts.getString();

          writeTempRunRecordStart(appId, programType, programId, pId, startTs);
          store.setStop(Id.Program.from(Id.Application.from(Constants.DEFAULT_NAMESPACE, appId), programType,
                                        programId), pId, runRecord.getStopTs(),
                        getControllerStateByStatus(runRecord.getStatus()));
        }
        return null;
      }
    });
  }

  /**
   * Writes the {@link AppMetadataStore#TYPE_RUN_RECORD_STARTED} entry in the app meta table so that
   * {@link AppMetadataStore#TYPE_RUN_RECORD_COMPLETED} can be written which deleted the started record.
   */
  private void writeTempRunRecordStart(String appId, ProgramType programType, String programId, String pId,
                                       long startTs) {
    store.setStart(Id.Program.from(Id.Application.from(Constants.DEFAULT_NAMESPACE, appId), programType, programId),
                   pId, Long.MAX_VALUE - startTs);
  }

  /**
   * Writes the updated application metadata through the {@link DefaultStore}
   *
   * @param row the {@link Row} containing the application metadata
   * @throws URISyntaxException if failed to create {@link URI} from the archive location in metadata
   */
  private void applicationMetadataHandler(Row row) throws URISyntaxException, IOException {
    ApplicationMeta appMeta = GSON.fromJson(Bytes.toString(row.get(COLUMN)), ApplicationMeta.class);
    store.addApplication(Id.Application.from(Constants.DEFAULT_NAMESPACE, appMeta.getId()), appMeta.getSpec(),
                         updateAppArchiveLocation(appMeta.getId(), new URI(appMeta.getArchiveLocation())));
  }

  /**
   * Creates the new archive location for application with namespace
   *
   * @param appId           : the application id
   * @param archiveLocation : the application archive location
   * @return {@link Location} for the new archive location with namespace
   */
  private Location updateAppArchiveLocation(String appId, URI archiveLocation) throws IOException {
    String archiveFilename = locationFactory.create(archiveLocation).getName();

    return locationFactory.create(Constants.DEFAULT_NAMESPACE).append(
      cConf.get(Constants.AppFabric.OUTPUT_DIR)).append(appId).append(Constants.ARCHIVE_DIR).append(archiveFilename);
  }

  private static final class UpgradeTable implements Iterable<Table> {
    final Table table;

    private UpgradeTable(Table table) {
      this.table = table;
    }

    @Override
    public Iterator<Table> iterator() {
      return Iterators.singletonIterator(table);
    }
  }

  /**
   * Gives the {@link ProgramController.State} for a given {@link ProgramRunStatus}
   * Note: This will given the state as {@link ProgramController.State#STARTING} for {@link ProgramRunStatus#RUNNING}
   * even though running has multiple mapping but that is fine in our case as we use this
   * {@link ProgramController.State} to write the runRecordCompleted through {@link DefaultStore#setStop} which converts
   * it back to {@link ProgramRunStatus#RUNNING}. So we don't really care about the temporary intermediate
   * {@link ProgramController.State}
   *
   * @param status : the status
   * @return the state for the status or null if there is no defined state for the given status
   */
  public static ProgramController.State getControllerStateByStatus(ProgramRunStatus status) {
    for (ProgramController.State state : ProgramController.State.values()) {
      if (state.getRunStatus() == status) {
        return state;
      }
    }
    return null;
  }
}
