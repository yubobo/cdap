'use strict';

define(['angular'], function (angular) {
return ['GuidService', function (GuidService) {
		
		var availableRowLayouts = [
			{ val: 'COL1', label: '1 column - full row span' },
			{ val: 'COL2', label: '2 equal columns (50% each)' },
			{ val: 'COL3', label: '3 equal columns (33% each)' },
			{ val: 'COL12', label: '1 span left (33%), 2 span right (66%)' },
			{ val: 'COL21', label: '2 span left (66%), 1 span right (33%)' }
		];
		var blankRow = { format: '', cols: [] };
        var blankColumn = { span: 0, widgets: [] };
        var blankWidget = { title: '', endpoint: '', component: '' };
        var blankComponent = {
        	type: '',
        	filter: function (dataIn) {
        		return dataIn;
        	}
        };

		return {
			getAvailableRowLayouts: function () {
				return availableRowLayouts;
			},
			getAvailableRowLayoutNames: function () {
				var names = [];
				angular.forEach(availableRowLayouts, function (layout) {
					names.push(layout.val);
				});
				return names;
			},
			getBlankRow: function () { var row = angular.copy(blankRow); row.id = GuidService.guid(); return row; },
			getBlankColumn: function () { var col = angular.copy(blankColumn); col.id = GuidService.guid(); return col; },
			getBlankWidget: function () { var widget = angular.copy(blankWidget); widget.id = GuidService.guid(); return widget; },
			// getBlankComponent: function () { return angular.copy(blankComponent); },
			getFormattedRow: function (format) {
				var colFormat;
				switch (format) {
					case 'COL1':
						colFormat = [1];
						break;
					case 'COL2':
						colFormat = [1,1];
						break;
					case 'COL3':
						colFormat = [1,1,1];
						break;
					case 'COL12':
						colFormat = [1,2];
						break;
					case 'COL21':
						colFormat = [2,1];
						break;
				}
				var row = angular.copy(blankRow);
				row.id = GuidService.guid();
				row.format = format;
				for (var i = 0, len = colFormat.length; i<len; i++) {
					var c = angular.copy(blankColumn);
					c.id = GuidService.guid();
					c.span = colFormat[i];
					row.cols.push(c);
				}
				return row;
			}
		};
	}];
});