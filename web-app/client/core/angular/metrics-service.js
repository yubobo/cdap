'use strict';

define(function () {

  var MetricsService = [
    '$http',
    '$interval',
    '$q',
    'REACTOR_ENDPOINT',
    'POLLING_INTERVAL',
    
    function (
      $http,
      $interval,
      $q,
      REACTOR_ENDPOINT,
      POLLING_INTERVAL) {

    return {

      metricsQueue: [],
      metricsResults: {},

      initialize: function () {
        var self = this;

        self.interval = $interval(function () {
          self.fetchMetricsFromServer();
        }, POLLING_INTERVAL);
      },

      trackMetric: function (metricEndpoint) {
        var self = this;
        if (self.metricsQueue.indexOf(metricEndpoint) === -1) {
          self.metricsQueue.push(metricEndpoint);
        }
      },

      fetchMetricsFromServer: function () {
        var self = this;
        if (self.metricsQueue.length) {
          $http({
            method: 'POST',
            url: '/metrics',
            data: self.metricsQueue,
          }).success(function (data, status, headers, config) {
            self.processResponse(data, status);
          }).error(function (data, status, headers, config) {
            console.log("error", arguments);
          });
        }
      },

      getMetricByEndpoint: function (endpoint) {
        var self = this;
        return self.metricsResults[endpoint];
      },

      processResponse: function (data, status) {
        var self = this;
        if (status === 200 && data.error === null) {
          data.result.forEach(function (metricResponse) {
            self.metricsResults[metricResponse.path] = metricResponse.result;
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