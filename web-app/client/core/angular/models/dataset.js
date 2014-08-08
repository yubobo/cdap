'use strict';

define(['helpers'], function (helpers) {

  var Model = Class.create({
    initialize: function (data) {
      this.classname = '';
      this.id = '';
      this.name = '';
      this.specification = '';
      this.type = 'Dataset';

      if (data && Object.prototype.toString.call(data) === '[object Object]') {
        for (var index in data) {
          this[index] = data[index];
        }
      }
    },

    getWriteRateEndpoint: function () {
      return helpers.getWriteRateEndpoint(this);
    },

    getStorageEndpoint: function () {
      return helpers.getStorageEndpoint(this);
    }

  });

  return Model;

});