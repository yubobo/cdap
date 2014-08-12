
'use strict';

define(['helpers'], function (helpers) {

  var Directive = ['$interval', '$timeout', '$compile', 'metricsService', 'POLLING_INTERVAL',
    function($interval, $timeout, $compile, metricsService, POLLING_INTERVAL) {

    var inlineTemplate = ('<div class="sparkline-list-value" style="display: block;">{{curValue}}{{curLabel}}'
      + '<div class="inline-display" ng-if="percent">%</div></div>');
    var nonbytesTemplate = ('<div class="sparkline-list-value" style="display: block;">{{curValue}}'
      + '<div class="inline-display" ng-if="percent">%</div></div>');
    var dashTemplate = ('<span class="value-number">{{curValue}}{{curLabel}}</span>'
      + '<span ng-if="percent">%</span></div>');

    var getTemplate = function (type) {
      var template;
      switch(type) {
        case 'dash':
          template = dashTemplate;
          break;
        case 'non-bytes':
          template = nonbytesTemplate;
          break;
        default:
         template = inlineTemplate;
         break;
      }
      return template;
    };

    return {

      restrict: 'AE',
      replace: true,
      scope: {
        countertype: '@',
        metricEndpoint: '@'
      },
      link: function (scope, elm, attrs) {
        var updatePending = false;
        var ival;

        scope.$watch('metricEndpoint', function (newVal, oldVal) {
          if (newVal) {
            if (!scope.metricEndpoint) {
              console.error('no metric specified for chart in div', elm);
              return;
            }
            
            elm.html(getTemplate(scope.countertype)).show();
            $compile(elm.contents())(scope);
            metricsService.trackMetric(scope.metricEndpoint);
            ival = $interval(function () {
              scope.data = metricsService.getMetricByEndpoint(scope.metricEndpoint);
            }, POLLING_INTERVAL);
          }
        });

        var bytesData = [];
        scope.$watch('data', function (newVal, oldVal) {
          if (angular.isObject(newVal) && newVal.hasOwnProperty('data')) {
            bytesData = helpers.bytes(newVal.data);
            scope.curValue = bytesData[0];
            scope.curLabel = bytesData[1];
          }
        });


        scope.$on('$destroy', function() {
          $interval.cancel(ival);
          metricsService.untrackMetric(scope.metricEndpoint);
        });
      }
    }

  }];

  return Directive;

});