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
      },

      getEndpoint: function (entity) {

        switch(entity.type.toLowerCase()) {

          case 'flow':
            return '/apps/' + entity.app + '/flows/' + entity.id + '/status';
            break;

          case 'mapreduce':
            return '/apps/' + entity.app + '/mapreduce/' + entity.id + '/status';
            break;

          case 'workflow':
            return '/apps/' + entity.app + '/workflows/' + entity.id + '/status';
            break;
          
          case 'procedure':
            return '/apps/' + entity.app + '/procedures/' + entity.id + '/status';                         
            break;

          default:
            break;
        }
      }

    }
  }];

  return Factory;

});