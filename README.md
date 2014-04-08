![SocialAuth Library](https://raw.github.com/wiki/3pillarlabs/socialauth/images/java.png)

Socialauth
================

SocialAuth is a Java library ([.NET port](http://code.google.com/p/socialauth-net/) & [Android](http://code.google.com/p/socialauth-android/) version available) for you if your web application requires: 

* Authenticating users through external oAuth providers like Gmail, Hotmail, Yahoo, Twitter, Facebook, LinkedIn, Foursquare, MySpace, Salesforce, Yammer, Google Plus, Instagram as well as through OpenID providers like myopenid.com. 
* Easy user registration. All you need to do is create a page where users can click on buttons for the above providers or other supported providers. Just call SocialAuth and you can get all their profile details. 
* Importing contacts from networking sites.

See our [SoicalAuth demo in action](http://labs.3pillarglobal.com/socialauthdemo) !

Download [socialauth-java-sdk-4.5.zip](https://sourceforge.net/projects/socialauth/files/latest/download)

![Build Status](http://labs.3pillarglobal.com/jenkins/job/socialauth-core/badge/icon)

Whats new in Version 4.5?
=========================
* Updated Yahoo API
* Updated Foursquare API
* Serialized SocialAuthTemplate object in socialauth-spring.

Whats new in Version 4.4?
=========================
* Updated Twitter API
* Changes in command line utility to generate access token
* Added !Nimble Provider

Whats new in Version 4.3?
=========================
* Added !GitHub Provider
* Added Flickr Provider
* Added !GooglePlus Feed Plugin
* Added !GooglePlus Album Plugin
* Added Facebook wall demo
* Jackson support in Command line utility to generate and save access token

Whats new in Version 4.2?
=========================

* Refresh token functionality for Facebook
* Added GooglePlus provider
* Added Instagram provider
* Twitter API v1.1 implemented
* Command line utility to generate and save access token
* All core projects are maven-ise now and jars are available on maven repository. 
* All demos except seam and grails are maven-ise now.
* OAuth endpoint (RequestToken URL, Authorization URL and AccessToken URL) can be configured through properties file now.
* Response object returned by UpdateStatus method.
* Bug fixes
