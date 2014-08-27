'use strict'
/**
	Dashboard Progress Bar
	wraps the ui-bootstrap progress bar
**/
angular
    .module('dashboard.components.utilities')

    .directive('dashboardProgressBar', ['ApiService', function (ApiService) {
    	return {
    		scope: {
    			endpoint: '@',
                class: '@'
    		},
    		templateUrl: '/components/utilities/progress-bar/template.html',

    		link: function (scope, el, attrs) {                
                
                // the value for the progressbar
                scope.value = 0;
                //
                // here is our api callback
                function callback (response) {                    
                    scope.value = parseInt(response.value);
                    // console.log('progress bar data', response, scope);
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