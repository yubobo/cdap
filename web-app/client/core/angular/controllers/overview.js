'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$rootScope', '$scope', '$http', '$routeParams',
    function($rootScope, $scope, $http, $routeParams) {

    $scope.message = "overview";

    $scope.value = {
      collect: 100
    }

  }];

  return Ctrl;

});