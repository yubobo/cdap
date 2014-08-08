
'use strict';

define(['helpers'], function (helpers) {

  var Directive = ['$interval', '$timeout', '$compile', 'actionService', 'statusService', 'POLLING_INTERVAL',
    function($interval, $timeout, $compile, actionService, statusService, POLLING_INTERVAL) {

    return {

      restrict: 'AE',
      replace: true,
      link: function (scope, elm, attrs) {
        console.log(scope);


        scope.$on('$destroy', function() {
          if (typeof ival !== 'undefined') {
            $interval.cancel(ival);
          }
          
          metricsService.untrackStatus(scope.metricEndpoint);
        });
      }
    }

  }];

  return Directive;

});