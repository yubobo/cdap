'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$http', '$interval', '$routeParams', 'dataFactory', 'helpers',
    function ($scope, $http, $interval, $routeParams, dataFactory, helpers) {

    /**
     * @type {App}
     */
    $scope.app = {};

    dataFactory.getApp($routeParams.appId, function (app) {
      $scope.app = app;

      dataFactory.getStreamsByApp($scope.app.id, function (streams) {
        $scope.app.streams = streams;
      });

      dataFactory.getFlowsByApp($scope.app.id, function (flows) {
        $scope.app.flows = flows;
      });

      dataFactory.getDatasetsByApp($scope.app.id, function (datasets) {
        $scope.app.datasets = datasets;
      });

      dataFactory.getProceduresByApp($scope.app.id, function (procedures) {
        $scope.app.procedures = procedures;
      });

    });
    
      

    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */    
    $scope.$on("$destroy", function () {
      if (intervals) {
        helpers.cancelAllIntervals($interval, intervals);  
      }
    });
  }];

  return Ctrl;

});