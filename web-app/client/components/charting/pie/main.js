/**
	Reactor Progress Bar
	wraps the ui-bootstrap progress bar
**/
angular
    .module('dashboard.components.charting')

    .directive('dashboardPieChart', ['ApiService', function (ApiService) {
    	return {
    		scope: {
    			endpoint: '@',
                class: '@'
    		},

            templateUrl: '/components/charting/pie/template.html',

    		link: function (scope, el, attrs) {
                scope.config = {
                    autorefresh: true,
                    refreshDataOnly: false
                }
                scope.options = {
                    chart: {
                        type: 'pieChart',
                        height: 350,
                        x: function(d){return d.label;},
                        y: function(d){return d.value;},
                        transitionDuration: 500
                    }
                };

                scope.data = [];

                var firstRender = true;

                //
                // here is our api callback
                function callback (response) {
                    scope.$evalAsync(function () {
                        scope.data = response.value;
                        // this doesn't seem to work with pie charts
                        
                        if (firstRender) {
                            scope.config.refreshDataOnly = false;
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