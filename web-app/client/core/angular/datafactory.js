'use strict';

define(function () {

  /* Items */

  var Factory = [
    '$http',
    '$interval',
    '$q',
    'REACTOR_ENDPOINT',
    'App',
    'Stream',
    'Flow',
    'Dataset',
    'Procedure',
    'Mapreduce',
    'Workflow',

    function (
      $http,
      $interval,
      $q,
      REACTOR_ENDPOINT,
      App,
      Stream,
      Flow,
      Dataset,
      Procedure,
      Mapreduce,
      Workflow) {

    return {

      /**
       * Gets all reactor apps. Depends on the App model.
       * @param  {Function} callback that consumes list of App objects.
       */
      getApps: function (callback) {
        $http.get(REACTOR_ENDPOINT + '/apps').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new App(entry));
          });
          if (typeof callback === 'function') {
            callback(entries);
          }
        });
      },

      /**
       * Gets all reactor streams. Depends on the Stream model.
       * @param  {Function} callback that consumes list of Stream objects.
       */
      getStreams: function (callback) {
        $http.get(REACTOR_ENDPOINT + '/streams').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Stream(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets all reactor flows. Depends on the Flow model.
       * @param  {Function} callback that consumes list of Flow objects.
       */
      getFlows: function (callback) {
        $http.get(REACTOR_ENDPOINT + '/flows').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Flow(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets all reactor workflows. Depends on the Workflow model.
       * @param  {Function} callback that consumes list of Workflow objects.
       */
      getWorkflows: function (callback) {
        $http.get(REACTOR_ENDPOINT + '/workflows').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Workflow(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets all reactor map reduce jobs. Depends on the Mapreduce model.
       * @param  {Function} callback that consumes list of Mapreduce objects.
       */
      getMapreduces: function (callback) {
        $http.get(REACTOR_ENDPOINT + '/mapreduce').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Mapreduce(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets all reactor datasets. Depends on the Dataset model.
       * @param  {Function} callback that consumes list of Dataset objects.
       */
      getDatasets: function (callback) {
        $http.get(REACTOR_ENDPOINT + '/datasets').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Dataset(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets a dataset by id. Depends on the Dataset model.
       * @param  {Function} callback that consumes a Dataset object.
       */
      getDatasetById: function (datasetId, callback) {
        $http.get(REACTOR_ENDPOINT + '/datasets/' + datasetId).success(function (data) {
          var dataset = new Dataset(data);
          callback(dataset);
        });
      },

      /**
       * Gets all reactor procedures. Depends on the Procedure model.
       * @param  {Function} callback that consumes list of Procedure objects.
       */
      getProcedures: function (callback) {
        $http.get(REACTOR_ENDPOINT + '/procedures').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Procedure(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets all reactor procedures. Depends on the Procedure model.
       * @param  {Function} callback that consumes list of Procedure objects.
       */
      getApp: function (appId, callback) {
        $http.get(REACTOR_ENDPOINT + '/apps/' + appId).success(function (data) {
          var app = new App(data);
          callback(app);
        });
      },

      /**
       * Gets streams for an app.
       * @param  {Function} callback that consumes list of Stream objects.
       */
      getStreamsByApp: function (appId, callback) {
        $http.get(REACTOR_ENDPOINT + '/apps/' + appId + '/streams').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Stream(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets flows for an app.
       * @param  {Function} callback that consumes list of Flow objects.
       */
      getFlowsByApp: function (appId, callback) {
        $http.get(REACTOR_ENDPOINT + '/apps/' + appId + '/flows').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Flow(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets map reduce jobs for an app.
       * @param  {Function} callback that consumes list of Mapreduce objects.
       */
      getMapreducesByApp: function (appId, callback) {
        $http.get(REACTOR_ENDPOINT + '/apps/' + appId + '/mapreduce').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Mapreduce(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets workflows for an app.
       * @param  {Function} callback that consumes list of Mapreduce objects.
       */
      getWorkflowsByApp: function (appId, callback) {
        $http.get(REACTOR_ENDPOINT + '/apps/' + appId + '/workflows').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Workflow(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets datasets for an app.
       * @param  {Function} callback that consumes list of Dataset objects.
       */
      getDatasetsByApp: function (appId, callback) {
        $http.get(REACTOR_ENDPOINT + '/apps/' + appId + '/datasets').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Dataset(entry));
          });
          callback(entries);
        });
      },

      /**
       * Gets procedures for an app.
       * @param  {Function} callback that consumes list of Procedure objects.
       */
      getProceduresByApp: function (appId, callback) {
        $http.get(REACTOR_ENDPOINT + '/apps/' + appId + '/procedures').success(function (data) {
          var entries = [];
          data.map(function (entry) {
            entries.push(new Procedure(entry));
          });
          callback(entries);
        });
      }


    };


  }];

  return Factory;

});