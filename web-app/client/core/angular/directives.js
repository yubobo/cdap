/*global define */

'use strict';

define(['angular'], function(angular) {

/* Directives */

  angular.module('ReactorWebapp.directives', [])
    
    .directive('appVersion', ['version', function(version) {
      return function(scope, elm, attrs) {
        elm.text(version);
      };
    }])
    
    .directive('closeWarning', [function() {
      return {
        scope: true,
        restrict: 'A',
        controller: function($scope, $element){
          $scope.click = function() {
            $("#warning").hide();
          };
        }
      }
    }])

    .directive('includeReplace', [function () {
      return {
        require: 'ngInclude',
        restrict: 'A', /* optional */
        link: function (scope, el, attrs) {
            el.replaceWith(el.children());
        }
      }
    }]);

});