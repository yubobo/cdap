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

      callEndpoint: function (endpoint, successCb, errorCb) {
        if (!endpoint) {
          console.error('No endpoint specified for action call.');
          return;  
        }
        $http({
          method: 'POST',
          url: endpoint
        }).success(function (data, status, headers, config) {

          if (typeof successCb === 'function') {
            successCb(data, status, headers, config);
          }

        }).error(function (data, status, headers, config) {

          if (typeof errorCb === 'function') {
            errorCb(data, status, headers, config);  
          }

        });
      },

      startFlow: function (flow, successCb, errorCb) {
        this.callEndpoint(flow.getStartEndpoint(), successCb, errorCb);
      },

      startProcedure: function (procedure, successCb, errorCb) {
        this.callEndpoint(procedure.getStartEndpoint(), successCb, errorCb);
      },
      
      stopFlow: function (flow, successCb, errorCb) {
        this.callEndpoint(flow.getStopEndpoint(), successCb, errorCb);
      },

      stopProcedure: function (procedure, successCb, errorCb) {
        this.callEndpoint(procedure.getStopEndpoint(), successCb, errorCb);
      }
    };


  }];

  return ActionService;

});