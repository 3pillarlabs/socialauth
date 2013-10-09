<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page isELIgnored="false"%>
    <style type="text/css">
        .style1{text-align: justify;}
    </style>
 <style type="text/css">
        .style1{text-align: justify;}
        .sectiontableheader {background-color:#C8D7E3;color:#293D6B;font-size:12pt;font-weight:bold;padding:2px;}
		.sectiontableentry2 {background:none repeat scroll 0 0 #F7F7F7;padding:2px;}
		.sectiontableentry1 {background:none repeat scroll 0 0 #FFFFF0;padding:2px;}
    </style>
<table cellspacing="1" cellspacing="4" border="0" bgcolor="e5e5e5"
	align="center" width="60%">
	<tr class="sectiontableheader">
					<th colspan="2">Facebbok Wall</th>
				</tr>
	<tr class="sectiontableentry2"><th>From</th><th>Message</th></tr>
	<c:forEach var="feed" items="${feeds}" varStatus="index">
	
		<tr
			class='<c:if test="${index.count % 2 == 0}">sectiontableentry2</c:if><c:if test="${index.count % 2 != 0}">sectiontableentry1</c:if>'>
			<td><c:out value="${feed.from}" /></td>
			<td><c:out value="${feed.message}" /></td>
		</tr>
	</c:forEach>

</table>