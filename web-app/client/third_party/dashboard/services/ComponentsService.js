'use strict';

define(['angular'], function (angular) {
return ['appComponents', function (appComponents) {
    	
    	var availableComponents = angular.copy(appComponents);

    	return {
    		getAvailableComponents: function () { return availableComponents; },
    		getComponentByNamespace: function (ns) {
    			var component = {};
    			var nsa = ns.split('.');
    			var componentName = nsa.pop();
    			var moduleName = nsa.join('.');
    			console.log('component name', componentName, 'moduleName', moduleName);
    			angular.forEach(availableComponents, function (group) {
    				if (group.module === moduleName) {
    					for (var i=0, len=group.components.length; i<len; i++) {
    						if (group.components[i].id === componentName) {
    							component = group.components[i];
    							component.namespace = group.namespace;
    							component.module = group.module;
    							break;
    						}
    					}
    				}
    			});
    			return component;
    		},
    		componentNameToDirectiveAttribute: function (cname) {
    			return cname.replace(/([a-z])([A-Z])/g, '$1-$2').toLowerCase();
    		}
    	}

    }];
});