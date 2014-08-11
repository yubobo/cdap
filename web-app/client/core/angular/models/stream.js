'use strict';

define(['helpers'], function (helpers) {

  var Model = [function() {
    return Class.create({
      initialize: function (data) {
        this.type = 'Stream';
        this.id = '';
        this.name = '';

        if (data && Object.prototype.toString.call(data) === '[object Object]') {
          for (var index in data) {
            this[index] = data[index];
          }
        }

        this.id = this.id ? this.id : this.name;

      },

      getEventsProcessedEndpoint: function () {
        return helpers.getEventsProcessedEndpoint(this);
      },

      getArrivalRateEndpoint: function () {
        return helpers.getArrivalRateEndpoint(this);
      },

      getStorageEndpoint: function () {
        return helpers.getStorageEndpoint(this);
      }
    });
  }];

  return Model;

});