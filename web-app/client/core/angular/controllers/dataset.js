'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$stateParams', 'dataFactory', 'statusService', 'metricsService', 'POLLING_INTERVAL',
    function($scope, $interval, $stateParams, dataFactory, statusService, metricsService, POLLING_INTERVAL) {

    /**
     * Dataset object.
     */
    $scope.dataset = {};

    var datasetId = $stateParams.datasetId;

    dataFactory.getDatasetById(datasetId, function (dataset) {
      $scope.dataset = dataset;
      dataFactory.getFlowsByDataset(datasetId, function (flows) {
        $scope.flows = flows;
      });
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});