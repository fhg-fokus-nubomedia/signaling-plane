#CDN Connector

The CDN Connector implements third party APIs of video CDN provider. This allows NUBOMEDIA clients to access exteral video services though their NUBOMEDIA user agents. The module is implemented as OpenXSP module and therefore uses the event bus of the Signaling Plane in order to receive client requests.

##Use Cases


 * **Upload a file to CDN**  
Uploading a video file to the specific CDN which has been recorded before. This allows a user to upload their video call session to a CDN and publish this video content to users connecting to this CDN.

 * **Broadcast video stream**  
Streaming the video to a CDN allows other users of the CDN to follow the live stream of a NUBOMEDIA video call. This allows NUBOMEDIA to use the infrastructure of the CDN to send live broadcasts to non-NUBOMEDIA user agents.

 * **Play video**  
Playing video file stored at the CDN allows NUBOMEDIA users to access videos content stored on a CDN.

 * **Discover videos stored on a CDN**  
Allows a user to discover the list of uploaded videos

 * **Delete Videos**  
Allows a user to delete previously uploaded videos


##Interface description

The CDN interface is based on JSON documents that are exchanged over the event bus between the user agent and the module. For each of the API services it registers a handler for a specific address. This address needs to be known by user agents that want to use the API.  

The CDN Connector module reuses the session description protocol developed in T3.4 for the session-related services.

###Upload Video
User agents that want to use this service need to send the inital session request to the following address: *nubomedia.cdn.fileupload*  
The message flow follows the session establishment procedure described in the event bus protocol for session establishment of T3.4.

####Message flow between user agent and module
First an **session initiation message** is sent by the user agent to the CDN connector. This request contains the information of the CDN to be used and the media session information that is used for the video call between the user agent and the Kurento media server:  
	
    {
        "from": <username>,
        "to":  <any_name>,
		"method": "init",
		"addess": <callback_adress_of_client>,
		"session_id": <sessionid>,
        "content":{
            "sdp":      <client sdp string>,
            "cdn_config": {
				"name": <name of the cdn>,
				<specific configuration parameters of the CDN>
			}
        },
		"content_type": "application/json"
    }

The cdn_config parameter of the content contains the name of the CDN as identifier and specific information like credentials, channel names etc. that are required by the CDN in order to user their 3rd party APIs.  
Following the three way handshake procedure of the event bus protocol, the CDN module will send a **session acknoledgement** response:  

    {
        "session_id": <sessionid>,
		"method":"accept",
		"address":"nubomedia.cdn.fileupload",
        "content":<sdp string of the media server>,
        "content_type": "application/sdp"
    }


As last part of the three way handshake, the client sends an empty **session confirm** messaage to the callback address of the CDN connector:

    {
        "session_id": <sessionid>,
		"method":"confirm",
		"address": <callback_adress_of_client>
    }

Now the session is established and the media can be sent between media server and user agent. At some point the client stops the video call session with an end session request:
    {
        "session_id": <sessionid>,
		"method":"end",
		"address": <callback_adress_of_client>
    }

This stops the media transfer and triggers the file upload to the CDN: The file that has been recorded by the media server is uploaded to the NUBOMEDIA cloud repository and from there downloaded by the CDN connector module. After downloading the file, the file will be uploaded to the CDN. When this upload is finished, the CDN Connector module will send a **confirm message** to the user agent which contains information about the uploaded video:

    {
        "session_id": <sessionid>,
		"method": "confirm",
		"address": "nubomedia.cdn.fileupload",
		"content": {
			"id": <ID of the uploaded video>,
			"meta_info": <meta information returned by the CDN about the uploaded video
		}
		"content_type": "application/json"
    }


###Play Video

As with the video upload, the video playback service endpoint uses the event bus protocol for session establishment. The video call establishment is triggered by a **session initiation** request from the user agent to the address of the download service event *nubomedia.cdn.download*:

