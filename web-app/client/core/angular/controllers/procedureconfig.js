'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$stateParams', '$state', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, $stateParams, $state, dataFactory, POLLING_INTERVAL) {
    if (!$scope.$parent.hasOwnProperty('procedure')) {
      throw "Route not loaded properly.";
    }
    /**
     * @type {Procedure}
     */
    $scope.procedure = {};

    var statusEndpoints = [];
    var intervals = [];

    var appId = $stateParams.appId;
    var procedureId = $stateParams.procedureId;


    $scope.$watch('$parent.procedure', function (newVal, oldVal) {
      if (angular.isObject(newVal) && Object.keys(newVal).length) {
        $scope.procedure = $scope.$parent.procedure;
        dataFactory.getProcedureConfigByAppNameAndId(appId, procedureId, function (config) {
          $scope.procedure.config = config;
        });
      }
    });


    $scope.closeConfig = function () {
      $state.go('proceduresDetail.status', {
        appId: $scope.procedure.app,
        procedureId: $scope.procedure.name
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