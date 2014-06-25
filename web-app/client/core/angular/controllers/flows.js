'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', 'dataFactory', 'statusService', 'helpers', 'POLLING_INTERVAL',
    function($scope, $interval, dataFactory, statusService, helpers, POLLING_INTERVAL) {

    var statusEndpoints = [];
    var intervals = [];
    $scope.flows = [];
    $scope.mapreduces = [];
    $scope.workflows = [];

    dataFactory.getFlows(function (flows) {
      $scope.flows = flows;

      $scope.flows.forEach(function (flow) {
        flow.endpoint = helpers.getEndpoint(flow);
        statusService.trackStatus(flow.endpoint);
        statusEndpoints.push(flow.endpoint);
      });

      intervals.push($interval(function () {
        $scope.flows.forEach(function (flow) {
          var status = statusService.getStatusByEndpoint(flow.endpoint);
          flow.status = status;
        });
      }, POLLING_INTERVAL));
    });

    dataFactory.getMapreduces(function (mapreduces) {
      $scope.mapreduces = mapreduces;
      $scope.mapreduces.forEach(function (mapreduce) {
        mapreduce.endpoint = helpers.getEndpoint(mapreduce);
        statusService.trackStatus(mapreduce.endpoint);
        statusEndpoints.push(mapreduce.endpoint);
      });

      intervals.push($interval(function () {
        $scope.mapreduces.forEach(function (mapreduce) {
          var status = statusService.getStatusByEndpoint(mapreduce.endpoint);
          mapreduce.status = status;
        });
      }, POLLING_INTERVAL));
    });

    dataFactory.getWorkflows(function (workflows) {
      $scope.workflows = workflows;
      $scope.workflows.forEach(function (workflow) {
        workflow.endpoint = helpers.getEndpoint(workflow);
        statusService.trackStatus(workflow.endpoint);
        statusEndpoints.push(workflow.endpoint);
      });

      intervals.push($interval(function () {
        $scope.workflows.forEach(function (workflow) {
          var status = statusService.getStatusByEndpoint(workflow.endpoint);
          workflow.status = status;
        });
      }, POLLING_INTERVAL));
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });

  }];

  return Ctrl;

});