'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$stateParams', '$state', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, $stateParams, $state, dataFactory, POLLING_INTERVAL) {

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
      dataFactory.getFlowConfigByAppNameAndId(appId, flowId, function (config) {
        $scope.flow.config = config;
      });
    });


    $scope.closeConfig = function () {
      $state.go('flowsDetail.status', {
        appId: $scope.flow.app,
        flowId: $scope.flow.name
      });
    };

    $scope.$on("$destroy", function() {
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});