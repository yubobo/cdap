'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory', 'statusService', 'metricsService', 'helpers', 'POLLING_INTERVAL',
    function($scope, $interval, $routeParams, dataFactory, statusService, metricsService, helpers, POLLING_INTERVAL) {

    var statusEndpoints = [];
    var intervals = [];

    var appId = $routeParams.appId;
    var streamId = $routeParams.streamId;

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