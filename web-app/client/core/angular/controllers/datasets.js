'use strict';

define(function () {


  var Ctrl = ['$scope', '$interval', 'dataFactory',
    function($scope, $interval, dataFactory) {

    /**
     * List of Dataset objects.
     */
    $scope.datasets = [];

    dataFactory.getDatasets(function (datasets) {
      $scope.datasets = datasets;
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });

  }];

  return Ctrl;

});