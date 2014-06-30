'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$http', '$interval', 'dataFactory', 'metricsService', 'POLLING_INTERVAL',
    function ($scope, $http, $interval, dataFactory, metricsService, POLLING_INTERVAL) {

    /** 
     * A list of app objects containing.
     * @type {Array}
     */
    $scope.apps = [];

    var intervals = [];

    dataFactory.getApps(function (apps) {
      $scope.apps = apps;
      for (var i = 0; i < $scope.apps.length; i++) {
        
        // Use closures to localize scope of i so that it doesn't change when the async function
        // returns.
        (function (i) {
          dataFactory.getStreamsByApp($scope.apps[i].id, function (streams) {
            $scope.apps[i].streams = streams;
          });
          
          dataFactory.getFlowsByApp($scope.apps[i].id, function (flows) {
            $scope.apps[i].flows = flows;
          });

          dataFactory.getMapreducesByApp($scope.apps[i].id, function (mapreduces) {
            $scope.apps[i].mapreduces = mapreduces;
         });

          dataFactory.getWorkflowsByApp($scope.apps[i].id, function (workflows) {
            $scope.apps[i].workflows = workflows;
          });

          dataFactory.getDatasetsByApp($scope.apps[i].id, function (datasets) {
            $scope.apps[i].datasets = datasets;
          });

          dataFactory.getProceduresByApp($scope.apps[i].id, function (procedures) {
            $scope.apps[i].procedures = procedures;
          });
        })(i);
      }

    });

    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */
    $scope.$on("$destroy", function() {
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });
  }];

  return Ctrl;

});