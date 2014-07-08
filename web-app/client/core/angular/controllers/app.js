'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory', 'actionService',
    'POLLING_INTERVAL', 'NOTIFICATION_CATEGORIES', 'DEFAULT_DISPLAY_TIME',
    function ($scope, $interval, $routeParams, dataFactory, actionService,
      POLLING_INTERVAL, NOTIFICATION_CATEGORIES, DEFAULT_DISPLAY_TIME) {

    /**
     * @type {App}
     */
    $scope.app = {};

    var intervals = [];
    var statusEndpoints = [];

    dataFactory.getApp($routeParams.appId, function (app) {
      $scope.app = app;

      dataFactory.getStreamsByApp($scope.app.id, function (streams) {
        $scope.app.streams = streams;
      });

      dataFactory.getFlowsByApp($scope.app.id, function (flows) {
        $scope.app.flows = flows;
      });

      dataFactory.getMapreducesByApp($scope.app.id, function (mapreduces) {
        $scope.app.mapreduces = mapreduces;
      });

      dataFactory.getWorkflowsByApp($scope.app.id, function (workflows) {
        $scope.app.workflows = workflows;
      });

      dataFactory.getDatasetsByApp($scope.app.id, function (datasets) {
        $scope.app.datasets = datasets;
      });

      dataFactory.getProceduresByApp($scope.app.id, function (procedures) {
        $scope.app.procedures = procedures;
      });

    });

    $scope.getStatusEndpoint = function (entity) {
      return helpers.getStatusEndpoint(entity);
    };

    $scope.startRunnables = function (entityType) {
      if (entityType.toLowerCase() === 'flow') {
        
        $scope.app.flows.forEach(function (flow) {
          actionService.startFlow(flow, function success (data, status) {
            var msg = flow.name + ' started';
            helpers.displayNotification(
              msg, NOTIFICATION_CATEGORIES.SUCCESS, DEFAULT_DISPLAY_TIME);
          }, function error (data, status) {
            var msg = 'Could not start ' + flow.name;
            helpers.displayNotification(msg, NOTIFICATION_CATEGORIES.DANGER);
          });
        });  
      
      } else if (entityType.toLowerCase() === 'procedure') {
        
        $scope.app.procedures.forEach(function (procedure) {
          actionService.startFlow(procedure, function success (data, status) {
            var msg = procedure.name + ' started';
            helpers.displayNotification(
              msg, NOTIFICATION_CATEGORIES.SUCCESS, DEFAULT_DISPLAY_TIME);
          }, function error (data, status) {
            var msg = 'Could not start ' + procedure.name;
            helpers.displayNotification(msg, NOTIFICATION_CATEGORIES.DANGER);
          });
        });  
  
      }
    };

    $scope.stopRunnables = function (entityType) {
      if (entityType.toLowerCase() === 'flow') {
        
        $scope.app.flows.forEach(function (flow) {
          actionService.stopFlow(flow, function success (data, status) {
            var msg = flow.name + ' started';
            helpers.displayNotification(
              msg, NOTIFICATION_CATEGORIES.SUCCESS, DEFAULT_DISPLAY_TIME);
          }, function error (data, status) {
            var msg = 'Could not stop ' + flow.name;
            helpers.displayNotification(msg, NOTIFICATION_CATEGORIES.DANGER);
          });
        });  
      
      } else if (entityType.toLowerCase() === 'procedure') {
        
        $scope.app.procedures.forEach(function (procedure) {
          actionService.stopProcedure(procedure, function success (data, status) {
            var msg = procedure.name + ' started';
            helpers.displayNotification(
              msg, NOTIFICATION_CATEGORIES.SUCCESS, DEFAULT_DISPLAY_TIME);
          }, function error (data, status) {
            var msg = 'Could not stop ' + procedure.name;
            helpers.displayNotification(msg, NOTIFICATION_CATEGORIES.DANGER);
          });
        });  
  
      }
    };


    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */
    $scope.$on("$destroy", function () {

      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });
  }];

  return Ctrl;

});