'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', 'dataFactory',
    function($scope, $interval, dataFactory) {

    $scope.streams = [];

    dataFactory.getStreams (function (streams) {
      $scope.streams = streams;
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});