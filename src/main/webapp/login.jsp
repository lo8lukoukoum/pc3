<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<% request.setAttribute("pageTitle", "用户登录"); %>
<jsp:include page="/WEB-INF/jspf/header.jspf" />

<div class="form-container auth-form">
    <h2>用户登录</h2>

    <%-- Session success message (e.g., after registration or logout) --%>
    <c:if test="${not empty sessionScope.successMessage}">
        <div class="alert alert-success" role="alert">
            <c:out value="${sessionScope.successMessage}"/>
        </div>
        <c:remove var="successMessage" scope="session"/>
    </c:if>

    <%-- Request error message (e.g., login failure) --%>
    <c:if test="${not empty requestScope.errorMessage}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${requestScope.errorMessage}"/>
        </div>
    </c:if>
    
    <%-- Warning message from session (e.g. "Please login first") --%>
    <c:if test="${not empty sessionScope.warningMessage}">
        <div class="alert alert-warning" role="alert">
            <c:out value="${sessionScope.warningMessage}"/>
        </div>
        <c:remove var="warningMessage" scope="session"/>
    </c:if>


    <form method="POST" action="${pageContext.request.contextPath}/login">
        <div class="form-group">
            <label for="username">用户名:</label>
            <input type="text" id="username" name="username" value="${fn:escapeXml(param.username)}" required>
        </div>
        <div class="form-group">
            <label for="password">密码:</label>
            <input type="password" id="password" name="password" required>
        </div>
        <div class="form-group">
            <button type="submit" class="btn">登录</button>
        </div>
    </form>
    <p class="auth-switch-link">还没有账户？ <a href="${pageContext.request.contextPath}/register.jsp">立即注册</a></p>
</div>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
