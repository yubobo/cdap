/*global define */

'use strict';

// Need time ago to make jQuery.timeago work.
define(['angular', 'timeago'], function (angular, timeago) {

/* Filters */

  angular.module('ReactorWebapp.filters', [])
    .filter('interpolate', [function() {
      return function(text) {
        return String(text).replace(/\%VERSION\%/mg, '1');
      }
    }])

    .filter('started', [function() {
      return function(start) {
        return start >= 0 ? jQuery.timeago(start*1000) : 'Never';
      }
    }])

    .filter('startDate', [function() {
      return function(start) {
        return new Date(start*1000).toString();
      }
    }])

    .filter('ended', [function() {
      return function(end) {
        return end >= 0 ? jQuery.timeago(end*1000) : 'Never';
      }
    }])

    .filter('endDate', [function() {
      return function(end) {
        return new Date(end*1000).toString();
      }
    }])

    .filter('statusClass', [function() {
      return function(status) {
        return {
          'stopped': 'label',
          'stopping': 'label label-warning',
          'running': 'label label-success',
          'failed': 'label label-warning'
        }[status.toLowerCase()];
      }
    }])

    .filter('detail', [function() {
      return function(state) {
        return {
          'STOPPED': 'Completed',
          'ERROR': 'Failed'
        }[state];
      }
    }]);


});