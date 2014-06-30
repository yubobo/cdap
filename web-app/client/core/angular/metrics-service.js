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
      pendingRequest: false,

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

      untrackMetric: function (metricEndpoint) {
        var self = this;
        var arrPos = self.metricsQueue.indexOf(metricEndpoint);
        if (arrPos !== -1) {
          self.metricsQueue.splice(arrPos, 1);
        }
      },

      fetchMetricsFromServer: function () {
        var self = this;
        if (self.metricsQueue.length && !self.pendingRequest) {
          self.pendingRequest = true;
          $http({
            method: 'POST',
            url: '/metrics',
            data: self.metricsQueue,
          }).success(function (data, status, headers, config) {
            self.registerRequestComplete();
            self.processResponse(data, status);
          }).error(function (data, status, headers, config) {
            self.registerRequestComplete();
            console.log("error", arguments);
          });
        }
      },

      registerRequestComplete: function () {
        this.pendingRequest = false;
      },

      getMetricByEndpoint: function (endpoint) {
        var self = this;
        if (endpoint in self.metricsResults) {
          return self.metricsResults[endpoint];
        }        
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