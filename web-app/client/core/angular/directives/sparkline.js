/*global define */

'use strict';

define(function() {

  var Directive = ['$interval', function($interval) {
  
    // needs its own data manager to just push data to while the chart updates by the second.
    // Data manager should remove duplicated, add times based on a timestamp value format.
    // call to update data should be all that is necessary with new data.
    // 
    var AnimatedSparkline = Class.create({

      initialize: function (elm, data, width, height, interpolation, animate, updateDelay, transitionDelay, percent, shade) {
          var self = this;


          this.elm = elm;
          this.data = data || [];
          this.width = width || 300;
          this.height = height || 30;
          this.interpolation = interpolation || "basis";
          this.animate = animate || true;
          this.updateDelay = updateDelay || 1000;
          this.transitionDelay = transitionDelay || 1000;
          this.isPercent = percent || false;
          this.isShade = shade || false;

          this.updateData(data);

          var dataLength = this.data.length;
          var yBuffer = 0.0;
          var extend = Math.round(this.width / dataLength);
          var margin = 5;
          var max = d3.max(this.data) || 100;
          var min = d3.min(this.data) || 0;

          // create an SVG element inside the #graph div that fills 100% of the div
          this.graph = d3.select(elm)
            .append("svg:svg")
            .attr("width", "100%")
            .attr("height", "100%")
            .attr('preserveAspectRatio', 'none');

          // X scale will fit values from 0-10 within pixels 0-100
          // starting point is -5 so the first value doesn't show and slides off the edge as part of the transition
          this.x = d3.scale.linear().domain([0, dataLength]).range([0 - extend, width - extend]);
          // Y scale will fit values from 0-10 within pixels 0-100
          this.y = d3.scale.linear()
              .domain([max + (max * yBuffer), min - (min * yBuffer)])
              .range([margin, height - margin]);

          // create a line object that represents the SVN line we're creating
          this.line = d3.svg.line().interpolate("monotone")
            // assign the X function to plot our line as we wish
            .x(function(d,i) { 
              // verbose logging to show what's actually being done
              //console.log('Plotting X value for data point: ' + d + ' using index: ' + i + ' to be at: ' + x(i) + ' using our xScale.');
              // return the X coordinate where we want to plot this datapoint
              return self.x(i); 
            })
            .y(function(d) { 
              // verbose logging to show what's actually being done
              //console.log('Plotting Y value for data point: ' + d + ' to be at: ' + y(d) + " using our yScale.");
              // return the Y coordinate where we want to plot this datapoint
              return self.y(d); 
            })
            .interpolate(interpolation);
        
          // display the line by appending an svg:path element with the data line we created above
          this.g = this.graph.append("svg:g");
          this.g.append("svg:path").attr('class', 'sparkline-data').attr("d", this.line(this.data));
          // or it can be done like this
          //graph.selectAll("path").data([data]).enter().append("svg:path").attr("d", line);

          this.tDelay = transitionDelay;

          if (true) {
            self. interval = setInterval(function () {
              self.redrawWithAnimation();
            }, this.tDelay);
          }
      },

      redrawWithAnimation: function () {
        // update with animation
        this.graph.selectAll("path.sparkline-data")
          .data([this.data]) // set the new data
          .attr("transform", "translate(" + this.x(1) + ")") // set the transform to the right by x(1) pixels (6 for the scale we've set) to hide the new value
          .attr("d", this.line) // apply the new data values ... but the new value is hidden at this point off the right of the canvas
          .transition() // start a transition to bring the new value into view
          .ease("linear")
          .duration(this.tDelay) // for this demo we want a continual slide so set this to the same as the setInterval amount below
          .attr("transform", "translate(" + this.x(0) + ")"); // animate a slide to the left back to x(0) pixels to reveal the new value
          
          /* thanks to 'barrym' for examples of transform: https://gist.github.com/1137131 */        
      },

      updateData: function (newData) {
        var formattedData = newData.map(function (entry) {
          return entry.value;
        });
        var arr = [];
        for (var i = 0, l = 60; i < l; i++) {
            arr.push(Math.round(Math.random() * l))
        }
        this.data = arr;
      },


      destroy: function () {
        clearInterval(this.interval);
      }


    });

    return {
      
      restrict: 'AE',
      scope: {
        data: "="
      },
      template: '<div class="sparkline-list-container"></div>',
      link: function (scope, elm, attrs) {
        var ts = new Date().getTime();
        var data = Array.apply(null, new Array(61)).map(
          Object.prototype.valueOf, {
            time: ++ts,
            value: 0
          }
        );
        var animatedSparkline = new AnimatedSparkline(
          elm[0], data, 190, 38, "basis", true, 1000, 1000, false, false);
        
        scope.$watch('data', function (newVal, oldVal) {
          if (newVal && angular.isArray(newVal) && newVal.length) {
            animatedSparkline.updateData(newVal);
          }
        });


        scope.$on('$destroy', function() {
          animatedSparkline.destroy();
        });
      }
    }
  
  }];

  return Directive;

});