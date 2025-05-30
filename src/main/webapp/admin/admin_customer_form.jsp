<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="isEditMode" value="${not empty customerToEdit && not empty customerToEdit.userId}" />
<c:set var="formTitle" value="${isEditMode ? '编辑客户信息' : '添加新客户'}" />
<% request.setAttribute("pageTitle", (String)pageContext.getAttribute("formTitle")); %>

<jsp:include page="/WEB-INF/jspf/admin_header.jspf" />

<div class="admin-page-container">
    <div class="card admin-form-container">
        <div class="card-header">
            ${formTitle}
            <c:if test="${isEditMode}"> - 用户ID: ${customerToEdit.userId}</c:if>
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/admin/customers" method="POST">
                <c:choose>
                    <c:when test="${isEditMode}">
                        <input type="hidden" name="action" value="edit_customer">
                        <input type="hidden" name="userId" value="${customerToEdit.userId}">
                    </c:when>
                    <c:otherwise>
                        <input type="hidden" name="action" value="add_customer">
                    </c:otherwise>
                </c:choose>

                <div class="form-group">
                    <label for="username">用户名 <span class="required-field">*</span></label>
                    <input type="text" id="username" name="username" class="form-control" 
                           value="${fn:escapeXml(customerToEdit.username)}" required 
                           <c:if test="${isEditMode && sessionScope.loggedInUser.userId == customerToEdit.userId && customerToEdit.username == 'admin'}">readonly title="系统默认admin用户名不可更改"</c:if>>
                    <c:if test="${isEditMode && sessionScope.loggedInUser.userId == customerToEdit.userId && customerToEdit.username == 'admin'}">
                        <small class="form-text text-muted">默认管理员 'admin' 的用户名不可更改。</small>
                    </c:if>
                </div>

                <div class="form-group">
                    <label for="email">邮箱 <span class="required-field">*</span></label>
                    <input type="email" id="email" name="email" class="form-control" value="${fn:escapeXml(customerToEdit.email)}" required>
                </div>

                <c:if test="${not isEditMode}">
                    <div class="form-group">
                        <label for="password">密码 <span class="required-field">*</span></label>
                        <input type="password" id="password" name="password" class="form-control" required>
                    </div>
                </c:if>
                
                <div class="form-row">
                    <div class="form-group col-md-6">
                        <label for="role">角色 <span class="required-field">*</span></label>
                        <select id="role" name="role" class="form-control" required 
                                <c:if test="${isEditMode && sessionScope.loggedInUser.userId == customerToEdit.userId && customerToEdit.role == 'admin'}">disabled title="不能修改自己的管理员角色"</c:if>>
                            <option value="user" ${customerToEdit.role == 'user' ? 'selected' : ''}>user</option>
                            <option value="admin" ${customerToEdit.role == 'admin' ? 'selected' : ''}>admin</option>
                        </select>
                         <c:if test="${isEditMode && sessionScope.loggedInUser.userId == customerToEdit.userId && customerToEdit.role == 'admin'}">
                            <input type="hidden" name="role" value="admin" /> <%-- Ensure role is submitted if disabled --%>
                            <small class="form-text text-muted">不能通过此表单修改自己的管理员角色。</small>
                        </c:if>
                    </div>
                     <div class="form-group col-md-6">
                        <label for="registrationDate">注册日期</label>
                        <input type="text" id="registrationDate" name="registrationDate" class="form-control" 
                               value="<fmt:formatDate value='${customerToEdit.registrationDate}' pattern='yyyy-MM-dd HH:mm:ss'/>" readonly 
                               title="注册日期不可修改">
                    </div>
                </div>

                <div class="form-group">
                    <label for="personalSignature">个人签名</label>
                    <textarea id="personalSignature" name="personalSignature" rows="3" class="form-control">${fn:escapeXml(customerToEdit.personalSignature)}</textarea>
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">${isEditMode ? '更新客户信息' : '添加客户'}</button>
                    <a href="${pageContext.request.contextPath}/admin/customers?action=list" class="btn btn-secondary">取消并返回列表</a>
                </div>
            </form>

            <c:if test="${isEditMode}">
                <hr id="reset-password-section" class="my-4">
                <h4><i class="fas fa-key"></i> 重置用户密码</h4>
                 <c:if test="${sessionScope.loggedInUser.userId == customerToEdit.userId}">
                    <p class="text-warning">提示：您正在编辑自己的账户。如需修改密码，请使用前台的“修改密码”功能或联系其他管理员。</p>
                </c:if>
                <form action="${pageContext.request.contextPath}/admin/customers" method="POST" class="mt-3">
                    <input type="hidden" name="action" value="reset_password">
                    <input type="hidden" name="userId" value="${customerToEdit.userId}">
                    <div class="form-group">
                        <label for="newPassword">新密码 <span class="required-field">*</span></label>
                        <input type="password" id="newPassword" name="newPassword" class="form-control" required 
                               <c:if test="${sessionScope.loggedInUser.userId == customerToEdit.userId}">disabled</c:if>>
                    </div>
                    <button type="submit" class="btn btn-warning" 
                            <c:if test="${sessionScope.loggedInUser.userId == customerToEdit.userId}">disabled title="请通过个人中心修改自己的密码"</c:if>>
                        确认重置密码
                    </button>
                </form>
            </c:if>

        </div>
    </div>
</div>

<c:set var="pageSpecificAdminStyles" scope="request">
    .admin-form-container .card-header { font-size: 1.2rem; }
    .required-field { color: red; margin-left: 2px;}
    .form-row { display: flex; flex-wrap: wrap; margin-right: -5px; margin-left: -5px; }
    .form-row > .col-md-6 { position: relative; width: 100%; padding-right: 5px; padding-left: 5px; flex: 0 0 50%; max-width: 50%; }
    @media (max-width: 768px) { .form-row > .col-md-6 { flex: 0 0 100%; max-width: 100%; } }
    .form-actions { margin-top: 25px; padding-top: 15px; border-top: 1px solid #eee; text-align: right;}
    .form-actions .btn { margin-left: 10px; }
    .my-4 { margin-top: 1.5rem !important; margin-bottom: 1.5rem !important; } /* Bootstrap-like margin utility */
    .mt-3 { margin-top: 1rem !important; }
    .text-muted { color: #6c757d !important; }
    .text-warning { color: #ffc107 !important; }
    .fas { margin-right: 3px; }
</c:set>

<jsp:include page="/WEB-INF/jspf/admin_footer.jspf" />
