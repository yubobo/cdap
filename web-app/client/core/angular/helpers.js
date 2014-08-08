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

      number: function (value) {

        value = Math.abs(value);

        if (value > 1000000000) {
          var digits = 3 - (Math.round(value / 1000000000) + '').length;
          digits = digits < 0 ? 2 : digits;
          value = value / 1000000000;
          var rounded = Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);
          return [rounded, 'B'];

        } else if (value > 1000000) {
          var digits = 3 - (Math.round(value / 1000000) + '').length;
          digits = digits < 0 ? 2 : digits;
          value = value / 1000000;
          var rounded = Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);
          return [rounded, 'M'];

        } else if (value > 1000) {
          var digits = 3 - (Math.round(value / 1000) + '').length;
          digits = digits < 0 ? 2 : digits;
          value = value / 1000;
          var rounded = Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);
          return [rounded, 'K'];

        }

        var digits = 3 - (value + '').length;
        digits = digits < 0 ? 2 : digits;
        var rounded = Math.round(value * Math.pow(10, digits)) / Math.pow(10, digits);

        return [rounded, ''];

      },

      numberArrayToString: function(value) {
        return this.number(value).join('');
      },

      bytes: function (value) {

        if (value >= 1073741824) {
          value /= 1073741824;
          return [((Math.round(value * 100) / 100)), 'GB'];
        } else if (value >= 1048576) {
          value /= 1048576;
          return [((Math.round(value * 100) / 100)), 'MB'];
        } else if (value >= 1024) {
          value /= 1024;
          return [((Math.round(value * 10) / 10)), 'KB'];
        }

        return [value, 'B'];
      },

      /**
       * HAX. Transforms v2 json to v1 format for rendering flow status diagram.
       * TODO: Change frontend to accept v2 json and remove this code.
       */
      transformFlowModel: function (model) {
        var obj = {};
        var meta = {};
        if (model.hasOwnProperty('name')) {
          meta.name = model.name;
        }
        obj.meta = meta;
        var datasets = [];
        var flowlets = [];
        var flowletStreams = {};
        if (model.hasOwnProperty('flowlets')) {
          for (var descriptor in model.flowlets) {
            var flowlet = model.flowlets[descriptor];
            flowlets.push({
              name: flowlet.flowletSpec.name,
              classname: flowlet.flowletSpec.className,
              instances: flowlet.instances
            });

            if (!flowlet.datasets.length) {
              datasets.push.apply(datasets, flowlet.datasets);
            }

            var strObj = {};
            if (!$.isEmptyObject(flowlet.inputs)) {
              strObj['queue_IN'] = {
                second: 'IN'
              };
            }
            if (!$.isEmptyObject(flowlet.outputs)) {
              strObj['queue_OUT'] = {
                second: 'OUT'
              };
            }
            flowletStreams[descriptor] = strObj;
          }
        }
        obj.flowletStreams = flowletStreams;
        obj.datasets = datasets;
        obj.flowlets = flowlets;
        var connections = [];
        var flowStreams = [];
        model.connections = this.validateConnections(model.connections);
        for (var i = 0; i < model.connections.length; i++) {
          var cn = model.connections[i];
          var from = {};
          var to = {};
          from[cn.sourceType.toLowerCase()] = cn.sourceName;
          to['flowlet'] = cn.targetName;
          connections.push({
            from: from,
            to: to
          });
          if (cn.sourceType === 'STREAM') {
            flowStreams.push({
              name: cn.sourceName
            });
          }
        }
        obj.flowStreams = flowStreams;

        obj.connections = connections;
        obj.name = obj.meta.name;
        return obj;
      },

      /**
       * Validates connections and inserts a dummy node where there are overlapping flowlets.
       * @param  {Array} connections JSON received from server.
       * @return {Array} Validated connections with dummy nodes appropriately inserted.
       */
      validateConnections: function (connections) {
        var assignments = {};

        // First determine which order the nodes are rendered visually. This is based on a horizontal
        // column format.
        for (var i = 0, len = connections.length; i < len; i++) {
          var conn = connections[i];
          if (!(conn['sourceName'] in assignments)) {
            assignments[conn['sourceName']] = 0;
          }
          if (!(conn['targetName'] in assignments)) {
            assignments[conn['targetName']] = assignments[conn['sourceName']] + 1;
          }
        }

        // Determine if there are any anomolies i.e. nodelevel3 --> nodelevel3 and increment to
        // nodelevel3 --> nodelevel4.
        for (var i = 0, len = connections.length; i < len; i++) {
          var conn = connections[i];
          if (assignments[conn['sourceName']] === assignments[conn['targetName']]) {
            assignments[conn['targetName']]++;
          }
        }

        // Set up dummy connections if anomoly is detected and there is distance between connecting
        // nodes. This changes connection nodelevel2 --> nodelevel5 to:
        // nodelevel2 --> dummylevel3, dummylevel3 --> dummylevel4, dummylevel4 --> nodelevel5.
        var newConnections = [];
        for (var i = 0, len = connections.length; i < len; i++) {
          var source = connections[i].sourceName;
          var destination = connections[i].targetName;
          if (assignments[destination] - assignments[source] > 1) {
            var diff = assignments[destination] - assignments[source];
            for (var z = 0; z < diff; z++) {
              if (z === 0) {
                newConnections.push({
                  sourceType: connections[i].sourceType,
                  sourceName: connections[i].sourceName,
                  targetName: 'dummy'
                });
              } else if (z > 0 && z !== diff -1) {
                newConnections.push({
                  sourceType: 'FLOWLET',
                  sourceName: 'dummy',
                  targetName: 'dummy'
                });
              } else if (z === diff - 1) {
                newConnections.push({
                  sourceType: 'FLOWLET',
                  sourceName: 'dummy',
                  targetName: connections[i].targetName
                });
              }
            }
          } else {
            newConnections.push(connections[i]);
          }
        }
        return newConnections;
      },

      interrupt: function () {

        $('#drop-border').addClass('hidden');

        $('#drop-label').hide();
        $('#drop-loading').show();
        $('#drop-hover').show();

      },

      proceed: function (done) {

        $('#drop-hover').fadeOut(function () {

          $('#drop-border').removeClass('hidden');

          $('#drop-label').show();
          $('#drop-loading').hide();
          if (typeof done === 'function') {
            done();
          }
        });

      },

      generateUid: function () {
        return Math.random().toString(36).substr(2,9);
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

      getStorageEndpoint: function (entity) {
        switch(entity.type.toLowerCase()) {

          case 'app':
            return '/reactor/apps/' + entity.name + '/store.bytes?aggregate=true';

          case 'stream':
            return '/reactor/streams/' + entity.name + '/store.bytes?aggregate=true';

          case 'dataset':
            return '/reactor/datasets/' + entity.name + '/store.bytes?aggregate=true';

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

      getEventsProcessedEndpoint: function (entity, optionalAppName) {
        optionalAppName = optionalAppName ? optionalAppName : entity.app;
        switch(entity.type.toLowerCase()) {

          case 'app':
            return ('/reactor/apps/' + entity.name
              + '/process.events.processed?start=now-60s&end=now-0s&count=60');

          case 'flow':
            return ('/reactor/apps/' + optionalAppName + '/flows/' + entity.name
              + '/process.events.processed?start=now-60s&end=now-0s&count=60');

          default:
            break;
        }
      },



      getRequestRateEndpoint: function (entity, optionalAppName) {
        optionalAppName = optionalAppName ? optionalAppName : entity.app;
        switch(entity.type.toLowerCase()) {

          case 'procedure':
            return ('/reactor/apps/' + optionalAppName + '/procedures/' + entity.name + '/query.requests?start=now-60s&end=now-0s&count=60');

          default:
            break;
        }
      },

      getErrorRateEndpoint: function (entity, optionalAppName) {
        optionalAppName = optionalAppName ? optionalAppName : entity.app;
        switch(entity.type.toLowerCase()) {

          case 'procedure':
            return ('/reactor/apps/' + optionalAppName + '/procedures/' + entity.name + '/query.failures?start=now-60s&end=now-0s&count=60');

          default:
            break;
        }
      },

      getMappingStatusEndpoint: function (entity, optionalAppName) {
        optionalAppName = optionalAppName ? optionalAppName : entity.app;
        switch(entity.type.toLowerCase()) {

          case 'mapreduce':
            return ('/reactor/apps/' + optionalAppName + '/mapreduce/' + entity.name + '/mappers/process.completion?start=now-60s&end=now-0s&count=60');
            break;

          default:
            break;
        }
      },

      getReducingStatusEndpoint: function (entity, optionalAppName) {
        optionalAppName = optionalAppName ? optionalAppName : entity.app;
        switch(entity.type.toLowerCase()) {

          case 'mapreduce':
            return ('/reactor/apps/' + optionalAppName + '/mapreduce/' + entity.name + '/reducers/process.completion?start=now-60s&end=now-0s&count=60');
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

      getArrivalRateEndpoint: function (entity) {
        switch(entity.type.toLowerCase()) {
          case 'stream':
            return '/reactor/streams/' + entity.name + '/collect.events?start=now-60s&end=now-0s&count=60';

          default:
            break;
        }
      },

      getWriteRateEndpoint: function (entity) {
        switch(entity.type.toLowerCase()) {
          case 'dataset':
            return '/reactor/datasets/' + entity.name + '/dataset.store.bytes?start=now-60s&end=now-0s&count=60';
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