<html>
<%@page import="de.deltatree.social.web.filter.api.SASFStaticHelper"%>
<%@page import="de.deltatree.social.web.filter.api.SASFHelper"%>
<%@page import="org.brickred.socialauth.SocialAuthManager"%>
<%@page import="org.codehaus.groovy.grails.web.pages.ext.jsp.GroovyPagesPageContext" %>
<head>
    <title>BrickRed SocialAuth Demo </title>
    <style type="text/css">
        .style1{text-align: justify;}
    </style>
	<script>
		function validate(obj){
			var val = obj.id.value;
			if(trimString(val).length <= 0){
				alert("Please enter OpenID URL");
				return false;
			}else{
				return true;
			}
		}
		function trimString(tempStr)
		{
		   return tempStr.replace(/^\s*|\s*$/g,"");
		}
	</script>
</head>
<body>
    <%
		SASFHelper helper = SASFStaticHelper.getHelper(request);
		SocialAuthManager socialAuthManager;
		if(helper!=null){
			socialAuthManager = helper.getAuthManager();
			if(socialAuthManager != null){
				GroovyPagesPageContext pageContext = new GroovyPagesPageContext(pageScope);
				pageContext.setAttribute("socialAuthManager",socialAuthManager);
			}
		}
	%>
    <g:set var="facebook" value="false" />
	<g:set var="twitter" value="false" />
	<g:set var="google" value="false" />
	<g:set var="yahoo" value="false" />
	<g:set var="hotmail" value="false" />
	<g:set var="linkedin" value="false" />
	<g:set var="foursquare" value="false" />
	<g:set var="myspace" value="false" />
	<g:if test="${socialAuthManager != null }">
		<g:each var="item" in="${socialAuthManager.connectedProvidersIds}">
		 	<g:if test="${'facebook'.equals(item)}">
		    	<g:set var="facebook" value="true" />
		  	</g:if>
		  	<g:if test="${'google'.equals(item)}">
		    	<g:set var="google" value="true" />
		  	</g:if>
		  	<g:if test="${'twitter'.equals(item)}">
		    	<g:set var="twitter" value="true" />
		  	</g:if>
		  	<g:if test="${'yahoo'.equals(item)}">
		    	<g:set var="yahoo" value="true" />
		  	</g:if>
		  	<g:if test="${'hotmail'.equals(item)}">
		    	<g:set var="hotmail" value="true" />
		  	</g:if>
		  	<g:if test="${'linkedin'.equals(item)}">
		    	<g:set var="linkedin" value="true" />
		  	</g:if>
		  	<g:if test="${'myspace'.equals(item)}">
		    	<g:set var="myspace" value="true" />
		  	</g:if>
		  	<g:if test="${'foursquare'.equals(item)}">
		    	<g:set var="foursquare" value="true" />
		  	</g:if>
		</g:each>
	</g:if>
    <div id="main">
        <div id="text" >
	       	<table cellpadding="10" cellspacing="10" align="center">
				<tr><td colspan="8"><h3 align="center">Welcome to Social Auth Demo</h3></td></tr>
				<tr><td colspan="8"><p align="center">Please click on any icon.</p></td></tr>
				<g:if test="${flash.message}">
					<tr><td colspan="8">
		        		<div>${flash.message}</div>
		        	</td></tr>
		       	</g:if>
				<tr>
					<td>
						<a href="socialAuth?id=facebook">
						 <img src="images/facebook_icon.png" alt="Facebook" title="Facebook" border="0"/>
						</a>
						<br/><br/>
						<g:if test="${facebook.equals('true')}">
							<a href="socialAuth/signout?id=facebook&mode=signout">Signout</a><br/>
						</g:if>
						<g:if test="${facebook.equals('false')}">
							<a href="socialAuth?id=facebook">Signin</a><br/>
						</g:if>
					</td>
					<td>
						<a href="socialAuth?id=twitter">
						  <img src="images/twitter_icon.png" alt="Twitter" title="Twitter" border="0"/>
						</a>
						<br/><br/>
						<g:if test="${twitter.equals('true')}">
							<a href="socialAuth/signout?id=twitter&mode=signout">Signout</a><br/>
						</g:if>
						<g:if test="${twitter.equals('false')}">
							<a href="socialAuth?id=twitter">Signin</a><br/>
						</g:if>
					</td>
					<td>
						<a href="socialAuth?id=google">
						  <img src="images/gmail-icon.jpg" alt="Gmail" title="Gmail" border="0"/>
						</a>
						<br/><br/>
						<g:if test="${google.equals('true')}">
							<a href="socialAuth/signout?id=google&mode=signout">Signout</a><br/>
						</g:if>
						<g:if test="${google.equals('false')}">
							<a href="socialAuth?id=google">Signin</a><br/>
						</g:if>
					</td>
					<td>
						<a href="socialAuth?id=yahoo">
						  <img src="images/yahoomail_icon.jpg" alt="YahooMail" title="YahooMail" border="0"/>
						</a>
						<br/><br/>
						<g:if test="${yahoo.equals('true')}">
							<a href="socialAuth/signout?id=yahoo&mode=signout">Signout</a><br/>
						</g:if>
						<g:if test="${yahoo.equals('false')}">
							<a href="socialAuth?id=yahoo">Signin</a><br/>
						</g:if>
					</td>
					<td>
						<a href="socialAuth?id=hotmail">
						  <img src="images/hotmail.jpeg" alt="HotMail" title="HotMail" border="0"/>
						</a>
						<br/><br/>
						<g:if test="${hotmail.equals('true')}">
							<a href="socialAuth/signout?id=hotmail&mode=signout">Signout</a><br/>
						</g:if>
						<g:if test="${hotmail.equals('false')}">
							<a href="socialAuth?id=hotmail">Signin</a><br/>
						</g:if>
					</td>
					<td>
						<a href="socialAuth?id=linkedin">
						  <img src="images/linkedin.gif" alt="Linked In" title="Linked In" border="0"/>
						</a>
						<br/><br/>
						<g:if test="${linkedin.equals('true')}">
							<a href="socialAuth/signout?id=linkedin&mode=signout">Signout</a><br/>
						</g:if>
						<g:if test="${linkedin.equals('false')}">
							<a href="socialAuth?id=linkedin">Signin</a><br/>
						</g:if>
					</td>
					<td>
						<a href="socialAuth?id=foursquare">
						  <img src="images/foursquare.jpeg" alt="FourSquare" title="FourSquare" border="0"/>
						</a>
						<br/><br/>
						<g:if test="${foursquare.equals('true')}">
							<a href="socialAuth/signout?id=foursquare&mode=signout">Signout</a><br/>
						</g:if>
						<g:if test="${foursquare.equals('false')}">
							<a href="socialAuth?id=foursquare">Signin</a><br/>
						</g:if>
					</td>
					<td>
						<a href="socialAuth?id=myspace">
						  <img src="images/myspace.jpeg" alt="MySpace" title="MySpace" border="0"/>
						</a>
						<br/><br/>
						<g:if test="${myspace.equals('true')}">
							<a href="socialAuth/signout?id=myspace&mode=signout">Signout</a><br/>
						</g:if>
						<g:if test="${myspace.equals('false')}">
							<a href="socialAuth?id=myspace">Signin</a><br/>
						</g:if>
					</td>
				</tr>
				<tr>
					<td colspan="8" align="center">
						<form action="socialAuth" onsubmit="return validate(this);">
							or enter OpenID url: <input type="text" value="" name="id"/>
							<input type="submit" value="Submit"/> 
						</form>
					</td>
				</tr>
				
			</table>
           	
        </div>
    </div>
    
</body>
</html>
