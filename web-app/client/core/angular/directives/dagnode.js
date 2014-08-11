
'use strict';

define(['helpers'], function (helpers) {

  var Directive = ['$interval', '$timeout', '$compile', 'metricsService', 'POLLING_INTERVAL',
    function($interval, $timeout, $compile, metricsService, POLLING_INTERVAL) {

    var dagTemplate = ('<div class="window-input"></div>' +
      '<div class="window-input-label">{{data}}{{element.eventsUnits}}</div>' + 
      '<div class="window-instances">{{element.instances}}</div>' +
      '<div class="window-icon"></div>' +
      '<div class="window-title">{{element.name}}</div>'
    );
    var emptyTemplate = ('<div class="flowletdummy"></div>');

    var getTemplate = function (type) {
      type = type.toLowerCase();
      var template;
      switch(type) {
        case 'dummy':
          template = emptyTemplate;
          break;

        default:
          template = dagTemplate
          break;
      }
      return template;
    };

    return {

      restrict: 'AE',
      transclude: true,
      scope: {
        element: '@',
        metricEndpoint: '@'
      },
      link: function (scope, elm, attrs) {
        var self = this;
        var ival;
        
        scope.$watch('element', function (newVal, oldVal) {
          // Need to parse json because angular doesn't handle prototype classes well.
          if (typeof newVal !== 'string') {
            return;
          }
          
          newVal = JSON.parse(newVal);
          if (newVal && angular.isObject(newVal)) {
            elm.html(getTemplate(newVal.type)).show();
            $compile(elm.contents())(scope);
            scope.element = newVal;
          }
        });

        scope.$watch('metricEndpoint', function (newVal, oldVal) {
          if (newVal) {
            metricsService.trackMetric(newVal);         
            ival = $interval(function () {
              var data = metricsService.getMetricByEndpoint(newVal);
              if (angular.isObject(data) && 'data' in data) {
                scope.data = data.data;
              }
            }, POLLING_INTERVAL);  
          }
        });
        

        scope.$on('$destroy', function() {
          $interval.cancel(ival);
          metricsService.untrackMetric(scope.metricEndpoint);
        });
      },
    }

  }];

  return Directive;

});