'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', 'dataFactory', 'statusService', 'helpers', 'POLLING_INTERVAL',
    function($scope, $interval, dataFactory, statusService, helpers, POLLING_INTERVAL) {

    var statusEndpoints = [];
    var intervals = [];
    $scope.procedures = [];

    dataFactory.getProcedures (function (procedures) {
      $scope.procedures = procedures;

      $scope.procedures.forEach(function (procedure) {
        procedure.endpoint = helpers.getEndpoint(procedure);
        statusService.trackStatus(procedure.endpoint);
        statusEndpoints.push(procedure.endpoint);
      });

      intervals.push($interval(function () {
        $scope.procedures.forEach(function (procedure) {
          var status = statusService.getStatusByEndpoint(procedure.endpoint);
          procedure.status = status;
        });
      }, POLLING_INTERVAL));

    });

    $scope.$on("$destroy", function() {

      for (var i = 0, len = statusEndpoints.length; i < len; i++) {
        statusService.untrackStatus(statusEndpoints[i]);
      }

      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });

  }];

  return Ctrl;

});