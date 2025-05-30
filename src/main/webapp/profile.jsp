<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<% request.setAttribute("pageTitle", "个人中心"); %>
<jsp:include page="/WEB-INF/jspf/header.jspf" />

<div class="container profile-page">
    <h2>个人中心</h2>

    <c:if test="${empty userProfile}">
        <div class="alert alert-warning">无法加载您的个人信息。请尝试重新登录。</div>
    </c:if>

    <c:if test="${not empty userProfile}">
        <div class="profile-details-grid">
            <div class="profile-info-card">
                <h3>账户信息</h3>
                <p><strong>用户名:</strong> <c:out value="${userProfile.username}"/></p>
                <p><strong>邮箱:</strong> <c:out value="${userProfile.email}"/></p>
                <p><strong>用户角色:</strong> 
                    <c:choose>
                        <c:when test="${userProfile.role == 'admin'}">管理员</c:when>
                        <c:otherwise>普通用户</c:otherwise>
                    </c:choose>
                </p>
                <p><strong>注册日期:</strong> <fmt:formatDate value="${userProfile.registrationDate}" pattern="yyyy-MM-dd HH:mm:ss"/></p>
                <p><strong>上次登录:</strong> 
                    <c:choose>
                        <c:when test="${not empty userProfile.lastLogin}">
                            <fmt:formatDate value="${userProfile.lastLogin}" pattern="yyyy-MM-dd HH:mm:ss"/>
                        </c:when>
                        <c:otherwise>首次登录或未记录</c:otherwise>
                    </c:choose>
                </p>
            </div>

            <div class="profile-signature-card">
                <h3>个人签名</h3>
                <c:if test="${not empty sessionScope.successMessage}">
                    <div class="alert alert-success">
                        <c:out value="${sessionScope.successMessage}"/>
                    </div>
                    <c:remove var="successMessage" scope="session"/>
                </c:if>
                <c:if test="${not empty requestScope.errorMessage}"> <%-- For errors on POST forward --%>
                    <div class="alert alert-danger">
                        <c:out value="${requestScope.errorMessage}"/>
                    </div>
                </c:if>
                 <c:if test="${not empty sessionScope.errorMessage}"> <%-- For errors on POST-redirect-GET --%>
                    <div class="alert alert-danger">
                        <c:out value="${sessionScope.errorMessage}"/>
                    </div>
                    <c:remove var="errorMessage" scope="session"/>
                </c:if>


                <p><strong>当前签名:</strong></p>
                <div class="current-signature">
                    <c:choose>
                        <c:when test="${not empty userProfile.personalSignature}">
                            <c:out value="${userProfile.personalSignature}"/>
                        </c:when>
                        <c:otherwise>
                            <em>您还没有设置个人签名。</em>
                        </c:otherwise>
                    </c:choose>
                </div>
                
                <hr>
                <h4>更新签名</h4>
                <form action="${pageContext.request.contextPath}/profile" method="POST" class="signature-form">
                    <input type="hidden" name="action" value="update_signature">
                    <div class="form-group">
                        <textarea name="personalSignature" rows="4" class="form-control" placeholder="输入您的新签名...">${fn:escapeXml(userProfile.personalSignature)}</textarea>
                    </div>
                    <button type="submit" class="btn btn-primary">更新签名</button>
                </form>
            </div>
        </div>

        <div class="profile-actions">
            <a href="${pageContext.request.contextPath}/order?action=list" class="btn btn-info">我的订单</a>
            <%-- <a href="${pageContext.request.contextPath}/change_password.jsp" class="btn btn-warning">修改密码</a> --%>
            <%-- Add more links as needed, e.g., my reviews, shipping addresses --%>
        </div>
    </c:if>
</div>

<c:set var="pageSpecificStyles" scope="request">
    .profile-page h2 { text-align: center; margin-bottom: 25px; }
    .profile-details-grid { 
        display: grid; 
        grid-template-columns: 1fr 1.5fr; 
        gap: 30px; 
        margin-bottom: 30px;
    }
    @media (max-width: 768px) { .profile-details-grid { grid-template-columns: 1fr; } }

    .profile-info-card, .profile-signature-card {
        background-color: #fff;
        padding: 25px;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.08);
    }
    .profile-info-card h3, .profile-signature-card h3 {
        color: #35424a;
        border-bottom: 1px solid #eee;
        padding-bottom: 10px;
        margin-top: 0;
        margin-bottom: 15px;
    }
    .profile-info-card p { margin-bottom: 10px; line-height: 1.7; }
    .profile-info-card p strong { color: #333; min-width: 80px; display: inline-block;}
    
    .current-signature { 
        padding: 10px; 
        background-color: #f9f9f9; 
        border-radius: 4px; 
        min-height: 40px; 
        margin-bottom: 15px; 
        font-style: italic;
        white-space: pre-wrap; /* Preserve line breaks in signature */
    }
    .signature-form textarea { margin-bottom: 10px; }
    .profile-actions { text-align: center; margin-top: 30px; padding-top:20px; border-top:1px solid #eee;}
    .profile-actions .btn { margin: 0 10px; }
</c:set>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
