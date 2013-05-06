<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false"%>
    <style type="text/css">
        .style1{text-align: justify;}
        
.provider {
    float: left;
   /* padding: 5px 10px;*/
}
    </style>
    <script>
    	var count = ${fn:length(connectedProvidersIds)};
    	function checkProviders(){
    		if(count == 0 ){
    			alert("You are not connected with any provider. Please click on any icon to connect with provider.");
    			return false;
    		}else{
    			return true;
    		}
    		
    	}
    </script>
	<c:set var="facebook" value="false" />
	<c:set var="twitter" value="false" />
	<c:set var="google" value="false" />
	<c:set var="yahoo" value="false" />
	<c:set var="hotmail" value="false" />
	<c:set var="linkedin" value="false" />
	<c:set var="foursquare" value="false" />
	<c:set var="myspace" value="false" />
	<c:set var="salesforce" value="false" />
	<c:set var="yammer" value="false" />
	<c:set var="mendeley" value="false" />
	<c:forEach var="item" items="${connectedProvidersIds}">
	 	<c:if test="${item eq 'facebook'}">
	    	<c:set var="facebook" value="true" />
	  	</c:if>
	  	<c:if test="${item eq 'google'}">
	    	<c:set var="google" value="true" />
	  	</c:if>
	  	<c:if test="${item eq 'twitter'}">
	    	<c:set var="twitter" value="true" />
	  	</c:if>
	  	<c:if test="${item eq 'yahoo'}">
	    	<c:set var="yahoo" value="true" />
	  	</c:if>
	  	<c:if test="${item eq 'hotmail'}">
	    	<c:set var="hotmail" value="true" />
	  	</c:if>
	  	<c:if test="${item eq 'linkedin'}">
	    	<c:set var="linkedin" value="true" />
	  	</c:if>
	  	<c:if test="${item eq 'myspace'}">
	    	<c:set var="myspace" value="true" />
	  	</c:if>
	  	<c:if test="${item eq 'foursquare'}">
	    	<c:set var="foursquare" value="true" />
	  	</c:if>
	  	<c:if test="${item eq 'salesforce'}">
	    	<c:set var="salesforce" value="true" />
	  	</c:if>
	  	<c:if test="${item eq 'yammer'}">
	    	<c:set var="yammer" value="true" />
	  	</c:if>
	  	<c:if test="${item eq 'mendeley'}">
	    	<c:set var="mendeley" value="true" />
	  	</c:if>
	</c:forEach>
    <div id="main">
        <div class="alert alert-info">
        	<strong>Step 1&nbsp;:&nbsp;</strong>Shows require providers for sharing. Share with all the connected providers that are marked with green color.<br>
        	Click on provider icon to connect if it is not marked with green color.
        </div>
        <div align="center">
        	<c:out value="${message }"/>
        </div>
        <div id="text" >
	       	<table cellpadding="10" cellspacing="10" align="center">
				<tr><td colspan="8">
					<c:if test="${ fn:length(connectedProvidersIds) == 0 }">
						You are not connected with any provider. Click on any provider to connect.
					</c:if>
				</td></tr>
				<tr><td colspan="8">
					<form action='<c:url value="/share.do"/>' method="post">
						<textarea rows="2" cols="50" id="message" name="message"></textarea>
						<c:if test="${ fn:length(connectedProvidersIds) == 0 }">
							<input type="submit" value="Share" disabled="disabled"/>
						</c:if>
						<c:if test="${ fn:length(connectedProvidersIds) > 0 }">
							<input type="submit" value="Share" />
						</c:if>
					</form>
				</td></tr>
				<tr>
					<td>
						<c:if test="${facebook eq true}">
							<div class="provider">
								<span style="position:absolute;">
									<img style="top:0px;left:0px;z-index:100" src="images/currentyes.png"/>
								</span>
								<img src="images/facebook_icon.png" alt="Facebook" title="Facebook" border="0"></img>
							</div>
						</c:if>
						<c:if test="${facebook eq false}">
							<a href="socialauth.do?id=facebook"><img src="images/facebook_icon.png" alt="Facebook" title="Facebook" border="0"></img></a>
						</c:if>
					</td>
					<td>
						<c:if test="${twitter eq true}">
							<div class="provider">
								<span style="position:absolute;">
									<img style="top:0px;left:0px;z-index:100" src="images/currentyes.png"/>
								</span>
								<img src="images/twitter_icon.png" alt="Twitter" title="Twitter" border="0"></img>
							</div>
						</c:if>
						<c:if test="${twitter eq false}">
							<a href="socialauth.do?id=twitter"><img src="images/twitter_icon.png" alt="Twitter" title="Twitter" border="0"></img></a>
						</c:if>
					</td>
					<td>
						<c:if test="${yahoo eq true}">
							<div class="provider">
								<span style="position:absolute;">
									<img style="top:0px;left:0px;z-index:100" src="images/currentyes.png"/>
								</span>
								<img src="images/yahoomail_icon.jpg" alt="YahooMail" title="YahooMail" border="0"></img>
							</div>
						</c:if>
						<c:if test="${yahoo eq false}">
							<a href="socialauth.do?id=yahoo"><img src="images/yahoomail_icon.jpg" alt="YahooMail" title="YahooMail" border="0"></img></a>
						</c:if>
					</td>
					<td>
						<c:if test="${hotmail eq true}">
							<div class="provider">
								<span style="position:absolute;">
									<img style="top:0px;left:0px;z-index:100" src="images/currentyes.png"/>
								</span>
								<img src="images/hotmail.jpeg" alt="HotMail" title="HotMail" border="0"></img>
							</div>
						</c:if>
						<c:if test="${hotmail eq false}">
							<a href="socialauth.do?id=hotmail"><img src="images/hotmail.jpeg" alt="HotMail" title="HotMail" border="0"></img></a>
						</c:if>
					</td>
					<td>
						<c:if test="${linkedin eq true}">
							<div class="provider">
								<span style="position:absolute;">
									<img style="top:0px;left:0px;z-index:100" src="images/currentyes.png"/>
								</span>
								<img src="images/linkedin.gif" alt="Linked In" title="Linked In" border="0"></img>
							</div>
						</c:if>
						<c:if test="${linkedin eq false}">
							<a href="socialauth.do?id=linkedin"><img src="images/linkedin.gif" alt="Linked In" title="Linked In" border="0"></img></a>
						</c:if>
					</td>
					<td>
						<c:if test="${myspace eq true}">
							<div class="provider">
								<span style="position:absolute;">
									<img style="top:0px;left:0px;z-index:100" src="images/currentyes.png"/>
								</span>
								<img src="images/myspace.jpeg" alt="MySpace" title="MySpace" border="0"></img>
							</div>
						</c:if>
						<c:if test="${myspace eq false}">
							<a href="socialauth.do?id=myspace"><img src="images/myspace.jpeg" alt="MySpace" title="MySpace" border="0"></img></a>
						</c:if>
					</td>
					<td>
						<c:if test="${yammer eq true}">
							<div class="provider">
								<span style="position:absolute;">
									<img style="top:0px;left:0px;z-index:100" src="images/currentyes.png"/>
								</span>
								<img src="images/yammer.jpg" alt="Yammer" title="Yammer" border="0"></img>
							</div>
						</c:if>
						<c:if test="${yammer eq false}">
							<a href="socialauth.do?id=yammer"><img src="images/yammer.jpg" alt="Yammer" title="Yammer" border="0"></img></a>
						</c:if>
					</td>
				</tr>
				
			</table>
           	<br />
	        <br />
	        <br />
	        <br />
	        <br />
	        <br />
	        <br />
	        <br />
	        <br />
            
        </div>
    </div>
    
