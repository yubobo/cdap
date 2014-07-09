'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, dataFactory, POLLING_INTERVAL) {

    //var statusEndpoints = [];
    var intervals = [];


    $scope.flows = [];
    $scope.mapreduces = [];
    $scope.workflows = [];

    dataFactory.getFlows(function (flows) {
      $scope.flows = flows;
    });

    dataFactory.getMapreduces(function (mapreduces) {
      $scope.mapreduces = mapreduces;
    });

    dataFactory.getWorkflows(function (workflows) {
      $scope.workflows = workflows;
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });

  }];

  return Ctrl;

});