'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$rootScope', '$scope', '$http', '$stateParams', '$interval',
    function($rootScope, $scope, $http, $stateParams, $interval) {

    $scope.message = "resources";

    var ival = $interval(function() {
      $.get('/resources');
    }, 1000);

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);  
      }
    });

  }];

  return Ctrl;

});