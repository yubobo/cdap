'use strict';

define(function () {

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
    }

  });

  return Model;

});