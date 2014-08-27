/*global require, requirejs */

'use strict';

requirejs.config({
  paths: {
    'angular': ['/third_party/angular-1.2.16/angular'],
    'angular-route': ['/third_party/angular-1.2.16/angular-route'],
    'angular-cookies': ['/third_party/angular-1.2.16/angular-cookies'],
    'ui-router': ['/third_party/angular-ui-router'],
    'ui-bootstrap' : ['/third_party/angular-bootstrap/ui-bootstrap-tpls'],
    'angularLocalStorage': ['/third_party/angularLocalStorage/src/angularLocalStorage'],
    'angular-sanitize': ['/third_party/angular-1.2.16/angular-sanitize'],
    'ui-sortable': ['/third_party/angular-ui-sortable/sortable'],
    'jQuery': ['/third_party/jquery-1.11.1.min'],
    'jquery-ui': ['/third_party/jquery-ui/jquery-ui'],
    'bootstrap': ['/third_party/bootstrap/bootstrap.min'],
    'helpers': ['/core/angular/helpers'],
    'plumber': ['/core/angular/plumber'],
    'timeago': ['/third_party/jquery.timeago'],
    'd3': ['/third_party/d3/d3'],
    'nvd3': ['/third_party/nvd3/nv.d3'],
    'angular-nvd3': ['/third_party/angular-nvd3/dist/angular-nvd3.min'],
    'ng-table': ['/third_party/ng-table/ng-table'],
    'ng-grid': ['/third_party/ng-grid/build/ng-grid'],
    'dashboard-app': ['/third_party/dashboard/dashboard-app.ad552724']
  },
  shim: {
    'angular': {
      exports : 'angular'
    },
    'jquery-ui' : {
      deps: ['jQuery']
    },
    'angular-route': {
      deps: ['angular'],
      exports : 'angular'
    },
    'angular-sanitize': {
      deps: ['angular'],
      exports : 'angular'
    },
    'angular-cookies': {
      deps: ['angular'],
      exports: 'angular'
    },
    'ui-router': {
      deps: ['angular']
    },
    'ui-bootstrap': {
      deps: ['angular']
    },
    'ui-sortable': {
      deps: ['angular', 'jquery-ui']
    },
    'ng-table': {
      deps: ['angular']
    },
    'ng-grid': {
      deps: ['angular']
    },
    'nvd3': {
      deps: ['d3']
    },
    'angular-nvd3': {
      deps: ['angular', 'nvd3']
    },
    'angularLocalStorage': {
      deps: ['angular', 'angular-cookies']
    },
    'bootstrap': {
      deps: ['jQuery']
    },
    'timeago': {
      deps: ['jQuery']
    },
    'dashboard-app': {
      deps: ['angular', 'jQuery', 'ui-bootstrap', 'ui-sortable', 'angularLocalStorage', 'angular-sanitize',
      'd3', 'nvd3', 'ng-table', 'ng-grid', 'angular-nvd3']
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
  './controllers/dashboard',
  './controllers/resources',
  './controllers/apps',
  './controllers/flows',
  './controllers/datasets',
  './controllers/procedures',
  './controllers/streams',
  './controllers/app',
  './controllers/flow',
  './controllers/flowconfig',
  './controllers/flowflowlet',
  './controllers/flowhistory',
  './controllers/mapreduce',
  './controllers/workflow',
  './controllers/stream',
  './controllers/procedure',
  './controllers/procedureconfig',
  './controllers/procedurehistory',
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
  './directives/startstopconfig',
  './directives/flowviz',
  './directives/dagnode',
  './directives/keyval',
  './directives/flowletviz',
  './directives/logview',

  // Filters.
  './filters',

  'angular-route',
  'angular-sanitize',
  'angularLocalStorage',
  'ui-router',
  'ui-bootstrap',
  'ui-sortable',
  'jQuery',
  'bootstrap',
  'dashboard-app'
  ],
  function (
    angular,

    // Controllers.
    BaseCtrl,
    OverviewCtrl,
    DashboardCtrl,
    ResourcesCtrl,
    AppsCtrl,
    FlowsCtrl,
    DatasetsCtrl,
    ProceduresCtrl,
    StreamsCtrl,
    AppCtrl,
    FlowCtrl,
    FlowConfigCtrl,
    FlowFlowletCtrl,
    FlowHistoryCtrl,
    MapreduceCtrl,
    WorkflowCtrl,
    StreamCtrl,
    ProcedureCtrl,
    ProcedureConfigCtrl,
    ProcedureHistoryCtrl,
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
    BytesCounter,
    StartStopConfig,
    FlowViz,
    DagNode,
    KeyVal,
    FlowletViz,
    LogView) {

    window.jQuery.ajax({
      url: 'config/dashboard-config.json'
    }).done(function (configResponse) {

      var components = configResponse.components;
      var totalLoads = 0;
      var totalLoaded = 0;
      function scriptLoaded () {
          totalLoaded++;
          if (totalLoaded >= totalLoads) {
            angular
                .module('dashboard.app')
                    .value('appSettings',configResponse.settings)
                    .value('appComponents',configResponse.components);



            // Instantiate Reactor webapp module.
            var reactorWebapp = angular.module('ReactorWebapp', [
              'ReactorWebapp.filters',
              'ngRoute',
              'ui.router',
              'dashboard.app'
            ]);

            // Configure routes.
            reactorWebapp.config(['$routeProvider', '$stateProvider', '$urlRouterProvider', 
              function ($routeProvider, $stateProvider, $urlRouterProvider) {

              $urlRouterProvider.otherwise('/pagenotfound');
              $urlRouterProvider.when('', '/overview');
              $urlRouterProvider.when('/apps/:appId/flows/:flowId', '/apps/:appId/flows/:flowId/status');
              $urlRouterProvider.when(
                '/apps/:appId/procedures/:procedureId', '/apps/:appId/procedures/:procedureId/status');

              $stateProvider
                .state('overview', {
                  url: '/overview',
                  templateUrl: '/templates/overview.html',
                  controller: OverviewCtrl
                })

                .state('dashboard1', {
                  url: '/dashboard1',
                  templateUrl: '/templates/dashboard1.html',
                  controller: DashboardCtrl
                })

                .state('dashboard2', {
                  url: '/dashboard2',
                  templateUrl: '/templates/dashboard2.html',
                  controller: DashboardCtrl
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
                    templateUrl: '/templates/partials/flowstatus.html',
                    controller: FlowCtrl
                  })

                    .state('flowsDetail.status.config', {
                      url: '/config',
                      templateUrl: '/templates/partials/flowstatusconfig.html',
                      controller: FlowConfigCtrl
                    })

                    .state('flowsDetail.status.flowletId', {
                      url: '/flowlet/:flowletId',
                      templateUrl: '/templates/partials/flowflowlet.html',
                      controller: FlowFlowletCtrl
                    })

                  .state('flowsDetail.log', {
                    url: '/log',
                    templateUrl: '/templates/partials/flowlog.html',
                    controller: FlowCtrl
                  })

                  .state('flowsDetail.history', {
                    url: '/history',
                    templateUrl: '/templates/partials/flowhistory.html',
                    controller: FlowHistoryCtrl
                  })

                .state('streamsDetail', {
                  url: '/streams/:streamId',
                  templateUrl: '/templates/stream.html',
                  controller: StreamCtrl
                })

                .state('datasetsDetail', {
                  url: '/datasets/:datasetId',
                  templateUrl: '/templates/dataset.html',
                  controller: DatasetCtrl
                })

                .state('proceduresDetail', {
                  url: '/apps/:appId/procedures/:procedureId',
                  templateUrl: '/templates/procedure.html',
                  controller: ProcedureCtrl
                })

                  .state('proceduresDetail.status', {
                    url: '/status',
                    templateUrl: '/templates/partials/procedurestatus.html',
                    controller: ProcedureCtrl
                  })

                    .state('proceduresDetail.status.config', {
                      url: '/config',
                      templateUrl: '/templates/partials/procedurestatusconfig.html',
                      controller: ProcedureConfigCtrl
                    })

                  .state('proceduresDetail.log', {
                    url: '/log',
                    templateUrl: '/templates/partials/procedurelog.html',
                    controller: ProcedureCtrl
                  })

                  .state('proceduresDetail.history', {
                    url: '/history',
                    templateUrl: '/templates/partials/procedurehistory.html',
                    controller: ProcedureHistoryCtrl
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
            reactorWebapp.factory('App', AppModel);
            reactorWebapp.factory('Stream', StreamModel);
            reactorWebapp.factory('Dataset', DatasetModel);
            reactorWebapp.factory('Procedure', ProcedureModel);
            reactorWebapp.factory('Flowlet', FlowletModel);
            reactorWebapp.factory('Flow', FlowModel);
            reactorWebapp.factory('Service', ServiceModel);
            reactorWebapp.factory('Mapreduce', MapreduceModel);
            reactorWebapp.factory('Workflow', WorkflowModel);


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


            // Assing controller a name so that they can be used in templates eg:
            // <div ng-include="<template location>" ng-controller="OverviewCtrl"></div>
            reactorWebapp.controller('BaseCtrl', BaseCtrl);
            reactorWebapp.controller('AppsCtrl', AppsCtrl)

            // Directives.
            reactorWebapp.directive('sparkline', Sparkline);
            reactorWebapp.directive('delayCounter', DelayCounter);
            reactorWebapp.directive('status', Status);
            reactorWebapp.directive('bytesCounter', BytesCounter);
            reactorWebapp.directive('startstopconfig', StartStopConfig);
            reactorWebapp.directive('flowviz', FlowViz);
            reactorWebapp.directive('dagnode', DagNode);
            reactorWebapp.directive('keyval', KeyVal);
            reactorWebapp.directive('flowletviz', FlowletViz);
            reactorWebapp.directive('logview', LogView);
            // Manually bootstrap the application since we are bootstrapping with requirejs.
            angular.bootstrap(document, ['ReactorWebapp']);
          }
      }
      angular.forEach(components, function (componentGroup) {
        totalLoads++;
        // add the custom component module into the dashboard.app dependencies...
        angular.module('dashboard.app').requires.push(componentGroup.module);
        // attach the module .js file to the DOM
        var s = window.document.createElement('script');
        s.type = 'text/javascript';
        s.src = componentGroup.moduleFile;
        s.onload = scriptLoaded;
        window.document.getElementsByTagName('body')[0].appendChild(s);
        // loop over all the components inside the component module and add their main.js and style.css files to the DOM
        angular.forEach(componentGroup.components, function (component) {
          totalLoads++;
          var cs = window.document.createElement('script');
          cs.type = 'text/javascript';
          cs.src = component.path + '/main.js';
          cs.onload = scriptLoaded;
          window.document.getElementsByTagName('body')[0].appendChild(cs);
          //
          var link = window.document.createElement('link');
          link.type = 'text/css';
          link.rel = 'stylesheet';
          link.href = component.path + '/style.css';
          window.document.getElementsByTagName('head')[0].appendChild(link);
        });          
      });
               

    }).fail(function () {
      alert('Dashboard config/dashboard-config.json file not found or is invalid JSON - aborting Dashboard app.');
    });

});