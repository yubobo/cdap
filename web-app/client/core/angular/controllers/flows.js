'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', 'dataFactory',
    function($scope, $interval, dataFactory) {

    dataFactory.getFlows(function (flows) {
      $scope.flows = flows;
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });

  }];

  return Ctrl;

});