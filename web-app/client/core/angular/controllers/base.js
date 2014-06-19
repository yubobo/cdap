'use strict';

define(function () {

  var Ctrl = ['$scope', 'dataFactory', 'helpers', 'metricsService', 'POLLING_INTERVAL',
    function ($scope, dataFactory, helpers, metricsService, POLLING_INTERVAL) {

    metricsService.initialize();


    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */    
    $scope.$on("$destroy", function () {
      metricsService.destroy();
    });

  }];

  return Ctrl;

});