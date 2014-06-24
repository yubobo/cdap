'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory',
    function($scope, $interval, $routeParams, dataFactory) {

    var appId = $routeParams.appId;
    var flowId = $routeParams.flowId;
    dataFactory.getFlowByAppNameAndId(appId, flowId, function (flow) {
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