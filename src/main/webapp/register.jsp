<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<% request.setAttribute("pageTitle", "用户注册"); %>
<jsp:include page="/WEB-INF/jspf/header.jspf" />

<div class="form-container auth-form">
    <h2>用户注册</h2>

    <%-- Request error message (e.g., registration validation failure) --%>
    <c:if test="${not empty requestScope.errorMessage}">
        <div class="alert alert-danger" role="alert">
            <c:out value="${requestScope.errorMessage}"/>
        </div>
    </c:if>

    <form method="POST" action="${pageContext.request.contextPath}/register">
        <div class="form-group">
            <label for="username">用户名:</label>
            <input type="text" id="username" name="username" 
                   value="<c:out value='${not empty param.username ? param.username : requestScope.submittedUsername}'/>" 
                   required>
        </div>
        <div class="form-group">
            <label for="email">邮箱:</label>
            <input type="email" id="email" name="email" 
                   value="<c:out value='${not empty param.email ? param.email : requestScope.submittedEmail}'/>" 
                   required>
        </div>
        <div class="form-group">
            <label for="password">密码:</label>
            <input type="password" id="password" name="password" required>
        </div>
        <div class="form-group">
            <label for="confirmPassword">确认密码:</label>
            <input type="password" id="confirmPassword" name="confirmPassword" required>
        </div>
        <div class="form-group">
            <label for="personalSignature">个人签名 (可选):</label>
            <textarea id="personalSignature" name="personalSignature" rows="3"><c:out value='${not empty param.personalSignature ? param.personalSignature : requestScope.submittedSignature}'/></textarea>
        </div>
        <div class="form-group">
            <button type="submit" class="btn">注册</button>
        </div>
    </form>
    <p class="auth-switch-link">已有账户？ <a href="${pageContext.request.contextPath}/login.jsp">立即登录</a></p>
</div>

<%-- Add some specific styles for auth forms if needed, or rely on general form styles from style.css --%>
<c:set var="pageSpecificStyles" scope="request">
    .auth-form {
        max-width: 500px;
        margin: 30px auto;
        padding: 25px;
        background-color: #fff;
        border: 1px solid #ddd;
        border-radius: 8px;
        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }
    .auth-form h2 {
        text-align: center;
        margin-bottom: 20px;
        color: #333;
    }
    .auth-switch-link {
        text-align: center;
        margin-top: 15px;
    }
</c:set>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
