'use strict';

define(function () {

  var Ctrl = ['$scope', 'dataFactory', 'metricsService', 'statusService',
    function ($scope, dataFactory, metricsService, statusService) {

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