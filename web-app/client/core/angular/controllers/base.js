'use strict';

define(function () {

  var Ctrl = ['$scope', 'dataFactory', 'helpers', 'metricsService', 'statusService',
    function ($scope, dataFactory, helpers, metricsService, statusService) {

    metricsService.initialize();
    statusService.initialize();


    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */    
    $scope.$on("$destroy", function () {
      statusService.destroy();
      metricsService.destroy();
    });

  }];

  return Ctrl;

});