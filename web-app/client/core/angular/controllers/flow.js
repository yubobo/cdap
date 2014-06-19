'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', 'dataFactory',
    function($scope, $interval, dataFactory) {

    $scope.message = "apps";

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});