package com.continuuity.internal.app.deploy;

import com.continuuity.api.metadata.Id;
import com.continuuity.api.metadata.ProgramType;

/**
 * Interface that is responsible to stopping programs. Used while stop programs that are being deleted during
 * re-deploy process.
 */
public interface ProgramTerminator {

  /**
   * Method to implement for stopping the programs.
   *
   * @param id         Account id.
   * @param programId  Program id.
   * @param type       Program Type.
   */
  void stop (Id.Account id, Id.Program programId, ProgramType type) throws Exception;

}
