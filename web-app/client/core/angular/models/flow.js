'use strict';

define(['helpers'], function (helpers) {

  var Model = ['Flowlet', 'Stream', function (Flowlet, Stream) {
    return Class.create({
      initialize: function (data) {
        this.app = '';
        this.id = '';
        this.name = '';
        this.description = '';
        this.type = 'Flow';
        this.datasets = [];
        this.flowlets = {};
        this.flowStreams = [];
        this.flowletStreams = [];
        this.connections = [];
        this.elements = {
          flowlet: [],
          stream: []
        };

        // Convenience map for easily looking up flowlets.
        this._elementMap = {
          flowlet: [],
          stream: []
        };

        if (data && Object.prototype.toString.call(data) === '[object Object]') {
          for (var index in data) {
            this[index] = data[index];
          }
        }
        this.transformFlowModel();
        this.processElements();

      },
      
      /**
       * HAX. Transforms v2 json to v1 format for rendering flow status diagram.
       * TODO: Change frontend to accept v2 json and remove this code.
       */
      transformFlowModel: function () {
        var meta = {};
        if (this.hasOwnProperty('name')) {
          meta.name = this.name;
        }
        this.meta = meta;
        var datasets = [];
        var flowlets = [];
        var flowletStreams = {};
        if (this.hasOwnProperty('flowlets')) {
          for (var descriptor in this.flowlets) {
            var flowlet = this.flowlets[descriptor];
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
        this.flowletStreams = flowletStreams;
        this.datasets = datasets;
        this.flowlets = flowlets;
        var connections = [];
        var flowStreams = [];

        // First get current assignments without dummy nodes.
        this.assignments = this.getAssignments(this.connections);

        // Assign dummy connections.
        this.connections = this.validateConnections(this.connections);

        // Get assignments again including dummy nodes.
        this.assignments = this.getAssignments(this.connections);

        for (var i = 0; i < this.connections.length; i++) {
          var cn = this.connections[i];
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
        this.flowStreams = flowStreams;

        this.plumbLinks = connections;
        this.name = this.meta.name;

        // Assign name to flowlet streams which don't come with a default name.
        if (this.hasOwnProperty('flowletStreams')) {
          var flowletStreamArr = [];
          for (var entry in this['flowletStreams']) {
            this['flowletStreams'][entry]['name'] = entry;
            flowletStreamArr.push(this['flowletStreams'][entry]);
          }
          this['flowletStreams'] = flowletStreamArr;
        }
      },

      getAssignments: function (connections) {
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
        return assignments;
      },

      /**
       * Validates connections and inserts a dummy node where there are overlapping flowlets.
       * @param  {Array} connections JSON received from server.
       * @return {Array} Validated connections with dummy nodes appropriately inserted.
       */
      validateConnections: function (connections) {

        // Set up dummy connections if anomoly is detected and there is distance between connecting
        // nodes. This changes connection nodelevel2 --> nodelevel5 to:
        // nodelevel2 --> dummylevel3, dummylevel3 --> dummylevel4, dummylevel4 --> nodelevel5.
        var newConnections = [];
        for (var i = 0, len = connections.length; i < len; i++) {
          var source = connections[i].sourceName;
          var destination = connections[i].targetName;
          if (this.assignments[destination] - this.assignments[source] > 1) {
            var diff = this.assignments[destination] - this.assignments[source];
            for (var z = 0; z < diff; z++) {
              if (z === 0) {
                newConnections.push({
                  sourceType: connections[i].sourceType,
                  sourceName: connections[i].sourceName,
                  targetName: 'dummy' + z
                });
              } else if (z > 0 && z !== diff -1) {
                newConnections.push({
                  sourceType: 'FLOWLET',
                  sourceName: 'dummy' + (z - 1),
                  targetName: 'dummy' + z
                });
              } else if (z === diff - 1) {
                newConnections.push({
                  sourceType: 'FLOWLET',
                  sourceName: 'dummy' + (z - 1),
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

      processElements: function () {
        for (var i = 0; i < this.flowlets.length; i++) {
          this.flowlets[i].app = this.app;
          this.flowlets[i].flow = this.name;
          this.elements.flowlet.push(new Flowlet(this.flowlets[i]));
          this._elementMap.flowlet[this.flowlets[i].name] = i;
        }
        for (var i = 0; i < this.flowStreams.length; i++) {
          this.flowStreams[i].app = this.app;
          this.flowStreams[i].flow = this.name;
          this.elements.stream.push(new Stream(this.flowStreams[i]));
          this._elementMap.stream[this.flowStreams[i].name] = i;
        }
      },

      getElement: function (name) {
        if (!this._elementMap.flowlet.hasOwnProperty(name) 
              && !this._elementMap.stream.hasOwnProperty(name)) {
          throw "Element with name: " + name + " not found";
        }
        return (this._elementMap.flowlet.hasOwnProperty(name) 
          ? this.elements.flowlet[this._elementMap.flowlet[name]] 
          : this.elements.stream[this._elementMap.stream[name]]);
      },

      getBusynessEndpoint: function () {
        return helpers.getBusynessEndpoint(this);
      },

      getEventsProcessedEndpoint: function () {
        return helpers.getEventsProcessedEndpoint(this);
      },

      getStatusEndpoint: function () {
        return helpers.getStatusEndpoint(this);
      },

      getStartEndpoint: function () {
        return helpers.getStartEndpoint(this);
      },

      getStopEndpoint: function () {
        return helpers.getStopEndpoint(this);
      }


    });
  }];

  return Model;

});