####Message Flow between user agent and module


    {
        "from": <username>,
        "to":  <any_name>,
		"method": "init",
		"addess": <callback_adress_of_client>,
		"session_id": <sessionid>,
        "content":{
            "sdp":      <client sdp string>,
            "cdn_config": {
				"name": <name of the cdn>,
				"id": <id of the video>,
				<specific configuration parameters of the CDN>
			}
        },
		"content_type": "application/json"
    }

The cdn_config parameter of the content contains the id of the video, the name of the CDN and specific information like credentials, channel names etc. that are required by the CDN in order to use their 3rd party APIs.  
Following the three way handshake procedure of the event bus protocol, the CDN module will send a **session acknoledgement** response:  

    {
        "session_id": <sessionid>,
		"method":"accept",
		"address":"nubomedia.cdn.download",
        "content":<sdp string of the media server>,
        "content_type": "application/sdp"
    }


As last part of the three way handshake, the client sends an empty **session confirm** messaage to the callback address of the CDN connector:

    {
        "session_id": <sessionid>,
		"method":"confirm",
		"address": <callback_adress_of_client>
    }

Now the session is established and the media can be sent between media server and user agent. At some point the client stops the video call session with an end session request:

    {
        "session_id": <sessionid>,
		"method":"end",
		"address": <callback_adress_of_client>
    }



####Messages

###Live Broadcast Video
The live broadcast video service endpoint receives events for the event *nubomedia.cdn.upstream*
####Message Flow flow between user agent and module

The message structure is the same as for the upload video service endpoint (of cause with the corresponding event addresses of the live broadcast event address). However, the last message for the confirmation of the session end request will look different with only CDN specific information:


    {
        "session_id": <sessionid>,
		"method": "confirm",
		"address": "nubomedia.cdn.upstream",
		"content": {
			"name":<id of the CDN>,
			<CDN specific information>
		}
		"content_type": "application/json"
    }


###Delete Video

The delete video service endpoint receives events for the event *nubomedia.cdn.delete*
####Message flow between user agent and module
The expected request of the user agent is a JSON object of the following structure:

    {
        "from": <username>,
        "to":  <any_name>,
		"method": "message",
		"addess": <callback_adress_of_client>,
        "content":{
            "cdn_config": {
				"name": <name of the cdn>,
				"id": <id of the video to be deleted>,
				<specific configuration parameters of the CDN>
			}
        },
		"content_type": "application/json"
    }

The reply message will be  

    {
        "status": "ok"
    }
   
if the operation was successful or  

    {
        "error": <error message>
    }

in case of an error


###Video Discovery
The video discovery service endpoint receives events for the event *nubomedia.cdn.getvideos*
####Message flow between user agent and module
The expected request of the user agent is a JSON object of the following structure:

    {
        "from": <username>,
        "to":  <any_name>,
		"method": "message",
		"addess": <callback_adress_of_client>,
        "content":{
            "cdn_config": {
				"name": <name of the cdn>,
				<specific configuration parameters of the CDN>
			}
        },
		"content_type": "application/json"
    }

The reply message will be

    {
        "videos": [
		{
			"id": <id of the video>,
			"title": <title of the video>,
			"upload_date": <time stamp of the upload time in rfc3339 format>,
			"meta_data": <meta information provided by the CDN>
		},
		{
			"id": <id of the video>,
			"title": <title of the video>,
			"upload_date": <time stamp of the upload time in rfc3339 format>,
			"meta_data": <meta information provided by the CDN>
		},
		...
		]
    }
   
if the operation was successful or

    {
        "error": <error message>
    }

in case of an error.



##Configuration

The configuration of the CDN Connector module requires configuration parameter for the Cloudrepository that will be used for storing media files (in case of video upload or play video requests). This will be the same repository where the media server will store the media files.

The generic structure of the configuration looks like this:

    {
        "kurento_address": <the websocket URI of the kurento server that should be used>,
		"repositroy_config": <configuration parameter of the repository to be used>
    }

