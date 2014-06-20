/*global define */

'use strict';

define(function() {

  var Directive = [function() {
  
    return {
      
      restrict: 'AE',
      template: '<div class="sparkline-list-container">',
      link: function(scope, elm, attrs) {
        scope.$watch('collect', function (newVal, oldVal) {
          if (angular.isArray(newVal.data) && newVal.data.length) {
            console.log(newVal);
          }
        });
        var percent = null;
        var shade = null;
        var data = [];
        var widget = jQuery(elm[0]);
        var w = 300;
        var h = 150;
        var allData = [], length = 0;
        var max = d3.max(allData) || 9;
        var min = d3.min(allData) || -1;
        var extend = Math.round(w / data.length);

        var margin = 5;
        var yBuffer = 0.0;
        var y, x;

        x = d3.scale.linear();//.domain([0, data.length]).range([0, w]);
        y = d3.scale.linear();

        var vis = widget
          .append("svg:svg")
          .attr('width', '100%')
          .attr('height', '100%')
          .attr('preserveAspectRatio', 'none');

        var g = vis.append("svg:g");
        var line = d3.svg.line().interpolate("monotone")
          .x(function(d,i) { return x(i); })
          .y(function(d) { return y(d); });

        if (percent || shade) {
          var area = d3.svg.area()
            .x(line.x())
            .y1(line.y())
            .y0(y(0));
          g.append("svg:path").attr('class', 'sparkline-area').attr("d", area(data));
        }

        g.append("svg:path").attr('class', 'sparkline-data').attr("d", line(data));

        return {
          g: g,
          percent: percent,
          shade: shade,
          update: function (name, data) {

            this.series[name] = data;

            var allData = [], length = 0;
            for (var i in this.series) {
              allData = allData.concat(this.series[i]);
              if (this.series[i].length > length) {
                length = this.series[i].length;
              }
            }
            var max = d3.max(allData) || 100;
            var min = d3.min(allData) || 0;
            var extend = Math.round(w / data.length);

            var yBuffer = 0.0;
            var y, x;

            x = d3.scale.linear().domain([0, length]).range([0 - extend, w - extend]);

            if (this.percent) {
              y = d3.scale.linear()
                .domain([100, 0])
                .range([margin, h - margin]);
            } else {
              if ((max - min) === 0) {
                if (data[0]) {
                  max = data[0] + data[0] * 0.1;
                  min = data[0] - data[0] * 0.1;
                } else {
                  max = 10;
                  min = 0;
                }
              }
              y = d3.scale.linear()
                .domain([max + (max * yBuffer), min - (min * yBuffer)])
                .range([margin, h - margin]);
            }


            var line = d3.svg.line().interpolate("monotone")
              .x(function(d,i) { return x(i); })
              .y(function(d) { return y(d); });

            if (this.percent || this.shade) {
              var area = d3.svg.area().interpolate("monotone")
                .x(line.x())
                .y1(line.y())
                .y0(y(-100));

              this.g.selectAll("path.sparkline-area")
                .data([data])
                .attr("transform", "translate(" + x(0) + ")")
                .attr("d", area)
                .transition()
                .ease("linear")
                .duration(C.POLLING_INTERVAL)
                .attr("transform", "translate(" + x(-(C.POLLING_INTERVAL / 1000)) + ")");
            }

            this.g.selectAll("path.sparkline-data")
              .data([data])
              .attr("transform", "translate(" + x(0) + ")")
              .attr("d", line)
              .transition()
              .ease("linear")
              .duration(C.POLLING_INTERVAL)
              .attr("transform", "translate(" + x(-(C.POLLING_INTERVAL / 1000)) + ")");

          }
        };
      }
    
    }
  
  }];

  return Directive;

});