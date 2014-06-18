'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$rootScope', '$scope', '$http', '$routeParams', '$interval',
    function($rootScope, $scope, $http, $routeParams, $interval) {

    $scope.message = "datasets";

    var ival = $interval(function() {
      $.get('/datasets');
    }, 1000);

    $scope.$on("$destroy", function(){
        $interval.cancel(ival);
    });

  }];

  return Ctrl;

});