'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory',
    function($scope, $interval, $routeParams, dataFactory) {

    var appId = $routeParams.appId;
    var procedureId = $routeParams.procedureId;
    dataFactory.getProcedureByAppNameAndId(appId, procedureId, function (procedure) {
      $scope.procedure = procedure;
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});