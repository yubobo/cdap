'use strict';

define(function () {

  var MetricsService = [
    '$http',
    '$interval',
    '$q',
    'REACTOR_ENDPOINT',
    'METRICS_TIMER',
    
    function (
      $http,
      $interval,
      $q,
      REACTOR_ENDPOINT,
      METRICS_TIMER) {

    return {

      metricsQueue: [],
      metricsResults: {},

      initialize: function () {
        var self = this;

        self.interval = $interval(function () {
          self.fetchMetrics();
        }, METRICS_TIMER);
      },

      trackMetric: function (metricEndpoint)  {
        var self = this;
        if (metricEndpoint in self.metricsQueue) {
          self.metricsQueue.push(metricName);
        } else {
          self.metricsQueue = metricName;
        }
      },

      fetchMetrics: function () {
        var self = this;
        if (self.metricsQueue.length) {
          $http({
            method: 'POST',
            url: '/metrics',
            data: self.metricsQueue,
          }).success(function (data, status, headers, config) {
            console.log("success", arguments);
          }).error(function (data, status, headers, config) {
            console.log("error", arguments);
          });
        }
      },

      destroy: function () {
        $interval.cancel(this.interval);
      }
      
    };


  }];

  return MetricsService;

});