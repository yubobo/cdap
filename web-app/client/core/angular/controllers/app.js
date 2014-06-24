'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory', 'helpers', 'statusService',
    'POLLING_INTERVAL',
    function ($scope, $interval, $routeParams, dataFactory, helpers, statusService,
      POLLING_INTERVAL) {

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
        $scope.app.flows.forEach(function (flow) {
          flow.endpoint = helpers.getEndpoint(flow);
          statusService.trackStatus(flow.endpoint);
          statusEndpoints.push(flow.endpoint);
        });

        intervals.push($interval(function () {
          $scope.app.flows.forEach(function (flow) {
            var status = statusService.getStatusByEndpoint(flow.endpoint);
            flow.status = status;
          });          
        }, POLLING_INTERVAL));

      });

      dataFactory.getMapreducesByApp($scope.app.id, function (mapreduces) {
        $scope.app.mapreduces = mapreduces;
        $scope.app.mapreduces.forEach(function (mapreduce) {
          mapreduce.endpoint = helpers.getEndpoint(mapreduce);
          statusService.trackStatus(mapreduce.endpoint);
          statusEndpoints.push(mapreduce.endpoint);
        });

        intervals.push($interval(function () {
          $scope.app.mapreduces.forEach(function (mapreduce) {
            var status = statusService.getStatusByEndpoint(mapreduce.endpoint);
            mapreduce.status = status;
          });          
        }, POLLING_INTERVAL));
      });

      dataFactory.getWorkflowsByApp($scope.app.id, function (workflows) {
        $scope.app.workflows = workflows;
        $scope.app.workflows.forEach(function (workflow) {
          workflow.endpoint = helpers.getEndpoint(workflow);
          statusService.trackStatus(workflow.endpoint);
          statusEndpoints.push(workflow.endpoint);
        });

        intervals.push($interval(function () {
          $scope.app.workflows.forEach(function (workflow) {
            var status = statusService.getStatusByEndpoint(workflow.endpoint);
            workflow.status = status;
          });          
        }, POLLING_INTERVAL));
      });

      dataFactory.getDatasetsByApp($scope.app.id, function (datasets) {
        $scope.app.datasets = datasets;
      });

      dataFactory.getProceduresByApp($scope.app.id, function (procedures) {
        $scope.app.procedures = procedures;
        $scope.app.procedures.forEach(function (procedure) {
          procedure.endpoint = helpers.getEndpoint(procedure);
          statusService.trackStatus(procedure.endpoint);
          statusEndpoints.push(procedure.endpoint);
        });

        intervals.push($interval(function () {
          $scope.app.procedures.forEach(function (procedure) {
            var status = statusService.getStatusByEndpoint(procedure.endpoint);
            procedure.status = status;
          });          
        }, POLLING_INTERVAL));
      });

    });
    
      

    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */    
    $scope.$on("$destroy", function () {

      for (var i = 0, len = statusEndpoints.length; i < len; i++) {
        statusService.untrackStatus(statusEndpoints[i])
      }

      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);  
      }
    });
  }];

  return Ctrl;

});