<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>BrickRed SocialAuth Demo </title>
    <style type="text/css">
        .style1{text-align: justify;}
        .sectiontableheader {background-color:#C8D7E3;
              color:#293D6B;font-size:8pt;
              font-weight:bold;padding:2px;}
    .even   {background:none repeat scroll 0 0 #F7F7F7;padding:2px;}
    .odd {background:none repeat scroll 0 0 #FFFFF0;padding:2px;}
    </style>
    <script>
      function updateStatus(){
          var btn = document.getElementById('btnUpdateStatus');
          btn.disabled=true;
      var msg = prompt("Enter your status here:");
      if(msg == null || msg.length == 0){
        btn.disabled=false;
          return false;
          }
      msg = "statusMessage="+msg;
      var req = new XMLHttpRequest();
      req.open("POST", "socialAuthUpdateStatus");
      req.setRequestHeader("Accept", "text/xml");
      req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
      req.setRequestHeader("Content-length", msg.length);
      req.setRequestHeader("Connection", "close");
      req.onreadystatechange = function () {
        if (req.readyState == 4) {
          if(req.responseText.length > 0) {
              alert(req.responseText);
              btn.disabled=false;
          }
        }
      };
      req.send(msg);
      }
  </script>
</head>
<body>
    
<div id="main">
             
<div id="text" >
  <h2 align="center">Authentication has been successful.</h2>
  <br/>
  <div align="center"><a href="index.gsp">Back</a></div>
  <br>
  <h3 align="center">Profile Information</h3>
  <table cellspacing="1" cellspacing="4" border="0" bgcolor="e5e5e5" width="60%" align="center">
    <tr class="sectiontableheader">
      <th>Profile Field</th>
      <th>Value</th>
    </tr>
    <tr class="odd">
      <td>Email:</td>
      <td>${profile.email}</td>
    </tr>
    <tr class="even  ">
      <td>First Name:</td>
      <td>${profile.firstName}</td>
    </tr>
    <tr class="odd">
      <td>Last Name:</td>
      <td>${profile.lastName}</td>
    </tr>
    <tr class="even  ">
      <td>Country:</td>
      <td>${profile.country}</td>
    </tr>
    <tr class="odd">
      <td>Language:</td>
      <td>${profile.language}</td>
    </tr>
    <tr class="even  ">
      <td>Full Name:</td>
      <td>${profile.fullName}</td>
    </tr>
    <tr class="odd">
      <td>Display Name:</td>
      <td>${profile.displayName}</td>
    </tr>
    <tr class="even  ">
      <td>DOB:</td>
      <td>${profile.dob}</td>
    </tr>
    <tr class="odd">
      <td>Gender:</td>
      <td>${profile.gender}</td>
    </tr>
    <tr class="even  ">
      <td>Location:</td>
      <td>${profile.location}</td>
    </tr>
    <tr class="odd">
      <td>Profile Image:</td>
      <td>
        <g:if test="${profile.profileImageURL != null}">
          <img src='${profile.profileImageURL}'/>
        </g:if>
      </td>
    </tr>
    <tr class="even  ">
      <td>Update status:</td>
      <td>
        <input type="button" value="Click to Update Status" onclick="updateStatus();" id="btnUpdateStatus"/>    
      </td>
    </tr>
  </table>
  <h3 align="center">Contact Details</h3>
  <table cellspacing="1" cellspacing="4" border="0" bgcolor="e5e5e5" align="center" width="60%">
    <tr class="sectiontableheader">
      <th width="15%">Name</th>
      <th>Email</th>
      <th>Profile URL</th>
    </tr>
    <g:each var="contact" in="${contacts}" status="index">
      <tr class='<g:if test="${index % 2 == 0}">even  </g:if><g:if test="${index % 2 != 0}">odd</g:if>'>
        <td>${contact.firstName} ${contact.lastName}</td>
        <td>${contact.email}</td>
        <td><a href='${contact.profileUrl}' target="_new">${contact.profileUrl}</a></td>
      </tr>
    </g:each>
  </table>
   
</div>
</div>
</body>
</html>
