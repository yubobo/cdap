'use strict';

define(['angular'], function (angular) {

    return angular.module('dashboard.app.directives', [
            'ui.bootstrap',
            'ui.sortable',
            'dashboard.app.services'
        ])

        .directive('dashboard', ['ProjectService', function (ProjectService) {
            return {
                scope: {
                    projectId: '@'
                },
                templateUrl: '/views/project.html',
                link: function (scope, el, attrs) {
                    scope.$watch('projectId', function (newVal, oldVal) {
                        console.log(newVal);
                        if (!!newVal && (newVal !== 'false')) {
                            ProjectService.loadProjectDetails(newVal)
                                .then(function (project) {
                                    console.log(project);
                                    scope.project = project.data;
                                    if (scope.project.tabs.length) {
                                        scope.setCurrentTab(scope.project.tabs[0].id);
                                    }
                                    el.show();
                                });
                        } else {
                            el.hide();
                        }
                    });
                },
                controller: 'ProjectCtrl'
            }
        }])

        .directive('componentWrapper', ['ComponentsService', '$compile', function (ComponentsService, $compile) {
            return {
                scope: {
                    componentWrapper: '@',
                    endpoint: '@'
                },
                link: function (scope, el, attrs) {
                    var component = ComponentsService.getComponentByNamespace(scope.componentWrapper);
                    var attrName = ComponentsService.componentNameToDirectiveAttribute(component.id);
                    el.html('<div ' + attrName + '="" endpoint="{{endpoint}}"></div>');
                    $compile(el.contents())(scope);
                }
            }
        }])

        .directive('editableText', function () {
            return {
                scope: {
                    editableText: '='
                },
                template: '<div class="form-group form-inline">' +                 
                    '<span ng-show="!editMode" ng-click="toggleEditMode()" class="editable-text">{{editedText}} </span>' + 
                    '<span ng-show="editMode"><input type="text" class="form-control input-sm" ng-model="editedText" enter-input="toggleEditMode()"></span>' + 
                    '<button type="button" class="btn btn-xs" ng-click="toggleEditMode()">{{ buttonLabel }}</button>' + 
                    '</div>',
                replace: true,
                controller: function ($scope) {
                    $scope.editedText = $scope.editableText.toString();
                    $scope.buttonLabel = 'Edit';
                    $scope.editMode = false;
                    $scope.toggleEditMode = function () {
                        $scope.editMode = !$scope.editMode;
                        if ($scope.editMode) {
                            $scope.buttonLabel = 'Done';
                        } else {
                             $scope.buttonLabel = 'Edit'; 
                             $scope.editableText = $scope.editedText;  
                        }
                    };
                }
            };
        })
        
        .directive('enterInput', function () {
            return function (scope, el, attrs) {
                el.bind('keydown', function (e) {
                    if (e.keyCode === 13) {
                        scope.$apply(attrs.enterInput);  
                    }
                });
            };
        })

        .directive('autoFocus', function () {
            return function (scope, el) {
                el.focus();
            };
        })

        .directive('slugInput', function () {
            return function (scope, el) {
                el.bind('input', function () {
                    var cleaned = el.val().replace(/[^\w-]/gi, '');
                    if (cleaned.slice(0,1) === '/') {
                        cleaned = cleaned.slice(1);
                    }
                    el.val(cleaned.toLowerCase());
                });
            };
        })

        .directive('numericInput', function () {
            return function (scope, el) {
                el.bind('input', function () {
                    var cleaned = el.val().replace(/[^0-9.]/g, '');
                    el.val(cleaned);
                });
            };
        });
});