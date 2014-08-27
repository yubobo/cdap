'use strict';

define(['angular'], function (angular) {
return ['$http', 'SettingsService', function ($http, SettingsService) {

		var endpoints = [];
		var apiIntervalTime = SettingsService.bind('api_call_interval', function (val) {
			apiIntervalTime = val;
		});
		var failedRequestLimit = SettingsService.bind('api_failed_request_limit', function (val) {
			failedRequestLimit = val;
		});
		var running = false;

		function run () {
			if (running) { return; }
			running = true;
			callEndpoints();
		}
		function stop () {
			running = false;
		}
		function callEndpoints () {
			if (!running || !endpoints.length) { return false; }
			angular.forEach(endpoints, _callEndpoint);
		}
		function _callEndpoint (endpoint) {
			if (!running || (endpoint.numFailedRequests >= failedRequestLimit)) {
				return false;
			}
			if (endpoint.listeners && endpoint.listeners.length) {
				$http.get(endpoint.url)
					.success(function (response) {
						if (endpoint.listeners && endpoint.listeners.length) {
							angular.forEach(endpoint.listeners, function (listener) {
								listener.callback.call(this, response);
							});
							setTimeout(function () {
								_callEndpoint(endpoint);
							}, apiIntervalTime);
						}
					})
					.error(function () {
						console.error('error calling endpoint', endpoint);
						endpoint.numFailedRequests++;
						if (endpoint.numFailedRequests >= failedRequestLimit) {
							console.error('endpoint failed', endpoint);
						}
					});
			}
		}
		function addEndpoint (url, callback) {
			var match = false;
			var e;
			var registration = Math.random().toString(36).replace(/[^a-z]+/g, '');
			for (var i=0; i<endpoints.length; i++) {
				if (endpoints[i].url === url) {
					e = endpoints[i];
					e.listeners.push({ registration: registration, callback: callback });
					match = true;
					break;
				}
			}
			if (!match) {
				e = {
					url: url,
					numFailedRequests: 0,
					listeners: [{ registration: registration, callback: callback }]
				};
				endpoints.push(e);
				// if the service is already running, and this is a new endpoint, run this endpoint url immediately
				if (running) {
					_callEndpoint(e);
				}
			}			
			return registration;
		}
		function removeEndpoint (url, callbackRegistration) {
			for (var i=0; i<endpoints.length; i++) {
				if (endpoints[i].url === url) {
					if (arguments.length < 2) { // no specific callback was supplied, so remove the whole endpoint
						endpoints.splice(i,1);
					} else { // find the specific endpoint to remove
						for (var k=0, numListeners=endpoints[i].listeners.length; k<numListeners; k++) {
							var l = endpoints[i].listeners[k].registration;
							if (l === callbackRegistration) {
								endpoints[i].listeners.splice(k,1);

								// if there are no listeners left, remove the endpoint completely
								if (endpoints[i].listeners.length === 0) {
									endpoints.splice(i,1);
								}
								return this;
							}
						}
					}
				}
			}
			return this;
		}
		function removeAllEndpoints () {
			angular.forEach(endpoints, function (endpoint) {
				endpoint.listeners = [];
			});
			endpoints = [];
		}

		function callImmediately (url) {
			return $http.get(url);
		}

		function getEndpoint (url) {
			for (var i=0; i<endpoints.length; i++) {
				if (endpoints[i].url === url) {
					return endpoints[i];
				}
			}
			return false;
		}

		return {
			run: run,
			stop: stop,
			addEndpoint: addEndpoint,
			removeEndpoint: removeEndpoint,
			removeAllEndpoints: removeAllEndpoints,
			callImmediately: callImmediately,
			getAllEndpoints: function () {
				return endpoints;
			},
			getEndpoint: getEndpoint
		};
		
	}];
});