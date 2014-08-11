'use strict';

define(['helpers'], function (helpers) {

  var Model = [function () {
    
    return Class.create({
      initialize: function (data) {
        this.type = 'Flowlet';
        this.subtype = '';
        this.id = '';
        this.name = '';
        this.description = '';
        this.instances = 0;
        this.classname = '';

        if (data) {
          for (var index in data) {
            this[index] = data[index];
          }
        }
        this.id = this.id ? this.id : this.name;
      },

      getEventsProcessedAggregateEndpoint: function () {
        return helpers.getEventsProcessedAggregateEndpoint(this);
      }

      getEventsProcessedEndpoint: function () {
        return helpers.getEventsProcessedEndpoint(this); 
      },

      getInboundEventsEndpoint: function () {
        return helpers.getInboundEventsEndpoint(this); 
      },

      getOutboundEventsEndpoint: function () {
        return helpers.getOutboundEventsEndpoint(this);
      },

      getBusynessEndpoint: function () {
        return helpers.getBusynessEndpoint(this);
      },

      getErrorsEndpoint: function () {
        return helpers.getErrorsEndpoint(this);
      }

    });

  }];

  return Model;

});