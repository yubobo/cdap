'use strict';

define(function () {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory',
    function($scope, $interval, $routeParams, dataFactory) {

    var appId = $routeParams.appId;
    var streamId = $routeParams.streamId;
    dataFactory.getStreamByAppNameAndId(appId, streamId, function (stream) {
      $scope.stream = stream;
    });

    $scope.$on("$destroy", function(){
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});