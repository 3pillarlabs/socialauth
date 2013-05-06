<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>
<div class="alert alert-info">
	<strong>Step 3&nbsp;:&nbsp;</strong>Show Registration details at the end. Thanks for Registration.
</div>
<div align="center">
<form>
	<table border="1">
		<tr>
			<td>Email</td>
			<td><c:out value="${user.email}"/></td>
		</tr>
		<tr>
			<td>Name</td>
			<td><c:out value="${user.name}"/></td>
		</tr>
		<tr>
			<td>Date of Birth</td>
			<td><c:out value="${user.dob}"/></td>
		</tr>
		<tr>
			<td>Country</td>
			<td><c:out value="${user.country}"/></td>
		</tr>
		<tr>
			<td>Language</td>
			<td><c:out value="${user.language}"/></td>
		</tr>
		<tr>
			<td>Gender</td>
			<td><c:out value="${user.gender}"/></td>
		</tr>
		<tr>
			<td>Location</td>
			<td><c:out value="${user.location}"/></td>
		</tr>
		<tr>
			<td>Profile Image</td>
			<td>
				<c:if test="${ user.profileImageURL != null}">
					<img src='<c:out value="${user.profileImageURL}"/>' alt="No Image"/>
				</c:if>
			</td>
		</tr>
	</table>
</form>
</div>