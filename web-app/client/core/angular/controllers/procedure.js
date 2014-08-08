'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$stateParams', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, $stateParams, dataFactory, POLLING_INTERVAL) {

    /**
     * @type {Procedure}
     */
    $scope.procedure = {};

    var intervals = [];
    var statusEndpoints = [];

    var appId = $stateParams.appId;
    var procedureId = $stateParams.procedureId;


    dataFactory.getProcedureByAppNameAndId(appId, procedureId, function (procedure) {
      $scope.procedure = procedure;
    });

    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */
    $scope.$on("$destroy", function () {

      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});