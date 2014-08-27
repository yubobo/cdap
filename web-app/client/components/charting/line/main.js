/**
	Reactor Progress Bar
	wraps the ui-bootstrap progress bar
**/
angular
    .module('dashboard.components.charting')

    .directive('dashboardLineChart', ['ApiService', function (ApiService) {
    	return {
    		scope: {
    			endpoint: '@',
                class: '@'
    		},

            templateUrl: '/components/charting/line/template.html',

    		link: function (scope, el, attrs) {
                console.log('initializing line chart');
                scope.config = {
                    autorefresh: true,
                    refreshDataOnly: false
                }
                scope.options = {
                    chart: {
                        type: 'lineChart',
                        height: 200,
                        margin : {
                            top: 20,
                            right: 20,
                            bottom: 40,
                            left: 55
                        },
                        x: function(d){ return d.x; },
                        y: function(d){ return d.y; },
                        useInteractiveGuideline: true,
                        transitionDuration:500,    
                        yAxis: {
                            tickFormat: function(d){
                               return d3.format('.01f')(d);
                            }
                        },
                        yDomain: [-1,1]
                    }
                };

                scope.data = [ { values: [] } ];

                var firstRender = true;
                var maxHorizontalValues = 20;

                //
                // here is our api callback
                function callback (response) {
                    scope.$evalAsync(function () {
                        scope.data[0].values.push(response.value);
                        var yval = response.value.y;
                        if (yval < scope.options.chart.yDomain[0]) {
                            scope.options.chart.yDomain[0] = yval;
                        } else if (yval > scope.options.chart.yDomain[1]) {
                            scope.options.chart.yDomain[1] = yval;
                        }
                        if (scope.data[0].values.length > maxHorizontalValues) scope.data[0].values.shift();
                        
                        if (firstRender) {
                            scope.config.refreshDataOnly = true;
                            firstRender = false;
                        }
                        
                    });
                };
                //
                // register the endpoint with apiservice
                var callbackRegistration = ApiService.addEndpoint(scope.endpoint, callback);
                ApiService.run();
                //
                // watch for changes in the endpoint url
                scope.$watch('endpoint', function (newVal, oldVal) {
                    if (newVal){
                        ApiService.removeEndpoint(oldVal, callbackRegistration);
                        callbackRegistration = ApiService.addEndpoint(newVal, callback);
                    }
                });
                //
                // when this directive is removed from the DOM, also remove its endpoint/callback
                scope.$on('$destroy', function() {
                    ApiService.removeEndpoint(scope.endpoint, callbackRegistration);
                });

    		}
    	}
    }]);