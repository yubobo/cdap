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
            { name: 'Test1', start: 1434589511, end: 1434590291 },
            { name: 'Test2', start: 1434589691, end: 1434590591 },
            { name: 'Test3', start: 1434589931, end: 1434590351 },
            { name: 'Test4', start: 1434589800, end: 1434590700 },
            { name: 'Test5', start: 1434589300, end: 1434590351 },
            { name: 'Test6', start: 1434590351, end: 1434591051 }
          ];
        }
      })

      .state('test-settings', {
        url: '/test/settings',
        templateUrl: '/assets/features/foo/settings.html',
        controller: 'FooPlaygroundController'
      });

  });
