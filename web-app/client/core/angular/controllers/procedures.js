'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', 'dataFactory',
    function($scope, $interval, dataFactory) {

    $scope.procedures = [];

    dataFactory.getProcedures (function (procedures) {
      $scope.procedures = procedures;
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });

  }];

  return Ctrl;

});