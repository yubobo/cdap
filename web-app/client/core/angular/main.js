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

/**
 * Load dependencies using Requirejs module loader.
 */
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
  './models/app',
  './datafactory',
  './helpers',
  'angular-route',
  'jQuery',
  'bootstrap'],
  function (
    angular,
    OverviewCtrl, 
    ResourcesCtrl, 
    AppsCtrl, 
    FlowsCtrl, 
    DatasetsCtrl,
    ProceduresCtrl, 
    StreamsCtrl, 
    AppCtrl, 
    FlowCtrl, 
    StreamCtrl, 
    ProcedureCtrl, 
    DatasetCtrl,
    LoadingCtrl, 
    LoginCtrl, 
    PageNotFoundCtrl, 
    ConnectionErrorCtrl, 
    AnalyzeCtrl, 
    ServicesCtrl,
    ServiceCtrl,
    AppModel,
    DataFactory,
    Helpers) {

    // Instantiate module and declare routes.

    var reactorWebapp = angular.module('ReactorWebapp', ['ngRoute']);
    reactorWebapp.config(['$routeProvider', function ($routeProvider) {
      
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
        templateUrl: '/partials/streams.html', controller: StreamsCtrl
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

    reactorWebapp.value('App', AppModel);
    
    // Backend connections and all ajax calls are made in the data factory.
    reactorWebapp.factory('dataFactory', DataFactory);
    reactorWebapp.factory('helpers', Helpers);

    // Declares any constants in the application here. 
    // Constants are defined in capital letters.
    reactorWebapp.constant('REACTOR_ENDPOINT', '/rest')
    .constant('METRICS_TIMER', 3000);
    
    // Assing controllers a name so that they can be used in templates eg:
    // <div ng-include="<template location>" ng-controller="OverviewCtrl"></div>

    reactorWebapp.controller('OverviewCtrl', OverviewCtrl)
    .controller('ResourcesCtrl', ResourcesCtrl)
    .controller('AppsCtrl', AppsCtrl)
    .controller('FlowsCtrl', FlowsCtrl)
    .controller('DatasetsCtrl', DatasetsCtrl)
    .controller('ProceduresCtrl', ProceduresCtrl)
    .controller('StreamsCtrl', StreamsCtrl)
    .controller('AppCtrl', AppCtrl)
    .controller('FlowCtrl', FlowCtrl)
    .controller('StreamCtrl', StreamCtrl)
    .controller('ProcedureCtrl', ProcedureCtrl)
    .controller('DatasetCtrl', DatasetCtrl)
    .controller('LoadingCtrl', LoadingCtrl)
    .controller('LoginCtrl', LoginCtrl)
    .controller('PageNotFoundCtrl', PageNotFoundCtrl)
    .controller('ConnectionErrorCtrl', ConnectionErrorCtrl)
    .controller('AnalyzeCtrl', AnalyzeCtrl)
    .controller('ServicesCtrl', ServicesCtrl)
    .controller('ServiceCtrl', ServiceCtrl);


    // Manually bootstrap the application since we are bootstrapping with requirejs.    
    angular.bootstrap(document, ['ReactorWebapp']);

});