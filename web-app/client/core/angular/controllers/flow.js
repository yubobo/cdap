'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$stateParams', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, $stateParams, dataFactory, POLLING_INTERVAL) {

    /**
     * @type {Flow}
     */
    $scope.flow = {};

    var statusEndpoints = [];
    var intervals = [];

    var appId = $stateParams.appId;
    var flowId = $stateParams.flowId;


    dataFactory.getFlowByAppNameAndId(appId, flowId, function (flow) {
      $scope.flow = flow;
      $scope.flow.app = appId;
    });

    $scope.$on("$destroy", function(){

      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});