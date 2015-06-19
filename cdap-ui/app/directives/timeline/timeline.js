angular.module(PKG.name + '.commons')
  .directive('timeline', function() {
    return {
      restrict: 'E',
      scope: {
        model: '='
      },
      templateUrl: 'timeline/timeline.html',
      link: timeline
    };
  });

function timeline(scope, elem, attr) {

  function render(dataset) {

    if (!dataset.length) { return; }

    var maxLength = Math.max.apply(null, dataset.map(function (d) { return measure(d.name); }));

    var margin = { top: 20, right: 30, bottom: 30, left: maxLength + 10 };

    var svgWidth = 1170;
    var svgHeight = 400;

    var width = svgWidth - margin.left - margin.right,
        barHeight = 20;

    var svg = d3.select('svg.timeline-svg')
      .attr('width', svgWidth)
      .attr('height', svgHeight);

    var tip = d3.tip()
      .attr('class', 'd3-tip')
      .offset([-10, 0])
      .html(function (d) {
        return constructTip(d);
      });

    svg.call(tip);

    var height = (dataset.length * barHeight);

    var x = d3.time.scale()
      .domain([d3.min(dataset, function (d) { return new Date(d.start); }) , d3.max(dataset, function (d) { return new Date(d.end); })])
      .range([0, width]);

    var y = d3.scale.ordinal()
      .domain(d3.range(0, dataset.length))
      .rangeRoundBands([0, height]);

    var chart = svg.append('g')
      .attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')');

    var bar = chart.selectAll('g')
      .data(dataset)
      .enter()
      .append('g')
      .attr('transform', function (d, i) { return 'translate(0, ' + i * barHeight + ')'; });


    var color = d3.scale.category20();

    bar.append('rect')
      .attr('width', function (d) { return x(d.end) - x(d.start); })
      .attr('height', barHeight - 4)
      .attr('x', function (d) { return x(d.start); })
      .attr('y', function () { return 2; })
      .attr('fill', function (d, i) { return color(i); })
      .on('mouseover', tip.show)
      .on('mouseout', tip.hide);

    // Adding Axis
    var xAxis = d3.svg.axis()
      .scale(x)
      .ticks(6)
      .orient('bottom');

    chart.append('g')
      .attr('class', 'axis')
      .attr('transform', 'translate(0,' + height + ')' )
      .call(xAxis);


    var yAxis = d3.svg.axis()
      .scale(y)
      .tickFormat(function (d) { return dataset[d].name; })
      .orient('left');

    chart.append('g')
      .attr('class', 'axis')
      .call(yAxis);


  }

  scope.$watchCollection('model', function() {

    if (!Object.keys(scope.model).length) { return; }

    d3.select('svg.timeline-svg').selectAll('g').remove();

    render(formatData());

  });

  function formatData() {
    var dataset = [];
    angular.forEach(scope.model, function (v, k) {
      dataset.push({
        name: k,
        start: v.start,
        end: v.end || new Date()
      });
    });

    return dataset;
  }

  function constructTip(d) {

    var html = '';
    html += '<p class="tooltip-heading text-center">' + d.name + '</p><hr/>';
    html += '<strong>Start: </strong>' + moment(d.start).format('LLL') + '<br />';
    html += '<strong>End: </strong>' + moment(d.end).format('LLL');

    return html;
  }

  function measure(text) {
    /**
      * Adapted from: http://stackoverflow.com/a/25467363
     **/

    // Create a dummy canvas (render invisible with css)
    var c=document.createElement('canvas');
    // Get the context of the dummy canvas
    var ctx=c.getContext('2d');
    // Set the context.font to the font that you are using
    ctx.font = '13px ' + 'Helvetica';
    // Measure the string
    var length = ctx.measureText(text).width;

    // Return width

    return length;
  }

}