
'use strict';

define(function() {

  var Directive = ['$interval', '$timeout', '$compile', 'metricsService', 'POLLING_INTERVAL',
    function($interval, $timeout, $compile, metricsService, POLLING_INTERVAL) {

    var inlineTemplate = ('<div class="sparkline-list-value" style="display: block;">{{curValue}}'
      + '<div class="inline-display" ng-if="percent">%</div></div>');
    var dashTemplate = ('<span class="value-number">{{curValue}}</span>'
      + '<span ng-if="percent">%</span></div>');

    var getTemplate = function (type) {
      var template;
      switch(type) {
        case 'dash':
          template = dashTemplate;
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
        percent: '@',
        countertype: '@',
        metricEndpoint: '@'
      },
      link: function (scope, elm, attrs) {
        
        elm.html(getTemplate(scope.countertype)).show();
        $compile(elm.contents())(scope);

        if (!scope.metricEndpoint) {
          console.error('no metric specified for chart in div', elm);
          return;
        }

        metricsService.trackMetric(scope.metricEndpoint);
        var ival = $interval(function () {
          scope.data = metricsService.getMetricByEndpoint(scope.metricEndpoint);
        }, POLLING_INTERVAL);



        var percent = Boolean(scope.percent);
        var intervals = [];
        var updatePending = false;

        scope.$watch('data', function (newVal, oldVal) {
          if (newVal && angular.isArray(newVal.data) && newVal.data.length) {
            if (!updatePending) {
              updatePending = true;
              var timesToRepeat = (POLLING_INTERVAL / 1000) + 1;
              scope.curValue = newVal.data[newVal.data.length - timesToRepeat].value;
              for (var i = 1; i < timesToRepeat; i++) {
                (function(i) {
                  $timeout(function () {
                    var curIndex = newVal.data.length - (timesToRepeat - i);
                    scope.curValue = newVal.data[curIndex].value;
                    if (i >= 3) {
                      updatePending = false;
                    }
                  }, i * 1000);
                })(i);
                
              }
            }            
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