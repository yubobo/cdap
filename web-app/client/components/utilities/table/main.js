/**
	Dashboard Data Table
    wraps Angular UI Guid - http://angular-ui.github.io/ng-grid/
**/
angular
    .module('dashboard.components.utilities')

    .directive('dashboardTable', ['ApiService','$filter', function (ApiService, $filter) {
    	return {
    		scope: {
    			endpoint: '@',
                class: '@'
    		},
            templateUrl: '/components/utilities/table/template.html',

    		link: function (scope, el, attrs) {                
                
                scope.data = [];
                scope.sortBy = '';
                scope.sortOrder = 'asc';
                var firstTime = true;

                scope.setSortBy = function (params) {
                    var which = params.col;
                    if (scope.sortBy === which) {
                        switch (scope.sortOrder) {
                            case 'asc':
                                scope.sortOrder = 'desc';
                                break;
                            default:
                                scope.sortOrder = 'asc';
                                break;
                        }
                    } else {
                        scope.sortOrder = 'asc';
                    }
                    scope.sortBy = which;
                    sortData();
                }

                function sortData (optionalData) {
                    if (!optionalData) {
                        optionalData = angular.copy(scope.data);
                    }
                    var sortedData = (scope.sortBy != '') ? $filter('orderBy')(optionalData, scope.sortBy) : optionalData;
                    if (scope.sortOrder === 'desc') {
                        sortedData = sortedData.reverse();
                    }
                    scope.$evalAsync(function () {
                        scope.data = sortedData.slice(0,10);
                    });
                }

                // here is our api callback
                function callback (response) {
                    sortData(response.value);
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