'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, $routeParams, dataFactory, POLLING_INTERVAL) {

    /**
     * @type {Mapreduce}
     */
    $scope.mapreduce = {};

    var appId = $routeParams.appId;
    var mapreduceId = $routeParams.mapreduceId;


    dataFactory.getMapreduceByAppNameAndId(appId, mapreduceId, function (mapreduce) {
      $scope.mapreduce = mapreduce;
    });

    $scope.$on("$destroy", function(){

      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});