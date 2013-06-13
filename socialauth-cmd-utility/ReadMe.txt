Instructions for GenerateToken Command-Line Utility

Prerequisites
-------------

1) Register your applications on required provider to get client id and client secret.
2) Copy these client id and secret in oauth_consumer.properties file. 
	For more info, please visit :: http://code.google.com/p/socialauth/wiki/SampleProperties
3) Place oauth_consumer.properties file in a jar classpath.


Usage
----------

java GenerateToken --providerId=[providerId] --host=[hostname] --port=[port] --returnURL=[returnURL] --tokenFileLocation=[tokenFileLocation]

Here 
	providerId is the id of provider like facebook etc.
	host is the host of your server. This will be the same which will you use in returnURL
	port is the port of server. This will be the same which will you use in returnURL. This is optional and default is 80 if not givrn.
	tokenFileLocation is the location where access token file will be saved. This is optional. If it is not given then files will be saved in user home directory.
	
for example
	java GenerateToken --providerId=facebook --host=opensource.brickred.com --port=80 --returnURL=http://opensource.brickred.com/socialauthdemo/socialAuthSuccessAction.do";
	
	
Note
-------
If you are running this utility on the same machine where your server is running then please shutdown your server first.
	

