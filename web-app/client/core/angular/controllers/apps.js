'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$http', '$interval', 'dataFactory', 'helpers', 'METRICS_TIMER',
    function($scope, $http, $interval, dataFactory, helpers, METRICS_TIMER) {

    /** 
     * A list of app objects containing.
     * @type {Array}
     */
    $scope.apps = [];

    var intervals = [];

    dataFactory.getApps(function(apps) {
      $scope.apps = apps;
    });

    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */
    $scope.$on("$destroy", function() {
      helpers.cancelAllIntervals($interval, intervals);
    });
  }];

  return Ctrl;

});