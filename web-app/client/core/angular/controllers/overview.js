'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', 'metricsService', '$interval', 'POLLING_INTERVAL',
    function($scope, metricsService, $interval, POLLING_INTERVAL) {

    /**
     * A list of intervals.
     */
    var intervals = [];

    /**
     * List of metrics to track.
     */
    $scope.metrics = {
      'collect': '/reactor/collect.events?count=60&start=now-65s&end=now-5s',
      'process': '/reactor/process.busyness?count=60&start=now-65s&end=now-5s',
      'store': '/reactor/dataset.store.bytes?count=60&start=now-65s&end=now-5s',
      'query': '/reactor/query.requests?count=60&start=now-65s&end=now-5s'
    };

    $scope.getEndpoint = function (entity) {
      return helpers.getOverviewEndpoint(entity);
    }

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