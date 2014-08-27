/**
	Dashboard Ternary components - displays one of 3 values
**/
angular
    .module('dashboard.components.utilities')

    .directive('dashboardTernary', ['ApiService', function (ApiService) {
    	
        return {
    		scope: {
    			endpoint: '@'
    		},
            
            templateUrl: '/components/utilities/ternary/template.html',

    		link: function (scope, el, attrs) {
                
                // the actual value that determines ternary state
                scope.value = 0;
                //
                // here is our api callback
                function callback (response) {
                    scope.value = response.value;
                };
                //
                // register the endpoint with apiservice
                var callbackRegistration = '';
                ApiService.run();
                //
                // watch for changes in the endpoint url
                scope.$watch('endpoint', function (newVal, oldVal) {
                    if (newVal && newVal != '') {
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