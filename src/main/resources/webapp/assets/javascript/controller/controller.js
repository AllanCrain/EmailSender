'use strict'
emailApp.controller('controller', function ($scope, $http, $location) {
    $scope.sendEmail = function () {
        if ($scope.to === undefined) {
            $scope.errorMessage = "To is required";
            return;
        }
        var data = {
            from: $scope.from,
            to: $scope.to.split(","),
            subject: $scope.subject,
            body: $scope.message
        };
        var res = $http.post('http://localhost:8080/api/send', angular.toJson(data));
        res.success(function (data, status, headers, config) {
            if (status == 200) {
                $scope.isSuccess = true;
                $scope.response = data.message;
            }
        });
        res.error(function (response, status, headers, config) {
            $scope.isSuccess = false;
            $scope.response = response.message;
        });
    }

});
