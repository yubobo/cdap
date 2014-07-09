'use strict';

define(['helpers'], function (helpers) {

  var Model = Class.create({
    initialize: function (data) {
      this.type = 'Workflow';
      this.id = '';
      this.name = '';
      this.finalMetric = 0;

      if (data && Object.prototype.toString.call(data) === '[object Object]') {
        for (var index in data) {
          this[index] = data[index];
        }
      }
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

  return Model;

});