
'use strict';

define(['helpers', 'plumber'], function (helpers, Plumber) {

  var Directive = ['$interval', '$timeout', '$compile', '$state', 'Flowlet', 'actionService', 'statusService', 'POLLING_INTERVAL',
    function($interval, $timeout, $compile, $state, Flowlet, actionService, statusService, POLLING_INTERVAL) {

    var COLUMN_WIDTH = 226;

    var visualizer = {

      init: function (flow) {
        var self = this;
        this.flow = flow;
        this.cols = [];
        this.maxNodes = 0;

        this.assignCols();
        // Wait for render.
        $timeout(function () {
          self.connectElements();  
        }, 200);
        
      },

      assignCols: function () {
        for (var entry in this.flow.assignments) {
          if (this.flow.assignments.hasOwnProperty(entry)) {
            var val = this.flow.assignments[entry];
            if (typeof this.cols[val] === 'undefined') {
              this.cols[val] = [this.getElement(entry)];
            } else {
              this.cols[val].push(this.getElement(entry));
            }
            // Set maxiumum nodes in column.
            if (this.cols[val].length > this.maxNodes) {
              this.maxNodes = this.cols[val].length;
            }
          }
        }
      },

      getElement: function (entry) {
        // Flow
        for (var i = 0; i < this.flow.elements.flowlet.length; i++) {
          var flowlet = this.flow.elements.flowlet[i]
          if (flowlet.name == entry) {
            return flowlet;
          }
        }

        //Stream
        for (var i = 0; i < this.flow.elements.stream.length; i++) {
          var stream = this.flow.elements.stream[i]
          if (stream.name == entry) {
            return stream;
          }
        }

        //Dummy
        return new Flowlet({
          name: entry,
          type: 'dummy'
        });
      },

      connectElements: function () {
        var self = this;
        var connector = [ "Bezier", { gap: 0, curviness: 70 } ];
        for (var i = 0; i < this.flow.connections.length; i++) {
          var from = 'flowlet' + this.flow.connections[i].sourceName;
          var to = 'flowlet' + this.flow.connections[i].targetName;
          if (this.numRows(this.flow.connections[i].sourceName) 
            !== this.numRows(this.flow.connections[i].targetName)) {
            Plumber.connect(from, to, '#7F7F7F', connector);
          } else {
            Plumber.connect(from, to, '#7F7F7F');
          }
          
        }
      },

      numRows: function (elementName) {
        for (var i = 0; i < this.cols.length; i++) {
          for (var j = 0; j < this.cols[i].length; j++) {
            if (this.cols[i][j].name === elementName) {
              return this.cols[i].length;
            }
          }
        }
        return 0;
      }




    };

    return {
      restrict: 'AE',
      replace: true,
      templateUrl: '/templates/directives/flowviz.html',
      scope: {
        data: '='
      },
      link: function (scope, elm, attrs) {
        // An array of arrays for maintaining columns. Each entry in inner array must be a flow
        // element.
        scope.cols = [];

        scope.$watch('data', function (newVal, oldVal) {
          if (newVal && angular.isObject(newVal) && Object.keys(newVal).length) {
            visualizer.init(newVal);
            scope.visualizer = visualizer;
            $('.flowviz-renderer').css('width', COLUMN_WIDTH * scope.visualizer.cols.length);
          }
        });

      
        scope.$on('$destroy', function() {
          if (typeof ival !== 'undefined') {
            $interval.cancel(ival);
          }
        });
      }
    }

  }];

  return Directive;

});