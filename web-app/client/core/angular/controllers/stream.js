'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$stateParams', 'dataFactory', 'statusService', 'metricsService', 'POLLING_INTERVAL',
    function($scope, $interval, $stateParams, dataFactory, statusService, metricsService, POLLING_INTERVAL) {

    var streamId = $stateParams.streamId;

    dataFactory.getStreamById(streamId, function (stream) {
      $scope.stream = stream;
      dataFactory.getFlowsByStream(streamId, function (flows) {
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