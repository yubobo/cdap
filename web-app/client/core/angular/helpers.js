'use strict';

define(function () {

    var notificationContainer = $('#warning');
    var notificationSpan = $('#warning .warning-text');
    var DANGER_COLOR = '#E93B51';
    var SUCCESS_COLOR = '#5cb85c';
    var INFO_COLOR = '#5bc0de';
    var WARNING_COLOR = '#f0ad4e';
    var DEFAULT_COLOR = '#777';

    return {

      cancelAllIntervals: function (intervalService, intervals) {
        for (var i = 0; i < intervals.length; i++) {
          intervalService.cancel(intervals[i]);
        }
      },

      displayNotification: function (message, category, displayTime) {

        // Hide any existing notifications.
        notificationContainer.hide();

        notificationSpan.html(message);

        // Determine color.
        var color = DEFAULT_COLOR;

        var category = category.toLowerCase();
        if (category == 'danger') {
          color = DANGER_COLOR;
        } else if (category == 'warning') {
          color = WARNING_COLOR;
        } else if (category == 'success') {
          color = SUCCESS_COLOR;
        } else if (category == 'info') {
          color = INFO_COLOR;
        }

        notificationContainer.find('div').css('background-color', color);

        // Show the notification.
        notificationContainer.show();

        // If display time is provided, show only for that period of time and then hide.
        if (typeof displayTime === 'number') {
          setTimeout(function () {
            notificationContainer.fadeOut();
          }, displayTime);
        }
      },

      getLastValue: function (metricData) {
        return metricData[metricData.length - 1].value;
      },

      getStatusEndpoint: function (entity, optionalAppName) {

        switch(entity.type.toLowerCase()) {

          case 'flow':
            if (optionalAppName) {
              return '/apps/' + optionalAppName + '/flows/' + entity.name + '/status';
            }
            return '/apps/' + entity.app + '/flows/' + entity.name + '/status';


          case 'mapreduce':
            if (optionalAppName) {
              return '/apps/' + optionalAppName + '/mapreduce/' + entity.name + '/status';
            }
            return '/apps/' + entity.app + '/mapreduce/' + entity.name + '/status';


          case 'workflow':
            if (optionalAppName) {
              return '/apps/' + optionalAppName + '/workflows/' + entity.name + '/status';
            }
            return '/apps/' + entity.app + '/workflows/' + entity.name + '/status';


          case 'procedure':
            if (optionalAppName) {
              return '/apps/' + optionalAppName + '/procedures/' + entity.name + '/status';
            }
            return '/apps/' + entity.app + '/procedures/' + entity.name + '/status';


          default:
            break;
        }
      },


      getBusynessEndpoint: function (entity, optionalAppName) {

        switch(entity.type.toLowerCase()) {

          case 'app':
            return ('/reactor/apps/' + entity.name
              + '/process.busyness?start=now-60s&end=now-0s&count=60');

          case 'flow':
            return ('/reactor/apps/' + entity.app + '/flows/' + entity.name
              + '/process.busyness?start=now-60s&end=now-0s&count=60');

          default:
            break;
        }
      },

      getEventsProcessedEndpoint: function (entity) {

        switch(entity.type.toLowerCase()) {

          case 'app':
            return ('/reactor/apps/' + entity.name
              + '/process.events.processed?start=now-60s&end=now-0s&count=60"');

          case 'flow':
            return ('/reactor/apps/' + entity.app + '/flows/' + entity.name
              + '/process.events.processed?start=now-60s&end=now-0s&count=60"');

          default:
            break;
        }
      },



      getRequestRateEndpoint: function (entity, optionalAppName) {

        switch(entity.type.toLowerCase()) {

          case 'procedure':
            return ('/reactor/apps/' + optionalAppName + '/procedures/' + entity.name + '/query.requests?start=now-60s&end=now-0s&count=60');

          default:
            break;
        }
      },

      getErrorRateEndpoint: function (entity, optionalAppName) {

        switch(entity.type.toLowerCase()) {

          case 'procedure':
            return ('/reactor/apps/' + optionalAppName + '/procedures/' + entity.name + '/query.failures?start=now-60s&end=now-0s&count=60');

          default:
            break;
        }
      },

      getMappingStatusEndpoint: function (entity, optionalAppName) {

        switch(entity.type.toLowerCase()) {

          case 'mapreduce':
            return ('/reactor/apps/' + optionalAppName + '/mapreduces/' + entity.name + '/mappers/process.completion?start=now-60s&end=now-0s&count=60');
            break;

          default:
            break;
        }
      },

      getReducingStatusEndpoint: function (entity, optionalAppName) {

        switch(entity.type.toLowerCase()) {

          case 'mapreduce':
            return ('/reactor/apps/' + optionalAppName + '/mapreduces/' + entity.name + '/reducers/process.completion?start=now-60s&end=now-0s&count=60');
            break;

          default:
            break;
        }
      },

      getOverviewEndpoint: function (entity) {

        switch(entity.toLowerCase()) {

          case 'collect':
            return '/reactor/collect.events?count=60&start=now-65s&end=now-5s';

          case 'process':
            return '/reactor/process.busyness?count=60&start=now-65s&end=now-5s';

          case 'store':
            return '/reactor/dataset.store.bytes?count=60&start=now-65s&end=now-5s';

          case 'query':
            return '/reactor/query.requests?count=60&start=now-65s&end=now-5s';

          default:
            break;
        }
      },

      getStartEndpoint: function (entity) {
        switch(entity.type.toLowerCase()) {
          case 'flow':
            return '/rest/apps/' + entity.app + '/flows/' + entity.name + '/start';

          case 'procedure':
            return '/rest/apps/' + entity.app + '/procedures/' + entity.name + '/start';

          default:
            break;
        }
      },

      getStopEndpoint: function (entity) {
        switch(entity.type.toLowerCase()) {
          case 'flow':
            return '/rest/apps/' + entity.app + '/flows/' + entity.name + '/stop';

          case 'procedure':
            return '/rest/apps/' + entity.app + '/procedures/' + entity.name + '/stop';

          default:
            break;
        }
      }

    }

});