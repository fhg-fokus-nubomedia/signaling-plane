(function() {
	angular.module("App.Controllers", [ "App.Model" ])

	.controller("AppController", function($scope, model) {
		$scope.refresh = function() {
			model.refreshAllModules();
		}

		/* Whenever Server is connected, refresh all the data */
		$scope.$on("server-open", function(e, data) {
			model.loadData();
		});
	})

	.controller("DeploymentsController", function($rootScope, $scope, model) {
		$scope.deploymentMap = {};

		function loadModules(m) {
			for (i in m) {
				loadModule(m[i]);
			}
		}
		function loadModule(m) {
			entry = {
				"module_name" : m.module_name,
				"repository_address" : m.repository_address,
				"module_config" : m.module_config,
				"deployments" : []
			};
			
			console.log(m);

			$scope.deploymentMap[m.module_name] = entry;
		}
		function unloadModule(m) {
			delete $scope.deploymentMap[m.module_name];
		}

		function loadDeployments(d) {
			for (i in d) {
				addDeployment(d[i]);
			}
		}
		function addDeployment(d) {
			var entry = $scope.deploymentMap[d.module.module_name];
			console.log("Loaded Entry");
			console.log(entry);
			entry.deployments.push({
				"deployment_id" : d.deploymet_id,
				"module" : d.module
			});
		}
		function removeDeployment(d) {
			var entry = $scope.deploymentMap[d.module.module_name];
			for (i in entry.deployments) {
				var d = entry.deployments[i];
				if (d.deployment_id === d.deployment_id) {
					entry.deployments.splice(i, 1);
					return;
				}
			}

		}
		
		function getUploadUrl(){
			
			return uploadUrl; //"http://localhost:8080/upload/";
		}
		
		var uploadUrl;
		function setUploadUrl(url){
			console.log("Setting upload url to "+url);
			uploadUrl = url;
		}
		
		$scope.setSelectedDeployment = function (deployments, selectionElement){
			console.log("called getDeployments("+deployments+")");
	
			selectionElement.value = "helloTest";	
		}
		
		$scope.setSelectedDeployment = function (deployment, selectionElement){
			console.log("called setSelectedDeployment");
			console.log(deployment+"");
		}

		/* Outlet Actions */
		$scope.deploy = function(module, instances) {
			console.log("Called deploy");
			console.log(module);
			if (instances > 0) {
				var configString = document.getElementById("config").value;
				if (configString) {
					console.log("Setting configuration to " + configString);
					module.module_config = JSON.parse(configString);
				}

				model.deployModule(module, instances);
			} else {
				model.undeployModule(module, 0 - instances);
			}
		}

		$scope.loadConfigurationFromModule = function(module) {
			console.log("Called loadConfigurationFromModule");
			console.log(module);

			if (module.module_config) {
				document.getElementById("config").value = JSON.stringify(module.module_config);
			} else
				document.getElementById("config").value = "";
		}
		
		$scope.loadConfigurationFromDeployment = function(deployment) {
			console.log("Called loadConfigurationFromDeployment");
			console.log(deployment);

			if (deployment.module.module_config) {
				document.getElementById("config").value = deployment.module.module_config;
			} else
				document.getElementById("config").value = "";
		}

		$scope.upload = function() {
			console.log("Called upload");

			var file = document.getElementById("fileupload").files[0];
			var config = document.getElementById("config").value
			var url = getUploadUrl();
			
			if(!url){
				console.log("Cannot find URL of upload server");
				return;
			}
			
			console.log(file);
			console.log(config);
			
			var client = new XMLHttpRequest();
			
			if(file){
				if(config){
					//upload config file
					var configUrl = url + file.name+".json";
					client.open("PUT", configUrl, false);
					client.send(config);
				}
				
				//upload zip file
				var zipUrl = url + file.name;
				client.open("PUT", zipUrl, true);
				
				//var formData = new FormData();
				//formData.append(file.name, file);
				client.send(file);
			}
			else console.log("No file for upload selected");
		}

		/* Load all the relevant data when loaded */
		$scope.$on("data-loaded", function(e, data) {
			$scope.deploymentList = [];
			$scope.deploymentMap = {};

			loadModules(data.modules);
			loadDeployments(data.deployments);
			setUploadUrl(data.upload_url);
			$scope.$digest();
		});

		$scope.$on("module-installed", function(e, module) {
			loadModule(module);

			$scope.$digest();
		});
		$scope.$on("module-uninstalled", function(e, module) {
			unloadModule(module);

			$scope.$digest();
		});

		$scope.$on("module-deployed", function(e, deployment) {
			addDeployment(deployment);

			$scope.$digest();
		});
		$scope.$on("module-undeployed", function(e, deployment) {
			removeDeployment(deployment);

			$scope.$digest();
		});

	})

	.controller("EventsController", function($scope) {
		$scope.events = [];

		function pushEvent(text) {
			$scope.events.push({
				text : text,
				datetime : new Date()
			});

			$scope.$digest();
		}

		$scope.$on("data-loaded", function(e) {
			pushEvent("Data Loaded");
		});

		$scope.$on("module-installed", function(e, module) {
			pushEvent('Module Installed: "' + module.module_name + '"');
		});

		$scope.$on("module-uninstalled", function(e, module) {
			pushEvent('Module Uninstalled: "' + module.module_name + '"');
		});

		$scope.$on("module-modified", function(e, module) {
			pushEvent('Module Modified: "' + module.module_name + '"');
		});

		$scope.$on("module-deployed", function(e, module) {
			pushEvent('Module Deployed: "' + module.module_name + '"');
		});

		$scope.$on("module-undeployed", function(e, module) {
			pushEvent('Module Undeployed: "' + module.module_name + '"');
		});
	})

	.run(function(model) {
		model.connect();
	});

})();