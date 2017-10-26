var filesutraApp = angular.module("filesutraApp", [
        'ngRoute',
  'filesutraControllers'
]);

filesutraApp.config(['$locationProvider', function(e) {
  e.html5Mode({
    enabled: false,
    requireBase: true
  });
}]);
