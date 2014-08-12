'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$stateParams', 'dataFactory', 'statusService', 'POLLING_INTERVAL',
    function($scope, $interval, $stateParams, dataFactory, statusService, POLLING_INTERVAL) {

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
      

      // Track status because we use it for a start/execute button.
      statusService.trackStatus($scope.procedure.getStatusEndpoint());
      var ival = $interval(function () {
        $scope.status = statusService.getStatusByEndpoint($scope.procedure.getStatusEndpoint());
      }, POLLING_INTERVAL);
      intervals.push(ival);
    });


    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */
    $scope.$on("$destroy", function () {
      if (Object.keys($scope.procedure).length) {
        statusService.untrackStatus($scope.procedure.getStatusEndpoint());  
      }      
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});