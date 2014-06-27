
'use strict';

define(function() {

  var Directive = ['$interval', '$timeout', 'POLLING_INTERVAL',
    function($interval, $timeout, POLLING_INTERVAL) {

    return {

      restrict: 'AE',
      scope: {
        data: "=",
        percent: "@",
      },
      template: ('<div class="sparkline-list-value" style="display: block;">{{curValue}}'
      +'<ng-if="percent">%</ng-if></div>'),
      link: function (scope, elm, attrs) {
        var percent = Boolean(scope.percent);
        var intervals = [];
        var updatePending = false;

        scope.$watch('data', function (newVal, oldVal) {
          if (newVal && angular.isArray(newVal) && newVal.length) {
            if (!updatePending) {
              updatePending = true;
              var timesToRepeat = (POLLING_INTERVAL / 1000) + 1;
              scope.curValue = newVal[newVal.length - timesToRepeat].value;
              for (var i = 1; i < timesToRepeat; i++) {
                (function(i) {
                  $timeout(function () {
                    var curIndex = newVal.length - (timesToRepeat - i);
                    scope.curValue = newVal[curIndex].value;
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
          for (var i = 0, len = intervals.length; i < len; i++) {
            $interval.cancel(intervals[i]);
          }
        });
      }
    }

  }];

  return Directive;

});