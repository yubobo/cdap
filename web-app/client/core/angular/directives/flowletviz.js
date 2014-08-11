'use strict';

define(['helpers'], function (helpers) {

  var Directive = ['$interval', '$timeout', '$compile', 'POLLING_INTERVAL',
    function($interval, $timeout, $compile, POLLING_INTERVAL) {

    return {

      restrict: 'AE',
      templateUrl: function (elem, attrs) {
        var url = '/templates/directives/flowflowletviz.html';
        if (attrs.hasOwnProperty('type')) {
          if (attrs.type.toLowerCase() === 'stream') {
            url = '/templates/directives/flowstreamviz.html';
          }
        }
        return url;
      },
      link: function (scope, elm, attrs) {

        scope.toggleTab = function (tabName) {
          jQuery('.flowlet-popup-tab-content').hide();
          jQuery('#flowlet-popup-' + tabName).show();
          jQuery('.flowlet-popup-tab').removeClass('tab-selected');
          jQuery('#flowlet-popup-' + tabName + '-tab').addClass('tab-selected');
        };


        scope.$on('$destroy', function() {
        });
      }
    }

  }];

  return Directive;

});