'use strict';

define(['helpers'], function (helpers) {

  var Model = Class.create({
    initialize: function (data) {
      this.type = 'Mapreduce';
      this.id = '';
      this.name = '';

      if (data && Object.prototype.toString.call(data) === '[object Object]') {
        for (var index in data) {
          this[index] = data[index];
        }
      }
    },

    getMappingStatus: function () {
      return helpers.getMappingStatus(this);
    },

    getReducingStatus: function () {
      return helpers.getReducingStatus(this);
    },

    getStatusEndpoint: function () {
      return helpers.getStatusEndpoint(this);
    }

  });

  return Model;

});