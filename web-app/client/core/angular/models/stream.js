'use strict';

define(['helpers'], function (helpers) {

  var Model = Class.create({
    initialize: function (data) {
      this.type = 'Stream';
      this.id = '';
      this.name = '';

      if (data && Object.prototype.toString.call(data) === '[object Object]') {
        for (var index in data) {
          this[index] = data[index];
        }
      }
    },

    getArrivalRateEndpoint: function () {
      return helpers.getArrivalRateEndpoint(this);
    },

    getStorageEndpoint: function () {
      return helpers.getStorageEndpoint(this);
    }

  });

  return Model;

});