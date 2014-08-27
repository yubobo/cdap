'use strict';

define(['angular'], function (angular) {
return ['appSettings','storage', function (appSettings, storage) {

		var bindings = {};
		var settings = angular.copy(appSettings);
		angular.forEach(settings, function (setting, key) {
			setting.value = setting.default;
			bindings[key] = [];
		});
		var storedSettings = storage.get('settings') || {};
		settings = angular.extend(settings,storedSettings);

		function getSetting (which) {
			return settings[which].value;
		}
		function setSetting (which, val) {
			settings[which].value = val;
			angular.forEach(bindings[which], function (callback) {
				callback.call(this, val);
			});
		}
		function restoreDefaults () {
			angular.forEach(appSettings, function (setting) {
				setting.value = setting.default;
			});
			storage.set('settings',settings);
		}
		function persist () {
			storage.set('settings',settings);
		}
		function bind (which, callback) {
			bindings[which].push(callback);
			return getSetting(which);
		}
		return {
			getAllSettings: function () { return settings; },
			getAllSettingNames: function () {
				var names = [];
				angular.forEach(settings, function (setting, key) {
					names.push(key);
				});
				return names;
			},
			restoreDefaults: restoreDefaults,
			getSetting: getSetting,
			setSetting: setSetting,
			persist: persist,
			bind: bind
		};
		
	}];
});