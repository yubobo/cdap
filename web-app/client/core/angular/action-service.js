'use strict';

define(['helpers'], function (helpers) {

  var ActionService = [
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

      callStart: function (endpoint, successCb, errorCb) {
        $http({
          method: 'POST',
          url: endpoint
        }).success(function (data, status, headers, config) {

          if (typeof callback === 'function') {
            successCb(data, status, headers, config);
          }

        }).error(function (data, status, headers, config) {

          if (typeof callback === 'function') {
            errorCb(data, status, headers, config);  
          }

        });
      },

      startFlow: function (flow, successCb, errorCb) {
        var endpoint = helpers.getStartEndpoint(flow);
        this.callStart(endpoint, successCb, errorCb);
      },

      startProcedure: function (procedure, successCb, errorCb) {
        var endpoint = helpers.getStartEndpoint(procedure);
        this.callStart(endpoint, successCb, errorCb);
      },
      
    };


  }];

  return ActionService;

});