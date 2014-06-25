'use strict';

define(function () {

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
    }

  });

  return Model;

});