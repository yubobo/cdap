'use strict';

define([
  'angular',
  '/third_party/dashboard/controllers/ProjectCtrl.js',
  '/third_party/dashboard/services/ApiService.js',
  '/third_party/dashboard/services/ComponentsService.js',
  '/third_party/dashboard/services/GuidService.js',
  '/third_party/dashboard/services/LayoutHelper.js',
  '/third_party/dashboard/services/ProjectService.js',
  '/third_party/dashboard/services/SettingsService.js',
  '/third_party/dashboard/directives.js'
  ], function (angular, controllers, ApiService, ComponentsService, GuidService,
  LayoutHelperService, ProjectService, SettingsService, directives) {
  // our main dashboard module
  angular.module('dashboard.app.services', ['angularLocalStorage'])
    .service('ApiService', ApiService)
    .service('ComponentsService', ComponentsService)
    .service('GuidService', GuidService)
    .service('LayoutHelperService', LayoutHelperService)
    .service('ProjectService', ProjectService)
    .service('SettingsService', SettingsService);
  console.log(controllers);
  angular
    .module('dashboard.app', [
        'ngSanitize',
        'dashboard.app.controllers',
        'dashboard.app.services',
        'dashboard.app.directives'
    ]);
});