'use strict'
var emailApp = angular.module('emailApp', ['ngRoute']);

emailApp.config(function($routeProvider) {
    $routeProvider
        .when('/', {
            templateUrl: 'assets/template/main.html',
            controller: 'controller'
        });

});

