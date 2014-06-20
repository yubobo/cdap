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

  // Controller locations.
  './controllers/base',
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

  // Model locations.
  './models/app',
  './models/flow',
  './models/flowlet',
  './models/dataset',
  './models/procedure',
  './models/service',
  './models/stream',
  './models/mapreduce',
  './models/workflow',

  // Helper library locations.
  './metrics-service',
  './datafactory',
  './helpers',

  // Directives.
  './directives/sparkline',

  // Filters.
  './filters',

  'angular-route',
  'jQuery',
  'bootstrap'],
  function (
    angular,

    // Controllers.
    BaseCtrl,
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

    // Models.
    AppModel,
    FlowModel,
    FlowletModel,
    DatasetModel,
    ProcedureModel,
    ServiceModel,
    StreamModel,
    MapreduceModel,
    WorkflowModel,

    // Helper libraries.
    MetricsService,
    DataFactory,
    Helpers,

    // Directives
    Sparkline) {

    // Instantiate Reactor webapp module.
    var reactorWebapp = angular.module('ReactorWebapp', [
      'ReactorWebapp.filters',
      'ngRoute'
    ]);

    // Configure routes.
    reactorWebapp.config(['$routeProvider', function ($routeProvider) {

      $routeProvider.when('/overview', {
        templateUrl: '/templates/overview.html', controller: OverviewCtrl
      });

      $routeProvider.when('/resources', {
        templateUrl: '/templates/resources.html', controller: ResourcesCtrl
      });

      $routeProvider.when('/apps', {
        templateUrl: '/templates/apps.html', controller: AppsCtrl
      });

      $routeProvider.when('/streams', {
        templateUrl: '/templates/streams.html', controller: StreamsCtrl
      });

      $routeProvider.when('/flows', {
        templateUrl: '/templates/flows.html', controller: FlowsCtrl
      });

      $routeProvider.when('/datasets', {
        templateUrl: '/templates/datasets.html', controller: DatasetsCtrl
      });

      $routeProvider.when('/procedures', {
        templateUrl: '/templates/procedures.html', controller: ProceduresCtrl
      });

      $routeProvider.when('/services', {
        templateUrl: '/templates/services.html', controller: ServicesCtrl
      });

      $routeProvider.when('/apps/:appId', {
        templateUrl: '/templates/app.html', controller: AppCtrl
      });

      $routeProvider.when('/flows/:flowId', {
        templateUrl: '/templates/flow.html', controller: FlowCtrl
      });

      $routeProvider.when('/streams/:streamId', {
        templateUrl: '/templates/stream.html', controller: StreamCtrl
      });

      $routeProvider.when('/datasets/:datasetId', {
        templateUrl: '/templates/dataset.html', controller: DatasetCtrl
      });

      $routeProvider.when('/procedures/:procedureId', {
        templateUrl: '/templates/procedure.html', controller: ProcedureCtrl
      });

      $routeProvider.when('/services/:serviceId', {
        templateUrl: '/templates/service.html', controller: ServiceCtrl
      });

      $routeProvider.when('/loading', {
        templateUrl: '/templates/loading.html', controller: LoadingCtrl
      });

      $routeProvider.when('/login', {
        templateUrl: '/templates/login.html', controller: LoginCtrl
      });

      $routeProvider.when('/pagenotfound', {
        templateUrl: '/templates/pagenotfound.html', controller: PageNotFoundCtrl
      });

      $routeProvider.when('/connectionerror', {
        templateUrl: '/templates/connectionerror.html', controller: ConnectionErrorCtrl
      });

      $routeProvider.when('/analyze', {
        templateUrl: '/templates/analyze.html', controller: AnalyzeCtrl
      });

      $routeProvider.otherwise({redirectTo: '/overview'});

    }]);


    // Declare models and other values here. These values can change in the future.
    reactorWebapp.value('App', AppModel);
    reactorWebapp.value('Flow', FlowModel);
    reactorWebapp.value('Stream', StreamModel);
    reactorWebapp.value('Dataset', DatasetModel);
    reactorWebapp.value('Procedure', ProcedureModel);
    reactorWebapp.value('Flowlet', FlowletModel);
    reactorWebapp.value('Service', ServiceModel);
    reactorWebapp.value('Mapreduce', MapreduceModel);
    reactorWebapp.value('Workflow', WorkflowModel);


    // Backend connections and all ajax calls are made in the data factory.
    reactorWebapp.factory('metricsService', MetricsService);
    reactorWebapp.factory('dataFactory', DataFactory);
    reactorWebapp.factory('helpers', Helpers);


    // Declares any constants in the application here. These values SHOULD NOT change.
    // Constants are defined in capital letters.
    reactorWebapp.constant('REACTOR_ENDPOINT', '/rest')
    .constant('POLLING_INTERVAL', 3000);


    // Assing controllers a name so that they can be used in templates eg:
    // <div ng-include="<template location>" ng-controller="OverviewCtrl"></div>
    reactorWebapp.controller('BaseCtrl', BaseCtrl)
    .controller('OverviewCtrl', OverviewCtrl)
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

    // Directives.
    reactorWebapp.directive('sparkline', Sparkline);


    // Manually bootstrap the application since we are bootstrapping with requirejs.
    angular.bootstrap(document, ['ReactorWebapp']);

});