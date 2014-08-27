'use strict'

angular
	.module('dashboard.components.custom')

	.directive('testComponent', ['ApiService', function (ApiService) {
		return {
			scope: {
				endpoint: '@'
			},
			// template: 
			// 	'<div reactor-progress-bar="" class="progress-striped" endpoint="{{ progressEndpoint }}"></div>' + 
			// 	'<div reactor-ternary="" endpoint="{{ ternaryEndpoint }}"></div>',
			templateUrl: '/components/custom/test/template.html',
			link: function (scope, el, attrs) {
				scope.finalOut = 'waiting';
				//scope.progressEndpoint = 'http://localhost:8080/api/progress';
				//scope.ternaryEndpoint = 'http://localhost:8080/api/ternary';
				function callback (response) {
					scope.finalOut = response.value * 10;
				}
				ApiService.addEndpoint('http://localhost:8080/api/ternary',callback);
				ApiService.addEndpoint('http://localhost:9998/rest/procedures/', function (data) {
					scope.procedures = data;

				});
			}
		}
	}]);