'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, $routeParams, dataFactory, POLLING_INTERVAL) {

    /**
     * @type {Procedure}
     */
    $scope.procedure = {};

    var intervals = [];
    var statusEndpoints = [];

    var appId = $routeParams.appId;
    var procedureId = $routeParams.procedureId;

    //$scope.failedEps = [];
    //$scope.successfulEps = [];

    /**
     * List of metrics to track.
     */
    /*var metrics = [
      { name: 'successfulEps',
        endpoint: '/reactor/apps/' + appId + '/procedures/' + procedureId + '/query.requests?start=now-60s&end=now-0s&count=60' }
    ];

    // Set up tracking for all metrics that need to be updated realtime.
    metrics.forEach(function (metric) {
      metricsService.trackMetric(metric.endpoint);
    });

    var ival = $interval(function () {
      metrics.forEach(function (metric) {
        $scope[metric.name] = metricsService.getMetricByEndpoint(metric.endpoint);
      });
    }, POLLING_INTERVAL);
    intervals.push(ival);*/


    dataFactory.getProcedureByAppNameAndId(appId, procedureId, function (procedure) {
      $scope.procedure = procedure;
      console.log($scope.procedure);


        /*$scope.procedure.endpoint = helpers.getStatusEndpoint($scope.procedure, appId);
        console.log($scope.procedure.endpoint);
        statusService.trackStatus($scope.procedure.endpoint);
        statusEndpoints.push($scope.procedure.endpoint);

      intervals.push($interval(function () {
          var status = statusService.getStatusByEndpoint($scope.procedure.endpoint);
          $scope.procedure.status = status;
      }, POLLING_INTERVAL));*/
    });

    $scope.getStatusEndpoint = function (entity) {
      return helpers.getStatusEndpoint(entity);
    };
    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */
    $scope.$on("$destroy", function () {
      for (var i = 0, len = metrics.length; i < len; i++) {
        metricsService.untrackMetric(metrics[i].endpoint);
      }

      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});