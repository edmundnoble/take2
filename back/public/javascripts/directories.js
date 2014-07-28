var ROOTURL = "http://0.0.0.0:9000/"

frostbite.filter('isDirectory', function() {
    return function(input) {
        if (input.isDir) {
	    return ROOTURL + "assets/images/directory.png"
	} else {
	    return ROOTURL + "assets/images/file.png"
	}	    
    }
})

.controller('DirecCtrl', ['$scope', '$http', function($scope, $http) {
    $scope.filename = "";

    $scope.items = []
    

    $scope.setFilename = function(filename) {
	console.log("filename = " + filename);
	$scope.items = filename;
	$scope.pathArray = $scope.items[0].path.split("/");
	$scope.pathArray.pop();
	for (var i = 0; i < $scope.items.length; i++) {
            $scope.items[i].editedlast  = {"id":7, "time":new Date()};
        }
	console.log($scope.items);
        $scope.getUsers();
	//$scope.getDirectories();
    }

    $scope.getDirectories = function() {
	console.log("Getting data");
	console.log("repo/" + $scope.filename);
	$http.get("/repo/" + $scope.filename).success(function(data) {
	     console.log("RESPONSE");
	     console.log(data);
	     $scope.items = data;
	     //TODO: switch to real date.. or just remove
	     for (var i = 0; i < $scope.items.length; i++) {
                 $scope.items[i].editedlast = {"id":7, "time":new Date()};
             }
             $scope.getUsers();
	});	
    }

    $scope.getUsers = function() {
	console.log("Getting users");
	for (var i = 0; i < $scope.items.length; i++) {
            // TODO: switch to actual number of users
	    (function(i) {
	        $http.get("/api/users/" + $scope.items[i].path).success(function(data) {
		    console.log("data: " + data);
		    console.log("I: " + i);
                    console.log($scope.items[i]);
		    $scope.items[i].users = data;
                    var numUsers = $scope.items[i].users.length;
                    if (numUsers >= 3) {
		        $scope.items[i].currentUsers = $scope.items[i].users[0].name + " and " + (numUsers - 1) + " Others";
	            } else if (numUsers == 2){
		        $scope.items[i].currentUsers = $scope.items[i].users[0].name + " and " + $scope.items[i].users[1].name;
	            } else if (numUsers == 1) {
		        $scope.items[i].currentUsers = $scope.items[i].users[0].name;
                    } else {
		        $scope.items[i].currentUsers = "";
	            }
	    });
            }(i));
	}
    }
}])
