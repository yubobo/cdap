'use strict';

define(function () {

  var Ctrl = ['$scope', 'dataFactory', 'helpers', 'metricsService', 'METRICS_TIMER',
    function ($scope, dataFactory, helpers, metricsService, METRICS_TIMER) {

    metricsService.initialize();
    
      

  }];

  return Ctrl;

});