###Repository Configuration
There might be different configuration parameters in the future according to the type of cloud repository that will be used. Currently only an implementation of a MongoDB FileFS repository is available. Therefore the configuration may look like this:

    {
		"repositroy_config": {
			"ip": <the IP address of the cloud repository>,
			"port": <the IP port where the coud repository is reachable> ,
			"user": <the user name>,
			"password": <the password of the user>,
			"database_name": (optional) <the name of the database that should be used; default is "admin">,
			"collection_name": (optional) <the nameof the collection where the files are stored; default value is "gridfs">,
			"downloadfolder":  (optional) <the folder where downloaded files will be stored; default is "RepositoryDownloads">
		}
    }

##Youtube Connector


###Prerequisites
When web client wants to make use of the Youtube connector, some prerequisites must be fulfilled. This mainly relates to the creation of a Youtube account and a specific project associated with that account and the authorization to be able to make API requests.

 1. **Create a Youtube account and a channel**  
Registering an account for Youtube is quite easy: visit google.com an create a new user account. With the Google account a user can make use of the Youtube services. Create a channel for your account at https://www.youtube.com/channel (while logged in).

 2. **Register a project**  
Visit https://console.developers.google.com (while you are logged in) and create a new project. On the left menu, go to "APIs & auth" -> "APIs" and enable the YouTube Data API

 3. **OAuth configuration**  
Youtube uses OAuth 2.0 to authorize API requests. Therefore you need to configure credentials that will be used by your web application (aka NUBOMEDIA webrtc client) to retrieve an access token. The access token and other parameter will be sent with the client requests to the connector so that the Youtube connector is able to make Youtube API requests.  
In the left menu (same page like in step 2) go to "APIs & auth" -> "Credentials". Click on "Create new Client ID" -> "Web application". Then new credentials will be generated. 

###Request structure

As already explained, the requests of the CDN connector require a specific part for the CDN configuration:

		"cdn_config": {
			...
			<specific configuration parameters of the CDN>
		}

These specific parameters are as follows for the Youtube connector:  

 * Project name  
The name of the project created in step 1 of the Prerequisies paragraph

 * Access token  
This toked needs to be generated by the client using the OAuth credetials, the user account credentials and the Authorization server of Youtube.

 * Client ID  
The client ID which is part of the OAuth configurarion parameters in part 3 of the Prerequisites paragraph

Summing up, the structure of specific configuration parameter for the Youtube connector must look like this:

		"cdn_config": {
			...
			"application_name": <this is the project name>,
			"auth":{
				"access_token": <the access token>,
				"client_id": <the cliend ID>
			}
		}

####Example Project
There is already an example of a web application for testing available. This web application is based on the JavaScript examples located at https://developers.google.com/youtube/v3/code_samples/javascript and implements the OAuth API. It shows how a web application can get an access token (and how it can upload video files to youtube, but this part is not interesting for us). The Web application is located in src/main/resources/webapp. In order to run a server with the webapp, have a look at the src/test/org/openxsp/cdn/test/youtube/UploadTest.java file. This class can be run as Unit Test which will start an HTTP server with this web application. Running the class as regular Java application will start a test which tests upload, delete and video discovery, but before the access token needs to be copied from the web console of the web application into the src/test/org/openxsp/cdn/test/youtubeAuthTest.java class (also client id and application name need to be adopted here to run the test).

###Limitations
The Youtube connector only supports a subset of the CDN Connector services, this is partly due to the reason that video downloads are not supported by the Youtube API and that for live video broadcasting there is still some work for transcoding required. This means the Youtube connector currently supports only video upload, video discovery and deleting videos.

##Source Code Location
As the CDN Connector is implemented as application function module inside the Signaling Plane, the source code is included into the repository of the Signalin Plane at modules/cdn_connector. For the location of the Signaling Plane source code, have a look at the documentation of the Signaling Plane.

##License
The CDN Connector module is published under the Apache 2.0 license.
