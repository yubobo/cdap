'use strict';

define(['helpers'], function (helpers) {

  var Model = Class.create({
    initialize: function (data) {
      this.id = '';
      this.name = '';
      this.description = '';
      this.streams = [];
      this.flows  = [];
      this.datasets = [];
      this.procedures = [];
      this.type = 'App';
      this.finalMetric = 0;

      if (data && Object.prototype.toString.call(data) === '[object Object]') {
        for (var index in data) {
          this[index] = data[index];
        }
      }
    },

    getBusynessEndpoint: function () {
      return helpers.getBusynessEndpoint(this);
    }

  });

  return Model;

});