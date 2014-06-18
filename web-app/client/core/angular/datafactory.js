'use strict';

define(function () {

  /* Items */

  var Factory = ['$http', '$interval', '$q', 'REACTOR_ENDPOINT', 'App',
    function($http, $interval, $q, REACTOR_ENDPOINT, App) {

    return {
      /**
       * Get all reactor apps. Depends on the App model.
       * @param  {Function} callback that consumes list of App objects.
       */
      getApps: function (callback) {
        $http.get(REACTOR_ENDPOINT + '/apps').success(function(data) {
          var apps = [];
          data.map(function (entry) {
            apps.push(new App(entry));
          });
          callback(apps);
        });
      }

    };


  }];

  return Factory;

});