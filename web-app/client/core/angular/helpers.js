'use strict';

define(function () {

  /* Items */

  var Factory = [function() {

    var warningContainer = $('#warning');
    var warningSpan = $('#warning .warning-text');
    
    return {
      
      cancelAllIntervals: function (intervalService, intervals) {
        for (var i = 0; i < intervals.length; i++) {
          intervalService.cancel(intervals[i]);
        }
      },

      displayWarning: function (message) {
        warningContainer.hide();
        warningSpan.html(message);
        warningContainer.show();
      },

      getLastValue: function (metricData) {
        return metricData[metricData.length - 1].value;
      }
    }


  }];

  return Factory;

});