
'use strict';

define(['helpers'], function (helpers) {

  var Directive = ['$interval', '$timeout', '$compile', 'POLLING_INTERVAL',
    function($interval, $timeout, $compile, POLLING_INTERVAL) {

    return {

      restrict: 'AE',
      templateUrl: '/templates/directives/keyval.html',
      scope: {
        data: '='
      },
      link: function (scope, elm, attrs) {
        scope.addingKey = '';
        scope.addingVal = '';


        scope.add = function () {
          if (typeof scope.addingKey === 'undefined' || typeof scope.addingVal === 'undefined') {
            return;
          }
          scope.data[scope.addingKey] = scope.addingVal;
          scope.addingKey = '';
          scope.addingVal = '';          
        },

        scope.remove = function (key) {
          delete scope.data[key];
        },

        scope.$on('$destroy', function() {
        });
      }
    }

  }];

  return Directive;

});