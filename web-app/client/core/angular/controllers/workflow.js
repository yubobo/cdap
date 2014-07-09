'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', '$interval', '$routeParams', 'dataFactory', 'POLLING_INTERVAL',
    function($scope, $interval, $routeParams, dataFactory, POLLING_INTERVAL) {

    var intervals = [];

    /**
     * @type {Workflow}
     */
    $scope.workflow = {};

    var appId = $routeParams.appId;
    var workflowId = $routeParams.workflowId;


    dataFactory.getWorkflowByAppNameAndId(appId, workflowId, function (workflow) {
      $scope.workflow = workflow;
    });

    $scope.$on("$destroy", function(){

      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });


  }];

  return Ctrl;

});