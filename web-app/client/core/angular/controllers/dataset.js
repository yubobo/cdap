'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory', 'statusService', 'metricsService', 'helpers', 'POLLING_INTERVAL',
    function($scope, $interval, $routeParams, dataFactory, statusService, metricsService, helpers, POLLING_INTERVAL) {

    var statusEndpoints = [];
    var intervals = [];

    var datasetId = $routeParams.datasetId;

    $scope.writeRateBytes = [];
    $scope.writeRateOps = [];
    $scope.readRateOps = [];

    var metrics = [
      { name: 'writeRateBytes',
        endpoint: '/reactor/datasets/' + datasetId + '/dataset.store.bytes?start=now-60s&end=now-0s&count=60' },
      { name: 'writeRateOps',
        endpoint: '/reactor/datasets/' + datasetId + '/dataset.store.writes?start=now-60s&end=now-0s&count=60' },
      { name: 'readRateOps',
        endpoint: '/reactor/datasets/' + datasetId + '/dataset.store.reads?start=now-60s&end=now-0s&count=60' }
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

    dataFactory.getDatasetById(datasetId, function (dataset) {
      $scope.dataset = dataset;
    });

    dataFactory.getFlowsByDataset(datasetId, function(flow) {
      $scope.flow = flow;
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