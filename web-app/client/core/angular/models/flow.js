'use strict';

define(['helpers'], function (helpers) {

  var Model = Class.create({
    initialize: function (data) {
      this.app = '';
      this.id = '';
      this.name = '';
      this.description = '';
      this.type = 'Flow';

      if (data && Object.prototype.toString.call(data) === '[object Object]') {
        for (var index in data) {
          this[index] = data[index];
        }
      }
    },

    getBusynessEndpoint: function () {
      return helpers.getBusynessEndpoint(this);
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