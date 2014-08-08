'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$stateParams', 'dataFactory', 'statusService', 'metricsService', 'helpers', 'POLLING_INTERVAL',
    function($scope, $interval, $stateParams, dataFactory, statusService, metricsService, helpers, POLLING_INTERVAL) {

    var statusEndpoints = [];
    var intervals = [];

    var appId = $stateParams.appId;
    var streamId = $stateParams.streamId;

    $scope.arrivalRate = [];

    var metrics = [
      { name: 'arrivalRate',
        endpoint: '/reactor/apps/' + appId + '/procedures/' + procedureId + '/query.requests?start=now-60s&end=now-0s&count=60' }
    ];

    dataFactory.getStreamByAppNameAndId(appId, streamId, function (stream) {
      $scope.stream = stream;
    });


    dataFactory.getFlowsByStream(streamId, function(flow) {
      $scope.flow = flow;
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});