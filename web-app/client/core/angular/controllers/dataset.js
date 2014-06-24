'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory',
    function($scope, $interval, $routeParams, dataFactory) {

    var appId = $routeParams.appId;
    var datasetId = $routeParams.datasetId;
    dataFactory.getDatasetByAppNameAndId(appId, datasetId, function (dataset) {
      $scope.dataset = dataset;
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});