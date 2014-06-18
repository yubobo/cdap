'use strict';

define(function () {

  /* Items */

  var Factory = [function() {

    return {
      cancelAllIntervals: function (intervalService, intervals) {
        for (var i = 0; i < intervals.length; i++) {
          intervalService.cancel(intervals[i]);
        }
      }
    }


  }];

  return Factory;

});