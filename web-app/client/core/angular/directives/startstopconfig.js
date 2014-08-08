
'use strict';

define(['helpers'], function (helpers) {

  var Directive = ['$interval', '$timeout', '$compile', '$state', 'actionService', 'statusService', 'POLLING_INTERVAL',
    function($interval, $timeout, $compile, $state, actionService, statusService, POLLING_INTERVAL) {

    return {
      templateUrl: '/templates/directives/startstopconfig.html',
      restrict: 'AE',
      replace: true,
      scope: {
        entity: '='
      },
      link: function (scope, elm, attrs) {

        scope.redirectToConfig = function () {
          var params = {};
          params['appId'] = scope.entity.app;
          params[scope.entity.type.toLowerCase() + 'Id'] = scope.entity.name;
          $state.go($state.current.name + '.config', params);
        };

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