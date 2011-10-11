<%--
  ~ Copyright (c) 2011 Brown Bag Consulting.
  ~ This file is part of the ExpressUI project.
  ~ Author: Juan Osuna
  ~
  ~ This program is free software: you can redistribute it and/or modify
  ~ it under the terms of the GNU Affero General Public License Version 3
  ~ as published by the Free Software Foundation with the addition of the
  ~ following permission added to Section 15 as permitted in Section 7(a):
  ~ FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
  ~ Brown Bag Consulting, Brown Bag Consulting DISCLAIMS THE WARRANTY OF
  ~ NON INFRINGEMENT OF THIRD PARTY RIGHTS.
  ~
  ~ This program is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  ~ GNU Affero General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Affero General Public License
  ~ along with this program.  If not, see <http://www.gnu.org/licenses/>.
  ~
  ~ The interactive user interfaces in modified source and object code versions
  ~ of this program must display Appropriate Legal Notices, as required under
  ~ Section 5 of the GNU Affero General Public License.
  ~
  ~ You can be released from the requirements of the license by purchasing
  ~ a commercial license. Buying such a license is mandatory as soon as you
  ~ develop commercial activities involving the ExpressUI software without
  ~ disclosing the source code of your own applications. These activities
  ~ include: offering paid services to customers as an ASP, providing
  ~ services from a web application, shipping ExpressUI with a closed
  ~ source product.
  ~
  ~ For more information, please contact Brown Bag Consulting at this
  ~ address: juan@brownbagconsulting.com.
  --%>

<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core_rt' %>

<html>
<head>
    <title>Login</title>
</head>

<body>
<h2 align="center">Login</h2>

<p align="center">Please log in as user "guest" with password "guest."</p>

<form name="f" action="<c:url value='/j_spring_security_check'/>" method="POST">
    <table align="center">
        <tr>
            <td><label for="j_username">User</label></td>
            <td><input type='text' id="j_username" name="j_username"
                       value='<c:if test="${not empty param.login_error}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>'/>
            </td>
            <td align="left"></td>
        </tr>
        <tr>
            <td><label for="j_password">Password</label></td>
            <td><input type='password' id="j_password" name='j_password'></td>
        </tr>
        <tr>
            <td align="right"><input type="checkbox" name="_spring_security_remember_me" value="true"></td>
            <td align="left">remember me</td>
        </tr>

        <tr>
            <td colspan='2'><input name="submit" type="submit" value="Login"></td>
        </tr>
    </table>
</form>

<c:if test="${not empty param.login_error}">
    <div align="center">
      <span style="color: red;">
        Your login attempt was not successful, please try again.
      </span>
    </div>
</c:if>

</body>
</html>