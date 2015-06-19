angular.module(PKG.name+'.feature.foo')
  .config(function ($stateProvider, MYAUTH_ROLE) {


    /**
     * State Configurations
     */
    $stateProvider

      .state('foo', {
        url: '/foo',
        templateUrl: '/assets/features/foo/foo.html'
      })
      .state('test-edwin', {
        url: '/test/edwin',
        templateUrl: '/assets/features/foo/edwin.html',
        controller: function ($scope, $timeout) {
          $scope.timeline = [
            { name: 'Test1', start: 1434589511000, end: 1434590291000 },
            { name: 'Test2', start: 1434589691000, end: 1434590591000 },
            { name: 'Test3', start: 1434589931000, end: 1434590351000 },
            { name: 'Test4', start: 1434589800000, end: 1434590700000},
            { name: 'Test5', start: 1434589300000, end: 1434590351000 },
            { name: 'Test6', start: 1434590351000, end: 1434592051000 }
          ];
        }
      })

      .state('test-settings', {
        url: '/test/settings',
        templateUrl: '/assets/features/foo/settings.html',
        controller: 'FooPlaygroundController'
      });

  });
