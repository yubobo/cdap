'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory', 'statusService', 'metricsService', 'helpers', 'POLLING_INTERVAL',
    function($scope, $interval, $routeParams, dataFactory, statusService, metricsService, helpers, POLLING_INTERVAL) {

    var statusEndpoints = [];
    var intervals = [];

    var appId = $routeParams.appId;
    var flowId = $routeParams.flowId;

    $scope.processingRate = [];
    $scope.busyness = [];

    var metrics = [
      { name: 'processingRate',
        endpoint: '/reactor/apps/' + appId + '/flows/' + flowId + '/process.events.processed?start=now-60s&end=now-0s&count=6' },
      { name: 'busyness',
        endpoint: '/reactor/apps/' + appId + '/flows/' + flowId + '/process.busyness?start=now-60s&end=now-0s&count=60'}
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
    intervals.push(ival);

    dataFactory.getFlowByAppNameAndId(appId, flowId, function (flow) {
      $scope.flow = flow;

      $scope.flow.endpoint = helpers.getEndpoint($scope.flow, appId);
      console.log($scope.flow.endpoint);
      statusService.trackStatus($scope.flow.endpoint);
      statusEndpoints.push($scope.flow.endpoint);

      intervals.push($interval(function () {
          var status = statusService.getStatusByEndpoint($scope.flow.endpoint);
          $scope.flow.status = status;
      }, POLLING_INTERVAL));
    });

    $scope.$on("$destroy", function(){
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