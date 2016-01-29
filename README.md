# signaling-plane
This project is part of the [NUBOMEDIA](http://www.nubomedia.eu/) research initiative.


## Getting Started
Here are the first steps to get you started. But before we go there, here are some prerequisites. Make sure you have the following installed on your operating system (we have developed and tested so far only on Debian (Ubuntu 14).

* Java from version 7 ```sudo apt-get install openjdk-7-jdk```
* Git ```sudo apt-get install git```
* Python ```sudo apt-get install python```

### 1 Setup and Installation
Once you got the prerequisites in place, go ahead and clone the git repository of the signaling plane with this command:

``` git clone http://80.96.122.50/alice.cheambe/signaling-plane.git ```

Next, you need to include it on your system's variables and path so you can run the commands from the command line. For that, set the following two variables.
* *$OPENXSP_HOME* should be the root directory where the sources from git was downloaded. (```export OPENXSP_HOME = $pwd```)
* *PATH* should include $OPENXSP_HOME (```export PATH=$PATH:$OPENXSP_HOME```)


### 2 Compiling and Running modules
This signaling plane currently contains the following modules:

* Deployment Manager(alpha): This is a draft version of what is called the PaaS API. Developers will later use this module to deploy NUBOMEDIA instances. For now, you can use it to deploy and run already developed modules (applications) 
* Kurento_client: Application Function to interact with the Media Plane (Kurento Server)
* java_sip: Alpha version of the IMS Connector
* lib-sessioncontrol: is a module implementing the Event Bus Protocol for session creation. Design is generic and can be mapped to any protocol by providing protocol adapters.
* lib-sip contains the SIP Stack for creating SIP based sessions. 
* nubo_user_registry: Every client connecting to the Event BUS must register it self on this registry in order to be reachable from the Event Bus. [More Information...](http://80.96.122.50/alice.cheambe/signaling-plane/tree/master/modules/nubo_user_registry)
* cdn_connector: Alpha implementation of using YouTube to publish videos recorded from the multimedia sessions with the Kurento server.


#### 2.1 Building the modules ####
The build process uses gradle as build tool. Run the script compile_modules to build all the modules.
```
./compile_modules
```

#### 2.2 Configuration ####
The start script uses a JSON configuration file ``` nubomedia_signaling_plane.json```. This file contains the modules to be executed and their configuration.
The IMS connector module needs to be configured with the local IP and port where is listens for SIP requests. This is done with the "bind" and "port" parameter. Secondly, the IMS parameters need to be configured.

#### 2.3 Running the modules ####
In order to run the modules, run the script 
```
/.nubomedia_signaling_plane

```

Issue tracker
-------------

Issues and bug reports should be posted to the [GitHub Issue List](https://github.com/fhg-fokus-nubomedia/signaling-plane/issues)

Licensing and distribution
--------------------------

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

Support
-------

Need some support, then get in contact with us!
