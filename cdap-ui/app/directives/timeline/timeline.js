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

  var margin = { top: 20, right: 30, bottom: 30, left: 40 };

  var svgWidth = 500;
  var svgHeight = 400;

  var width = svgWidth - margin.left - margin.right,
      barHeight = 20;

  var svg = d3.select('svg.timeline-svg')
    .attr('width', svgWidth)
    .attr('height', svgHeight);

  var x = d3.scale.linear()
    .domain([d3.min(scope.model, function (d) { return d.start; }) , d3.max(scope.model, function (d) { return d.end; })])
    .range([0, width]);

  var chart = svg.append('g')
    .attr('transform', 'translate(' + margin.left + ', ' + margin.top + ')');

  var bar = chart.selectAll('g')
    .data(scope.model)
    .enter()
    .append('g')
    .attr('transform', function (d, i) { return 'translate(0, ' + i * barHeight + ')'; });

  bar.append('rect')
    .attr('width', function (d) { return x(d.end) - x(d.start); })
    .attr('height', barHeight - 1)
    .attr('x', function (d) { return x(d.start); })
    .attr('fill', '#e74c3c');

  // Adding Axis
  var xAxis = d3.svg.axis()
    .scale(x)
    .tickFormat(function (d) {

      var date = new Date(d);
      console.log('date', date);
      return date.getSeconds() + '.' + date.getMilliseconds();
     })
    .orient('bottom');

  var height = (scope.model.length * barHeight);

  chart.append('g')
    .attr('class', 'axis')
    .attr('transform', 'translate(0,' + height  + ')' )
    .call(xAxis);


  var y = d3.scale.ordinal()
    .domain(d3.range(0, scope.model.length))
    .rangeRoundBands([0, height]);

  var yAxis = d3.svg.axis()
    .scale(y)
    .tickFormat(function (d) { return scope.model[d].name; })
    .orient('left');

  chart.append('g')
    .attr('class', 'axis')
    .call(yAxis);

}