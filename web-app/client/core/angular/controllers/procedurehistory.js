'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$stateParams', '$state', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, $stateParams, $state, dataFactory, POLLING_INTERVAL) {
    if (!$scope.$parent.hasOwnProperty('procedure')) {
      throw "Route not loaded properly.";
    }
    /**
     * @type {Flow}
     */
    $scope.procedure = {};

    var statusEndpoints = [];
    var intervals = [];

    var appId = $stateParams.appId;
    var procedureId = $stateParams.procedureId;

    $scope.$watch('$parent.procedure', function (newVal, oldVal) {
      if (angular.isObject(newVal) && Object.keys(newVal).length) {
        $scope.procedure = $scope.$parent.procedure;
        dataFactory.getProcedureHistoryByAppNameAndId(appId, procedureId, function (history) {
          $scope.procedure.history = history;
        });
      }
    });
  

    $scope.$on("$destroy", function() {
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});