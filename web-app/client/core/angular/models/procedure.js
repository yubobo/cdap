'use strict';

define(function () {

  var Model = function (data) {
    this.type = 'Procedure';
    this.id = '';
    this.name = '';
    this.description = '';
    this.streams = [];
    this.flows  = [];
    this.datasets = [];
    this.procedures = [];

    if (data) {
      for (var index in data) {
        this[index] = data[index];
      }
    }

  };

  return Model;

});