'use strict';

define(function () {

  var Model = Class.create({
    initialize: function (data) {
      this.type = '';
      this.id = '';
      this.name = '';
      this.description = '';
      this.streams = [];
      this.flows  = [];
      this.datasets = [];
      this.procedures = [];

      if (data && Object.prototype.toString.call(data) === '[object Object]') {
        for (var index in data) {
          this[index] = data[index];
        }
      }  
    }

  });

  return Model;

});