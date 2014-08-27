'use strict';

define(['angular'], function (angular) {
return ['$http', '$q', 'storage', 'LayoutHelper', 'GuidService', function ($http, $q, storage, LayoutHelper, GuidService) {
        var blankTab = {
            id: 0,
            order: 0,
            name: 'New Tab',
            layout: {
                rows: []
            }
        };
        var blankProject = {
            id: 0,
            name: '',
            tabs: []
        };
        
        function validateProject (projectObj) {
            if (!projectObj.id) { return 'No project id specified'; }
            if (!projectObj.name) { return 'No project name specified'; }
            if (!projectObj.tabs || (typeof projectObj.tabs !== 'object')) { return 'Project must contain a "tabs" array'; }
            if (!projectObj.tabs.length) { return 'Your project must contain at least one tab'; }
            for (var i=0, len = projectObj.tabs.length; i<len; i++) {
                var tab = projectObj.tabs[i];
                if (!tab.id) { return 'Tab ' + (i+1) + ' has no id attribute'; }
                if (!tab.name) { return 'Tab ' + (i+1) + ' has no name attribute'; }
                if (!tab.layout && (typeof tab.layout !== 'object')) { return 'Tab ' + (i+1) + ' has no layout Object'; }
                if (!tab.layout.rows || (typeof tab.layout.rows !== 'object')) { return 'Tab ' + (i+1) + ' layout has no "rows" array'; }
                for (var j=0, numrows=tab.layout.rows.length; j<numrows; j++) {
                    var row = tab.layout.rows[j];
                    var validRow = validateRow(row);
                    if (validRow !== true) { return 'Tab ' + (i+1) + ' Row ' + (j+1) + ' error: ' + validRow; }
                    for (var k=0, numcols=row.cols.length; k<numcols; k++) {
                        var col = row.cols[k];
                        var validCol = validateColumn(col);
                        if (validCol !== true) { return 'Tab ' + (i+1) + ' Row ' + (j+1) + ' Col ' + (k+1) + ' error: ' + validCol; }
                    }
                }
            }
            return true;
        }

        var availableRowFormatNames = LayoutHelper.getAvailableRowLayoutNames();
        function validateRow (row) {
            if (!row.format || (availableRowFormatNames.indexOf(row.format) === -1)) { return 'invalid row format: ' + row.format; }
            if (!row.cols || (typeof row.cols !== 'object')) { return 'no "cols" array'; }
            return true;
        }

        function validateColumn (col) {
            if (!col.widgets || (typeof col.widgets !== 'object')) { return 'no "widgets" array'; }
            return true;
        }
 

        return {
            validateProject: validateProject,
            deleteAllProjects: function () {
                storage.remove('projects');
                return true;
            },
            getProjects: function () {
                return storage.get('projects') || [];
            },
            getBlankProject: function () {
                return angular.copy(blankProject);
            },
            addProject: function (projectName, firstTabName, firstTabRowFormat) {
                var d = $q.defer();
                setTimeout(function () {
                    var projects = storage.get('projects') || [];
                    var project = angular.copy(blankProject);
                    project.id = GuidService.guid();
                    project.name = projectName;
                    var tab = angular.copy(blankTab);
                    tab.id = GuidService.guid();
                    tab.order = 1;
                    tab.name = firstTabName;
                    var row = LayoutHelper.getFormattedRow(firstTabRowFormat);
                    tab.layout.rows.push(row);
                    project.tabs.push(tab);            
                    projects.push(project);
                    storage.set('projects',projects);
                    d.resolve(project);
                },10);                
                return d.promise;
            },
            deleteProject: function (id) {
                id = id.toString();
                var projects = storage.get('projects') || [];
                for (var i = 0, len = projects.length; i < len; i++) {
                    if (projects[i].id.toString() === id) {
                        projects.splice(i,1);
                        break;
                    }
                }
                storage.set('projects',projects);
                return projects;
            },
            importProject: function (project) {
                var projects = storage.get('projects') || [];
                projects.push(project);
                storage.set('projects',projects);
                return project;
            },
            getProjectById: function (id) {
                id = id.toString();
                var p = {};
                var projects = storage.get('projects') || [];
                for (var i = 0, len = projects.length; i < len; i++) {
                    if (projects[i].id.toString() === id) {
                        p = projects[i];
                        break;
                    }
                }
                return p;
            },
            saveProject: function (project) {
                var projectId = project.id;
                var projects = storage.get('projects') || [];
                for (var i = 0, len = projects.length; i < len; i++) {
                    if (projects[i].id === projectId) {
                        projects[i] = project;
                        break;
                    }
                }
                storage.set('projects',projects);
                return true;
            },
            loadProjectDetails: function (id) {
                //return $http.get('data/' + id + '.json');
                id = id.toString();
                var projects = storage.get('projects') || [];
                var d = $q.defer();
                setTimeout(function () {
                    var p = {};
                    for (var i = 0, len = projects.length; i < len; i++) {
                        if (projects[i].id.toString() === id) {
                            p.data = projects[i];
                            break;
                        }
                    }
                    d.resolve(p);
                },10);                
                return d.promise;
            },
            getBlankTab: function () {
                var t = angular.copy(blankTab);
                t.id = GuidService.guid();
                return t;
            },
            getBlankRow: function () {
                return LayoutHelper.getBlankRow();
            },
            getBlankColumn: function () {
                return LayoutHelper.getBlankColumn();
            },
            getBlankWidget: function () {
                var widget = LayoutHelper.getBlankWidget();
                //var component = LayoutHelper.getBlankComponent();
                //widget.components.push(component);
                return widget;
            }
        };
    }];
});