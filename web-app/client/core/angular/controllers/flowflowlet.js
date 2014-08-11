'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$stateParams', '$state', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, $stateParams, $state, dataFactory, POLLING_INTERVAL) {

    var self = this;

    /**
     * @type {Flow}
     */
    $scope.flow = {};
    /**
     * @type {Flowet|Stream}
     */
    $scope.flowlet = {};
    /**
     * Map between flowlet names and their classes.
     */
    $scope.elementMap = {};
    /**
     * @type [Flowet|Stream]
     */
    $scope.previous = [];
    /**
     * @type [Flowet|Stream]
     */
    $scope.next = [];

    var statusEndpoints = [];
    var intervals = [];

    var appId = $stateParams.appId;
    var flowId = $stateParams.flowId;
    var flowletId = $stateParams.flowletId;


    dataFactory.getFlowByAppNameAndId(appId, flowId, function (flow) {
      $scope.flow = flow;

      // Flowlets come through from the flow, there is no separate endpoint for them. Iterate
      // through the flow and the find the flowlet.
      for (var i = 0; i < $scope.flow.elements.stream.length; i++) {
        $scope.elementMap[$scope.flow.elements.stream[i].name] = $scope.flow.elements.stream[i];
      }

      for (var i = 0; i < $scope.flow.elements.flowlet.length; i++) {
        $scope.elementMap[$scope.flow.elements.flowlet[i].name] = $scope.flow.elements.flowlet[i];
      }
      $scope.flowlet = $scope.elementMap[flowletId];
      setPrevious();
      setNext();

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

    var setPrevious = function () {
      if (!Object.keys($scope.flowlet).length) {
        return;
      }
      for (var i = 0; i < $scope.flow.connections.length; i++) {
        var conn = $scope.flow.connections[i];
        if (conn.targetName === $scope.flowlet.name) {
          $scope.previous.push($scope.elementMap[conn.sourceName]);
        }
      }
    };

    var setNext = function () {
      if (!Object.keys($scope.flowlet).length) {
        return;
      }
      for (var i = 0; i < $scope.flow.connections.length; i++) {
        var conn = $scope.flow.connections[i];
        if (conn.sourceName === $scope.flowlet.name) {
          $scope.next.push($scope.elementMap[conn.targetName]);
        }
      }
    };

  }];

  return Ctrl;

});