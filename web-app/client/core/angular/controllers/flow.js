'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, $routeParams, dataFactory, POLLING_INTERVAL) {

    var statusEndpoints = [];
    var intervals = [];

    var appId = $routeParams.appId;
    var flowId = $routeParams.flowId;

    //$scope.processingRate = [];
    //$scope.busyness = [];

    /*var metrics = [
      { name: 'processingRate',
        endpoint: '/reactor/apps/' + appId + '/flows/' + flowId + '/process.events.processed?start=now-60s&end=now-0s&count=6' },
      { name: 'busyness',
        endpoint: '/reactor/apps/' + appId + '/flows/' + flowId + '/process.busyness?start=now-60s&end=now-0s&count=60'}
    ];*/

    dataFactory.getFlowByAppNameAndId(appId, flowId, function (flow) {
      $scope.flow = flow;
    });

    $scope.$on("$destroy", function(){
      for (var i = 0, len = metrics.length; i < len; i++) {
        metricsService.untrackMetric(metrics[i].endpoint);
      }

      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});