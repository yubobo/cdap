'use strict';

define(function () {

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

      getEndpoint: function (entity, optionalAppName) {

        switch(entity.type.toLowerCase()) {

          case 'flow':
            if (optionalAppName) {
              return '/apps/' + optionalAppName + '/flows/' + entity.name + '/status';
            }
            return '/apps/' + entity.app + '/flows/' + entity.name + '/status';
            break;

          case 'mapreduce':
            if (optionalAppName) {
              return '/apps/' + optionalAppName + '/mapreduce/' + entity.name + '/status';
            }
            return '/apps/' + entity.app + '/mapreduce/' + entity.name + '/status';
            break;

          case 'workflow':
            if (optionalAppName) {
              return '/apps/' + optionalAppName + '/workflows/' + entity.name + '/status';
            }
            return '/apps/' + entity.app + '/workflows/' + entity.name + '/status';
            break;

          case 'procedure':
            if (optionalAppName) {
              return '/apps/' + optionalAppName + '/procedures/' + entity.name + '/status';
            }
            return '/apps/' + entity.app + '/procedures/' + entity.name + '/status';
            break;

          default:
            break;
        }
      },


      getBusynessEndpoint: function (entity, optionalAppName) {

        switch(entity.type.toLowerCase()) {

          case 'app':
            return ('/reactor/apps/' + entity.name 
              + '/process.busyness?start=now-60s&end=now-0s&count=60');
            break;

          default:
            break;
        }
      },

      getOverviewEndpoint: function (entity) {
        
        switch(entity.toLowerCase()) {

          case 'collect':
            return '/reactor/collect.events?count=60&start=now-65s&end=now-5s';
            break;

          case 'process':
            return '/reactor/process.busyness?count=60&start=now-65s&end=now-5s';
            break;

          case 'store':
            return '/reactor/dataset.store.bytes?count=60&start=now-65s&end=now-5s';
            break;

          case 'query':
            return '/reactor/query.requests?count=60&start=now-65s&end=now-5s';
            break;

          default:
            break;
        }
      }

    }

});