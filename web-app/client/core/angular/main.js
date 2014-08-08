/*global require, requirejs */

'use strict';

requirejs.config({
  paths: {
    'angular': ['/third_party/angular-1.2.16/angular'],
    'angular-route': ['/third_party/angular-1.2.16/angular-route'],
    'ui-router': ['/third_party/angular-ui-router'],
    'angular-sanitize': ['/third_party/angular-1.2.16/angular-sanitize'],
    'jQuery': ['/third_party/jquery-1.11.1.min'],
    'bootstrap': ['/third_party/bootstrap/bootstrap.min'],
    'helpers': ['/core/angular/helpers']
  },
  shim: {
    'angular': {
      exports : 'angular'
    },
    'ui-router': {
      deps: ['angular']
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
  './controllers/mapreduce',
  './controllers/workflow',
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
  './action-service',
  './metrics-service',
  './status-service',
  './datafactory',

  // Directives.
  './directives/sparkline',
  './directives/delay-counter',
  './directives/status',
  './directives/bytes-counter',

  // Filters.
  './filters',

  'angular-route',
  'ui-router',
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
    MapreduceCtrl,
    WorkflowCtrl,
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
    ActionService,
    MetricsService,
    StatusService,
    DataFactory,

    // Directives
    Sparkline,
    DelayCounter,
    Status,
    BytesCounter) {

    // Instantiate Reactor webapp module.
    var reactorWebapp = angular.module('ReactorWebapp', [
      'ReactorWebapp.filters',
      'ngRoute',
      'ui.router'
    ]);

    // Configure routes.
    reactorWebapp.config(['$routeProvider', '$stateProvider', '$urlRouterProvider', 
      function ($routeProvider, $stateProvider, $urlRouterProvider) {

      $urlRouterProvider.otherwise('/pagenotfound');
      $urlRouterProvider.when('', '/overview');
      $urlRouterProvider.when('/apps/:appId/flows/:flowId', '/apps/:appId/flows/:flowId/status');

      $stateProvider
        .state('overview', {
          url: '/overview',
          templateUrl: '/templates/overview.html',
          controller: OverviewCtrl
        })

        .state('resources', {
          url: '/resources',
          templateUrl: '/templates/resources.html',
          controller: ResourcesCtrl
        })        

        .state('apps', {
          url: '/apps',
          templateUrl: '/templates/apps.html',
          controller: AppsCtrl
        })

        .state('streams', {
          url: '/streams',
          templateUrl: '/templates/streams.html',
          controller: StreamsCtrl
        })

        .state('flows', {
          url: '/flows',
          templateUrl: '/templates/flows.html',
          controller: FlowsCtrl
        })

        .state('datasets', {
          url: '/datasets',
          templateUrl: '/templates/datasets.html',
          controller: DatasetsCtrl
        })

        .state('procedures', {
          url: '/procedures',
          templateUrl: '/templates/procedures.html',
          controller: ProceduresCtrl
        })

        .state('services', {
          url: '/services',
          templateUrl: '/templates/services.html',
          controller: ServicesCtrl
        })

        .state('appsDetail', {
          url: '/apps/:appId',
          templateUrl: '/templates/app.html',
          controller: AppCtrl
        })

        .state('flowsDetail', {
          url: '/apps/:appId/flows/:flowId',
          templateUrl: '/templates/flow.html',
          controller: FlowCtrl
        })

          .state('flowsDetail.status', {
            url: '/status',
            templateUrl: '/templates/partials/flowStatus.html',
            controller: FlowCtrl
          })

          .state('flowsDetail.log', {
            url: '/log',
            templateUrl: '/templates/partials/flowLog.html',
            controller: FlowCtrl
          })

          .state('flowsDetail.history', {
            url: '/history',
            templateUrl: '/templates/partials/flowHistory.html',
            controller: FlowCtrl
          })

        .state('streamsDetail', {
          url: '/apps/:appId/streams/:streamId',
          templateUrl: '/templates/stream.html',
          controller: StreamCtrl
        })

        .state('datasetsDetail', {
          url: '/apps/:appId/datasets/:datasetId',
          templateUrl: '/templates/dataset.html',
          controller: DatasetCtrl
        })

        .state('proceduresDetail', {
          url: '/apps/:appId/procedures/:procedureId',
          templateUrl: '/templates/procedure.html',
          controller: ProcedureCtrl
        })

        .state('mapreducesDetail', {
          url: '/apps/:appId/mapreduces/:mapreduceId',
          templateUrl: '/templates/procedure.html',
          controller: MapreduceCtrl
        })

        .state('workflowsDetail', {
          url: '/apps/:appId/workflows/:workflowId',
          templateUrl: '/templates/workflow.html',
          controller: WorkflowCtrl
        })

        .state('servicesDetail', {
          url: '/services/:serviceId',
          templateUrl: '/templates/service.html',
          controller: ServiceCtrl
        })

        .state('loading', {
          url: '/loading',
          templateUrl: '/templates/loading.html',
          controller: LoadingCtrl
        })

        .state('login', {
          url: '/login',
          templateUrl: '/templates/login.html',
          controller: LoginCtrl
        })

        .state('pagenotfound', {
          url: '/pagenotfound',
          templateUrl: '/templates/pagenotfound.html',
          controller: PageNotFoundCtrl
        })

        .state('connectionerror', {
          url: '/connectionerror',
          templateUrl: '/templates/connectionerror.html',
          controller: ConnectionErrorCtrl
        })

        .state('analyze', {
          url: '/analyze',
          templateUrl: '/templates/analyze.html',
          controller: AnalyzeCtrl
        })

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


    // Backend connections and all ajax calls are made in the factory handlers.
    reactorWebapp.factory('actionService', ActionService);
    reactorWebapp.factory('metricsService', MetricsService);
    reactorWebapp.factory('statusService', StatusService);
    reactorWebapp.factory('dataFactory', DataFactory);


    // Declares any constants in the application here. These values SHOULD NOT change.
    // Constants are defined in capital letters.
    reactorWebapp.constant('REACTOR_ENDPOINT', '/rest')
    .constant('POLLING_INTERVAL', 3000)
    .constant('NOTIFICATION_CATEGORIES', {
      'SUCCESS': 'SUCCESS',
      'DANGER': 'DANGER',
      'WARNING': 'WARNING',
      'INFO': 'INFO'
    })
    .constant('DEFAULT_DISPLAY_TIME', 1000);


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
    .controller('FlowCtrl', MapreduceCtrl)
    .controller('FlowCtrl', WorkflowCtrl)
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
    reactorWebapp.directive('delayCounter', DelayCounter);
    reactorWebapp.directive('status', Status);
    reactorWebapp.directive('bytesCounter', BytesCounter);


    // Manually bootstrap the application since we are bootstrapping with requirejs.
    angular.bootstrap(document, ['ReactorWebapp']);

});