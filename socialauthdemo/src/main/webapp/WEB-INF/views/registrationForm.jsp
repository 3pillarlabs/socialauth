<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>
<div class="alert alert-info">
        	<strong>Step 2&nbsp;:&nbsp;</strong>Shows pre-filled form with the information given by provider with other required fields.<br>
        	Following informtation is retrieved. Please fill the missing fields.
        </div>
<div align="center">

<form action='<c:url value="/submitRegistration.do"/>' method="post">
	<table border="1">
		<tr>
			<td>Email</td>
			<td><input type="text" size="25" name="email" id="email" value='<c:out value="${profile.email}"/>'/></td>
		</tr>
		<tr>
			<td>Name</td>
			<td><input type="text" size="25" name="name" id="name" value='<c:out value="${profile.fullName}"/>'/></td>
		</tr>
		<tr>
			<td>Date of Birth</td>
			<td><input type="text" size="25" name="dob" id="dob" value=""/></td>
		</tr>
		<tr>
			<td>Country</td>
			<td><input type="text" size="25" name="country" id="country" value='<c:out value="${profile.country}"/>'/></td>
		</tr>
		<tr>
			<td>Language</td>
			<td><input type="text" size="25" name="language" id="language" value='<c:out value="${profile.language}"/>'/></td>
		</tr>
		<tr>
			<td>Gender</td>
			<td><input type="text" size="25" name="gender" id="gender" value='<c:out value="${profile.gender}"/>'/></td>
		</tr>
		<tr>
			<td>Location</td>
			<td><input type="text" size="25" name="location" id="location" value='<c:out value="${profile.location}"/>'/></td>
		</tr>
		<tr>
			<td>Profile Image</td>
			<td>
				<c:if test="${ profile.profileImageURL != null}">
					<img src='<c:out value="${profile.profileImageURL}"/>' alt="No Image"/>
				</c:if>
			</td>
		</tr>
		<tr>
			<td colspan="2" align="center">
				<input type="hidden" name="uniqueId" id="uniqueId" value='<c:out value="${profile.validatedId}"/>'/>
				<input type="hidden" name="profileImageURL" id="profileImageURL" value='<c:out value="${profile.profileImageURL}"/>'/>
				<input type="submit" value="Submit"/></td>
		</tr>
	</table>
</form>
</div>