
'use strict';

define(function() {

  var Directive = ['$interval', '$timeout', '$compile', 'statusService', 'POLLING_INTERVAL',
    function($interval, $timeout, $compile, statusService, POLLING_INTERVAL) {

    var inlineTemplate = '<span class="app-list-status app-status">{{status}}</span>';
    var largeTemplate = '<div class="flow-state"><strong>{{status}}</strong></div>';

    var getTemplate = function (type) {
      var template;
      switch(type) {
        case 'large':
          template = largeTemplate;
          break;
        default:
         template = inlineTemplate;
         break;
      }
      return template;
    };

    return {

      restrict: 'AE',
      scope: {
        displaytype: '@',
        statusEndpoint: '@'
      },
      link: function (scope, elm, attrs) {
        
        elm.html(getTemplate(scope.displaytype)).show();
        $compile(elm.contents())(scope);

        if (!scope.statusEndpoint) {
          console.error('no endpoint specified for status in element', elm);
          return;
        }

        statusService.trackStatus(scope.statusEndpoint);
        var ival = $interval(function () {
          scope.status = statusService.getStatusByEndpoint(scope.statusEndpoint);
        }, POLLING_INTERVAL);


        scope.$on('$destroy', function() {
          $interval.cancel(ival);
          statusService.untrackStatus(scope.statusEndpoint);
        });
      }
    }

  }];

  return Directive;

});