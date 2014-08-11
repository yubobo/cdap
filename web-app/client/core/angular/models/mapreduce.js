'use strict';

define(['helpers'], function (helpers) {

  var Model = [function() {
    return Class.create({
      initialize: function (data) {
        this.type = 'Mapreduce';
        this.id = '';
        this.name = '';
        this.description = '';

        if (data && Object.prototype.toString.call(data) === '[object Object]') {
          for (var index in data) {
            this[index] = data[index];
          }
        }
      },

      getMappingStatusEndpoint: function () {
        return helpers.getMappingStatusEndpoint(this);
      },

      getReducingStatusEndpoint: function () {
        return helpers.getReducingStatusEndpoint(this);
      },

      getStatusEndpoint: function () {
        return helpers.getStatusEndpoint(this);
      },

      getStartEndpoint: function () {
        return helpers.getStartEndpoint(this);
      },

      getStopEndpoint: function () {
        return helpers.getStopEndpoint(this);
      }

    });
  }];
  return Model;

});