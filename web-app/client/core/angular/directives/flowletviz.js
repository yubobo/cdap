
'use strict';

define(['helpers'], function (helpers) {

  var Directive = ['$interval', '$timeout', '$compile', 'POLLING_INTERVAL',
    function($interval, $timeout, $compile, POLLING_INTERVAL) {

    return {

      restrict: 'AE',
      templateUrl: '/templates/directives/flowletviz.html',
      link: function (scope, elm, attrs) {

        scope.$watch('flowlet', function (newVal, oldval) {

          if(newVal && angular.isObject(newVal) && Object.keys(newVal).length) {
            console.log(newVal);
          }

        });


        scope.$on('$destroy', function() {
        });
      }
    }

  }];

  return Directive;

});