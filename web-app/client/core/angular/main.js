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
  './controllers/apps',
  './controllers/flows',
  './controllers/datasets',
  './controllers/procedures',
  './controllers/streams',
  './controllers/app',
  './controllers/flow',
  './controllers/stream',
  './controllers/procedure',
  './controllers/dataset',
  './controllers/loading',
  './controllers/login',
  './controllers/pagenotfound',
  './controllers/connectionerror',
  './controllers/analyze',
  './controllers/services',
  './controllers/service',
  'angular-route',
  'jQuery',
  'bootstrap'],
  function (angular, OverviewCtrl, ResourcesCtrl, AppsCtrl, FlowsCtrl, DatasetsCtrl,
    ProceduresCtrl, StreamsCtrl, AppCtrl, FlowCtrl, StreamCtrl, ProcedureCtrl, DatasetCtrl,
    LoadingCtrl, LoginCtrl, PageNotFoundCtrl, ConnectionErrorCtrl, AnalyzeCtrl, ServicesCtrl,
    ServiceCtrl) {

    // Declare app level module which depends on filters, and services

    angular.module('ReactorWebapp', ['ngRoute'])
    .config(['$routeProvider', function ($routeProvider) {
      
      $routeProvider.when('/overview', {
        templateUrl: '/partials/overview.html', controller: OverviewCtrl
      });
      
      $routeProvider.when('/resources', {
        templateUrl: '/partials/resources.html', controller: ResourcesCtrl
      });
      
      $routeProvider.when('/apps', {
        templateUrl: '/partials/apps.html', controller: AppsCtrl
      });

      $routeProvider.when('/streams', {
        templateUrl: '/partials/apps.html', controller: StreamsCtrl
      });

      $routeProvider.when('/flows', {
        templateUrl: '/partials/flows.html', controller: FlowsCtrl
      });

      $routeProvider.when('/datasets', {
        templateUrl: '/partials/datasets.html', controller: DatasetsCtrl
      });

      $routeProvider.when('/procedures', {
        templateUrl: '/partials/procedures.html', controller: ProceduresCtrl
      });

      $routeProvider.when('/procedures', {
        templateUrl: '/partials/procedures.html', controller: ProceduresCtrl
      });

      $routeProvider.when('/services', {
        templateUrl: '/partials/services.html', controller: ServicesCtrl
      });

      $routeProvider.when('/apps/:appId', {
        templateUrl: '/partials/app.html', controller: AppCtrl
      });

      $routeProvider.when('/flows/:flowId', {
        templateUrl: '/partials/flow.html', controller: FlowCtrl
      });

      $routeProvider.when('/streams/:streamId', {
        templateUrl: '/partials/stream.html', controller: StreamCtrl
      });

      $routeProvider.when('/datasets/:datasetId', {
        templateUrl: '/partials/procedure.html', controller: DatasetCtrl
      });

      $routeProvider.when('/procedures/:procedureId', {
        templateUrl: '/partials/procedure.html', controller: ProcedureCtrl
      });

      $routeProvider.when('/services/:serviceId', {
        templateUrl: '/partials/service.html', controller: ServiceCtrl
      });

      $routeProvider.when('/loading', {
        templateUrl: '/partials/loading.html', controller: LoadingCtrl
      });

      $routeProvider.when('/login', {
        templateUrl: '/partials/login.html', controller: LoginCtrl
      });

      $routeProvider.when('/pagenotfound', {
        templateUrl: '/partials/pagenotfound.html', controller: PageNotFoundCtrl
      });

      $routeProvider.when('/connectionerror', {
        templateUrl: '/partials/connectionerror.html', controller: ConnectionErrorCtrl
      });

      $routeProvider.when('/analyze', {
        templateUrl: '/partials/analyze.html', controller: AnalyzeCtrl
      });

      $routeProvider.otherwise({redirectTo: '/overview'});
    
    }]);

    angular.bootstrap(document, ['ReactorWebapp']);

});