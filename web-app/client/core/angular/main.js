/*global require, requirejs */

'use strict';

requirejs.config({
  paths: {
    'angular': ['/third_party/angular-1.2.16/angular'],
    'angular-route': ['/third_party/angular-1.2.16/angular-route'],
    'angular-sanitize': ['/third_party/angular-1.2.16/angular-sanitize'],
    'jQuery': ['/third_party/jquery-1.11.1.min'],
    'bootstrap': ['/third_party/bootstrap/bootstrap.min']
  },
  shim: {
    'angular': {
      exports : 'angular'
    },
    'angular-route': {
      deps: ['angular'],
      exports : 'angular'
    },
    'bootstrap': {
      deps: ['jQuery']
    }
  }
});

require([
  'angular',
  './controllers/overview',
  './controllers/resources',
  'angular-route',
  'jQuery',
  'bootstrap'],
  function (angular, OverviewCtrl, ResourcesCtrl) {

    // Declare app level module which depends on filters, and services

    angular.module('ReactorWebapp', ['ngRoute'])
    .config(['$routeProvider', function ($routeProvider) {
      
      $routeProvider.when('/overview', {
        templateUrl: '/partials/overview.html', controller: OverviewCtrl
      });
      
      $routeProvider.when('/resources', {
        templateUrl: '/partials/resources.html', controller: ResourcesCtrl
      });
      
      $routeProvider.otherwise({redirectTo: '/overview'});
    
    }]);

    angular.bootstrap(document, ['ReactorWebapp']);

});