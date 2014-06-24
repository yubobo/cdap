'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory', 'statusService', 'helpers', 'POLLING_INTERVAL',
    function($scope, $interval, $routeParams, dataFactory, statusService, helpers, POLLING_INTERVAL) {

    var statusEndpoints = [];
    var intervals = [];
    var appId = $routeParams.appId;
    var procedureId = $routeParams.procedureId;
    dataFactory.getProcedureByAppNameAndId(appId, procedureId, function (procedure) {
      $scope.procedure = procedure;
      console.log($scope.procedure);


        $scope.procedure.endpoint = helpers.getEndpoint($scope.procedure, appId);
        console.log($scope.procedure.endpoint);
        statusService.trackStatus($scope.procedure.endpoint);
        statusEndpoints.push($scope.procedure.endpoint);

      intervals.push($interval(function () {
          var status = statusService.getStatusByEndpoint($scope.procedure.endpoint);
          $scope.procedure.status = status;
      }, POLLING_INTERVAL));
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});