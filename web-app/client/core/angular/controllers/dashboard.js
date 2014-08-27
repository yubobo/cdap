'use strict';

define(['helpers'], function (helpers) {

  /* Items */

  var Ctrl = ['$scope', 'metricsService', '$interval', 'POLLING_INTERVAL',
    function($scope, metricsService, $interval, POLLING_INTERVAL) {

    $scope.projectId1 = "ef61c02f-1d8d-4830-8160-c9e7b5997644";
    $scope.projectId2 = "b687baaf-7833-4e46-49e3-33b07a4aea73";

    if (!localStorage.getItem('projects')) {
      localStorage.setItem('projects', '[{"id":"15455904-dc72-43e0-919a-f2a494bcc2e3","name":"asdf","tabs":[{"id":"f67bb7f1-ce0a-40b1-b0a6-fbfc8fbce07e","order":1,"name":"Overview","layout":{"rows":[{"format":"COL1","cols":[{"span":1,"widgets":[{"title":"asdf","endpoint":"http://localhost:8080/api/pie-chart","component":"dashboard.components.charting.dashboardPieChart","id":"8c31dc64-64fa-4d62-be34-b96b23937366"}],"id":"a7b369c2-0f44-4c24-a5e2-becbc62b387d","$$hashKey":"01W"}],"id":"29d10dc9-03d2-4b46-c33a-4147a356cfc9","$$hashKey":"01S"}]}}]},{"id":"b687baaf-7833-4e46-49e3-33b07a4aea73","name":"Test Project","tabs":[{"id":"dcab4b29-1710-4420-6d53-83dbd1763c79","order":1,"name":"Overview","layout":{"rows":[{"format":"COL2","cols":[{"span":1,"widgets":[{"title":"line","endpoint":"http://localhost:8080/api/line-chart-feed","component":"dashboard.components.charting.dashboardLineChart","id":"3366f613-9087-478c-ff2f-db7a46608792","$$hashKey":"01H"}],"id":"cd2d7791-be26-4b7b-f740-a27aab2aae37","$$hashKey":"01D"},{"span":1,"widgets":[{"title":"pie","endpoint":"http://localhost:8080/api/pie-chart","component":"dashboard.components.charting.dashboardPieChart","id":"9fc70231-981e-4f5f-b423-3ad86612bb3a","$$hashKey":"01J"}],"id":"7390ea23-8c7b-498e-0a2e-c80ff985a62c","$$hashKey":"01E"}],"id":"b340bc1c-3c96-4c34-4e8d-14e01ec7e039","$$hashKey":"00F"},{"format":"COL2","cols":[{"span":1,"widgets":[{"title":"bar","endpoint":"http://localhost:8080/api/bar-chart","component":"dashboard.components.charting.dashboardBarChart","id":"c1393e88-9f68-44fd-0de9-fa8cc6ec1947","$$hashKey":"01P"}],"id":"fd31ba83-473b-48e5-88b8-fd6949e7582f","$$hashKey":"01L"},{"span":1,"widgets":[{"title":"test custom","endpoint":"http://","component":"dashboard.components.custom.testComponent","id":"cec26707-344d-448e-bf1c-41ef79d8253a","$$hashKey":"01R"},{"title":"table","endpoint":"http://localhost:8080/api/data-table","component":"dashboard.components.utilities.dashboardTable","id":"8711eb9f-85e7-40f3-b8ef-5fcd5b3d97d3"}],"id":"876acdc0-5b81-4ac3-d662-99285bee01e2","$$hashKey":"01M"}],"id":"44fed9e3-973f-4049-036b-f9d5bda8ba50","$$hashKey":"00G"}]}},{"id":"a274d831-aedc-481a-8484-f629c97bb93d","order":2,"name":"more","layout":{"rows":[{"format":"COL1","cols":[{"span":1,"widgets":[{"title":"Table2","endpoint":"http://localhost:8080/api/data-table","component":"dashboard.components.utilities.dashboardTable","id":"4a20237a-5be5-45ea-d6a8-080c9c668ee4"}],"id":"018dca76-7124-4131-bc43-efb53b12a2a8","$$hashKey":"0LU"}],"id":"40cf07af-8461-411e-6fae-97ebdc2e59c9"}]}}]},{"id":"ef61c02f-1d8d-4830-8160-c9e7b5997644","name":"asdfsaf","tabs":[{"id":"de66af9f-e241-4f29-52ad-9231129e08a1","order":1,"name":"Overview","layout":{"rows":[{"format":"COL3","cols":[{"span":1,"widgets":[{"title":"line chart","endpoint":"http://localhost:8080/api/line-chart-feed","component":"dashboard.components.charting.dashboardLineChart","id":"2e4216be-5ce4-474b-31c1-81ad2b6c8c14"}],"id":"5f5a2d28-4ebe-4949-b569-0536260b789f","$$hashKey":"00F"},{"span":1,"widgets":[{"title":"ppie","endpoint":"http://localhost:8080/api/pie-chart","component":"dashboard.components.charting.dashboardPieChart","id":"3e512101-d9c1-4125-7a8b-00c79e929389"}],"id":"d7da613b-708a-4056-7507-4a2851c9d0d6","$$hashKey":"00G"},{"span":1,"widgets":[{"title":"Progress","endpoint":"http://localhost:8080/api/progress","component":"dashboard.components.utilities.dashboardProgressBar","id":"648e948f-0f38-475a-1362-a6ef8ef693c1"}],"id":"91e3763d-e0ca-4538-a98e-b816099c1b70","$$hashKey":"00H"}],"id":"8e15bd97-ded4-46f3-af51-03b1adf34f1a","$$hashKey":"009"},{"format":"COL2","cols":[{"span":1,"widgets":[{"title":"Sample bar","endpoint":"http://localhost:8080/api/bar-chart","component":"dashboard.components.charting.dashboardBarChart","id":"bc1388cf-73df-4e6a-0072-098161010b07"}],"id":"8147ed99-9a04-4b9c-a2d4-b21f442546af","$$hashKey":"037"},{"span":1,"widgets":[{"title":"Line 2","endpoint":"http://localhost:8080/api/line-chart-feed","component":"dashboard.components.charting.dashboardLineChart","id":"250f3c29-34de-4aae-0b21-8e27b0e193e3"}],"id":"644ffea4-141b-4623-9af6-0c99602e36f1","$$hashKey":"038"}],"id":"3d1f0152-4f1d-4055-5a8a-95c85206e809"}]}}]}]');
    }

    /**
     * Gets triggered on every route change, cancel all activated intervals.
     */
    $scope.$on("$destroy", function() {
      if (typeof intervals !== 'undefined') {
        helpers.cancelAllIntervals($interval, intervals);
      }
    });
  }];

  return Ctrl;

});