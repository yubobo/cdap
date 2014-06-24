'use strict';

define(function () {

  var StatusService = [
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

      statusQueue: [],
      statusResults: {},

      initialize: function () {
        var self = this;

        self.interval = $interval(function () {
          self.fetchStatusesFromServer();
        }, POLLING_INTERVAL);
      },

      trackStatus: function (statusEndpoint) {
        var self = this;
        if (self.statusQueue.indexOf(statusEndpoint) === -1) {
          self.statusQueue.push(statusEndpoint);
        }
      },

      untrackStatus: function (statusEndpoint) {
        var self = this;
        var arrPos = self.statusQueue.indexOf(statusEndpoint);
        if (arrPos !== -1) {
          self.statusQueue.splice(arrPos, 1);
        }
      },

      fetchStatusesFromServer: function () {
        var self = this;
        this.statusQueue.forEach(function (statusEndpoint) {
          $http({
            method: 'GET',
            url: '/rest' + statusEndpoint
          }).success(function (data, status, headers, config) {
            self.processResponse(statusEndpoint, data, status);
          }).error(function (data, status, headers, config) {
            console.log("error", arguments);
          });
        });
          
      },

      getStatusByEndpoint: function (endpoint) {
        var self = this;
        if (endpoint in self.statusResults) {
          return self.statusResults[endpoint];  
        }
      },

      processResponse: function (statusEndpoint, data, status) {
        var self = this;
        if (status === 200 && 'status' in data) {
          self.statusResults[statusEndpoint] = data.status;
        }
      },

      destroy: function () {
        $interval.cancel(this.interval);
      }
      
    };


  }];

  return StatusService;

});