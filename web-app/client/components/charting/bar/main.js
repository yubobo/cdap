/**

**/
angular
    .module('dashboard.components.charting')

    .directive('dashboardBarChart', ['ApiService', function (ApiService) {
    	return {
    		scope: {
    			endpoint: '@',
                class: '@'
    		},

            templateUrl: '/components/charting/bar/template.html',

    		link: function (scope, el, attrs) {
                scope.config = {
                    autorefresh: true,
                    refreshDataOnly: false
                };
                scope.options = {
                    chart: {
                        type: 'discreteBarChart',
                        height: 350,
                        margin : {
                            top: 20,
                            right: 20,
                            bottom: 60,
                            left: 55
                        },
                        x: function(d){ return d.label; },
                        y: function(d){ return d.value; },
                        showValues: true,
                        valueFormat: function(d){
                            return d3.format(',.4f')(d);
                        },
                        transitionDuration: 500,
                        xAxis: {
                            axisLabel: 'X Axis'
                        },
                        yAxis: {
                            axisLabel: 'Y Axis',
                            axisLabelDistance: 30
                        }
                    }
                };

                scope.data = [ { values: [] } ];

                var firstRender = true;

                //
                // here is our api callback
                function callback (response) {
                    scope.$evalAsync(function () {
                        scope.data[0].values = response.value;
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