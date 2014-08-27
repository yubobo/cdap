'use strict';

define(['angular'], function (angular) {
return angular.module('dashboard.app.controllers', ['dashboard.app.services','ui.bootstrap'])
    .controller('ProjectCtrl', function ($scope, $modal, $window, ProjectService, LayoutHelper, SettingsService, ComponentsService, ApiService) {
        // $scope.project = projectDetails; // projectDetails is resolved from the route
        $scope.project = ProjectService.getBlankProject();
        $scope.availableRowLayouts = LayoutHelper.getAvailableRowLayouts();
        $scope.newRowFormat = $scope.availableRowLayouts[0].val;
        $scope.availableComponents = ComponentsService.getAvailableComponents();

        // when this controller is destroyed (e.g. when switching projects), remove all api endpoints/callbacks
        $scope.$on('$destroy', function() {
            ApiService.removeAllEndpoints();
        });

        $scope.editingRows = false;
        $scope.toggleRowEditingMode = function () {
            $scope.editingRows = !$scope.editingRows;
        };
                
        $scope.sortableWidgetOptions = {
			placeholder: 'widget-placeholder',
			connectWith: '.widget-container',
			cursor: 'move',
			handle: '.widget-move-handle',
            stop: function() {
              console.log('TO DO: save layout without the $watch statement');
            }
        };
        $scope.sortableRowOptions = {
            placeholder: '',
            connectWith: '.widget-row',
            cursor: 'move',
            handle: '.move-handle',
            stop: function() {
              console.log('TO DO: save layout without the $watch statement');
            }
        };
        $scope.newTabSettings = {
            name: '',
            firstTabRowFormat: $scope.availableRowLayouts[0].val
        };
        $scope.newWidgetSettings = {
            title: '',
            endpoint: 'http://',
            row: 0,
            col: 0,
            component: ''
        };


        // deal with changing tab views
        $scope.setCurrentTab = function (tabId) {
            var c;
            if (typeof tabId === 'undefined') {
                c = $scope.project.tabs[0];
            }
            for (var i = 0, len = $scope.project.tabs.length; i<len; i++) {
                if ($scope.project.tabs[i].id === tabId) {
                    c = $scope.project.tabs[i];
                    break;
                }
            }
            if (c === null) {
                c = $scope.project.tabs[0];
            }
            $scope.$evalAsync(function () {
                $scope.currentLayout = c;
            });            
        }

        // watch for changes to the project and auto-save it
        $scope.$watch('project', function (newVal, oldVal) {
            if (newVal && (newVal !== oldVal)) {
                ProjectService.saveProject(newVal);
            }            
        }, true);
        //
        $scope.maxTabs = SettingsService.getSetting('max_tabs_per_project');
        $scope.addTab = function () {
            if ($scope.project.tabs.length >= $scope.maxTabs) {
                $window.alert('You can only add up to ' + $scope.maxTabs + ' tabs per project!');
                return false;
            }
            $modal.open({
                templateUrl: 'views/modals/add-project-tab.html',
                controller: 'AddTabModalCtrl',
                scope: $scope
            });            
        };
        $scope.deleteCurrentTab = function () {
            if ($window.confirm('Are you sure you want to delete this tab/view? There is no UNDO!')) {
                if ($scope.project.tabs.length === 1) {
                    $window.alert('You must have at least one tab/view in your project!');
                    return false;
                }
                var tabId = $scope.currentLayout.id;
                for (var i=0, len=$scope.project.tabs.length; i<len; i++) {
                    if ($scope.project.tabs[i].id === tabId) {
                        $scope.project.tabs.splice(i,1);
                        break;
                    }
                }
                ProjectService.saveProject($scope.project);
                $scope.setCurrentTab($scope.project.tabs[0].id);
            }            
        };
        $scope.addRow = function () {
            $scope.$evalAsync(function () {
                var newRow = LayoutHelper.getFormattedRow($scope.newRowFormat);
                $scope.currentLayout.layout.rows.push(newRow);
            });
        };
        $scope.deleteRow = function (index) {
            if ($window.confirm('Are you sure you want to delete this row? There is no UNDO!')) {
                $scope.currentLayout.layout.rows.splice(index,1);
                ProjectService.saveProject($scope.project);
            }
        };

        $scope.addWidget = function () {
            $modal.open({
                templateUrl: 'views/modals/add-widget.html',
                controller: 'AddWidgetModalCtrl',
                scope: $scope
            });
        };
        $scope.deleteWidget = function (col, index) {
            if ($window.confirm('Are you sure you want to delete this widget? There is no UNDO!')) {
                col.widgets.splice(index,1);
                ProjectService.saveProject($scope.project);
            }
        };
        $scope.editWidgetSettings = function (widget) {
            $modal.open({
                templateUrl: 'views/modals/widget-settings.html',
                controller: 'WidgetSettingsModalCtrl',
                resolve: {
                    widget: function () { return widget; }
                }
            });
        };

        $scope.showSettings = function () {
            $modal.open({
                templateUrl: 'views/modals/project-settings.html',
                controller: 'ProjectSettingsModalCtrl',
                scope: $scope,
                backdrop: 'static'
            });
        };

        $scope.exportProject = function () {
            $modal.open({
                templateUrl: 'views/modals/export-project.html',
                controller: 'ProjectExportModalCtrl',
                scope: $scope
            });
        };
    })
    .controller('ProjectSettingsModalCtrl', function ($scope, $rootScope, $window, $modalInstance, ProjectService) {
        $scope.editProjectError = false;

        $scope.save = function () {
            if ($scope.project.name === '') {
                $scope.editProjectError = 'Your project name cannot be blank!';
            } else {
                $rootScope.$emit('projectUpdated');
                $modalInstance.dismiss();
            }   
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
        $scope.deleteProject = function () {
            if ($window.confirm('Are you sure you want to delete this project? There is no UNDO!')) {
                ProjectService.deleteProject($scope.project.id);
                $rootScope.$emit('projectDeleted');
                $modalInstance.dismiss();
            }
        };
    })
    .controller('AddTabModalCtrl', function ($scope, $rootScope, $modalInstance, ProjectService, LayoutHelper) {
        $scope.addTabError = false;

        $scope.add = function () {
            if ($scope.newTabSettings.name === '') {
                $scope.addTabError = 'Please enter a name for your new tab';
            } else {
                var newTab = ProjectService.getBlankTab();
                newTab.order = $scope.project.tabs.length + 1;
                newTab.name = $scope.newTabSettings.name;
                //
                var newRow = LayoutHelper.getFormattedRow($scope.newTabSettings.firstTabRowFormat);
                newTab.layout.rows.push(newRow);
                $scope.project.tabs.push(newTab);
                //
                $scope.newTabSettings.name = '';
                $scope.newTabSettings.firstTabRowFormat = $scope.availableRowLayouts[0].val;
                $scope.addTabError = false;
                $scope.setCurrentTab(newTab.id);
                $modalInstance.dismiss();   
            }
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    })
    .controller('AddWidgetModalCtrl', function ($scope, $rootScope, $modalInstance, ProjectService, ComponentsService) {
        $scope.addWidgetError = false;
        $scope.requiresEndpoint = false;

        $scope.setWidgetTarget = function (row, col) {            
            $scope.newWidgetSettings.row = row;
            $scope.newWidgetSettings.col = col;
        };

        $scope.add = function () {
            if ($scope.newWidgetSettings.title === '') {
                $scope.addWidgetError = 'Please enter a title for your new widget';
            } else if ($scope.newWidgetSettings.component === '') {
                $scope.addWidgetError = 'Please select a component for your widget';
            } else if (($scope.requiresEndpoint === true) && (($scope.newWidgetSettings.endpoint === '') || ($scope.newWidgetSettings.endpoint === 'http://'))) {
                $scope.addWidgetError = 'Please enter an API endpoint for your widget';
            } else {
                var widget = ProjectService.getBlankWidget();
                widget.title = $scope.newWidgetSettings.title;
                widget.component = $scope.newWidgetSettings.component;
                widget.endpoint = $scope.newWidgetSettings.endpoint;
                $scope.currentLayout.layout.rows[$scope.newWidgetSettings.row].cols[$scope.newWidgetSettings.col].widgets.push(widget);
                $scope.newWidgetSettings.title = '';
                $scope.newWidgetSettings.endpoint = 'http://';
                $scope.newWidgetSettings.component = '';
                $scope.addWidgetError = false;
                $scope.usingCustomComponent = true;
                $modalInstance.dismiss();
            }
        };
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
        $scope.componentSelected = function () {
            var component = ComponentsService.getComponentByNamespace($scope.newWidgetSettings.component);
            $scope.requiresEndpoint = component.requireEndpoint;
        };
    })
    .controller('WidgetSettingsModalCtrl', function ($scope, widget, $modalInstance) {
        $scope.widget = widget;
        $scope.close = function () {
            $modalInstance.dismiss();
        };
    })
    .controller('ProjectExportModalCtrl', function ($scope, $modalInstance) {
        $scope.close = function () {
            $modalInstance.dismiss();
        };
    });
});
