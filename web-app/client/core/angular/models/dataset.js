'use strict';

define(function () {

  var Model = Class.create({
    initialize: function (data) {
      this.type = '';
      this.classname = '';
      this.id = '';
      this.name = '';

      if (data && Object.prototype.toString.call(data) === '[object Object]') {
        for (var index in data) {
          this[index] = data[index];
        }
      }  
    }

  });

  return Model;

});