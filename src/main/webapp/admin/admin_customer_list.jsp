<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<% request.setAttribute("pageTitle", "客户管理"); %>
<jsp:include page="/WEB-INF/jspf/admin_header.jspf" />

<div class="admin-page-container">
    <div class="admin-page-toolbar">
        <a href="${pageContext.request.contextPath}/admin/customers?action=add_form" class="btn btn-success"><i class="fas fa-user-plus"></i> 添加新客户</a>
    </div>

    <div class="card">
        <div class="card-header">
            客户列表
        </div>
        <div class="card-body">
            <c:choose>
                <c:when test="${not empty customerList}">
                    <div class="table-responsive">
                        <table class="admin-table customer-admin-table">
                            <thead>
                                <tr>
                                    <th>用户ID</th>
                                    <th>用户名</th>
                                    <th>邮箱</th>
                                    <th>角色</th>
                                    <th>注册日期</th>
                                    <th>上次登录</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="customer" items="${customerList}">
                                    <tr>
                                        <td><c:out value="${customer.userId}"/></td>
                                        <td><c:out value="${customer.username}"/></td>
                                        <td><c:out value="${customer.email}"/></td>
                                        <td>
                                            <span class="role-badge role-${fn:toLowerCase(customer.role)}">
                                                <c:out value="${customer.role}"/>
                                            </span>
                                        </td>
                                        <td><fmt:formatDate value="${customer.registrationDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty customer.lastLogin}">
                                                    <fmt:formatDate value="${customer.lastLogin}" pattern="yyyy-MM-dd HH:mm"/>
                                                </c:when>
                                                <c:otherwise>从未登录</c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/customers?action=edit_form&userId=${customer.userId}" class="btn btn-primary btn-sm"><i class="fas fa-edit"></i> 编辑</a>
                                            <c:if test="${sessionScope.loggedInUser.userId != customer.userId}">
                                                <form action="${pageContext.request.contextPath}/admin/customers" method="POST" style="display:inline;" onsubmit="return confirmAction('确定要删除用户 #${customer.userId} (${fn:escapeXml(customer.username)}) 吗？此操作不可恢复。');">
                                                    <input type="hidden" name="action" value="delete_customer_post">
                                                    <input type="hidden" name="userId" value="${customer.userId}">
                                                    <input type="hidden" name="pageRet" value="${currentPage}">
                                                    <button type="submit" class="btn btn-danger btn-sm"><i class="fas fa-trash"></i> 删除</button>
                                                </form>
                                            </c:if>
                                            <c:if test="${sessionScope.loggedInUser.userId == customer.userId}">
                                                <button type="button" class="btn btn-danger btn-sm" disabled title="不能删除当前登录的管理员账户"><i class="fas fa-trash"></i> 删除</button>
                                            </c:if>
                                            <%-- Reset password could be a link to edit_form section or a direct action --%>
                                            <a href="${pageContext.request.contextPath}/admin/customers?action=edit_form&userId=${customer.userId}#reset-password-section" class="btn btn-warning btn-sm"><i class="fas fa-key"></i> 重置密码</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="text-muted">当前没有客户记录。</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- Pagination Controls --%>
    <c:if test="${totalPages > 1}">
        <nav class="pagination admin-pagination">
            <c:choose>
                <c:when test="${currentPage > 1}">
                    <a href="${pageContext.request.contextPath}/admin/customers?action=list&page=${currentPage - 1}">&laquo; 上一页</a>
                </c:when>
                <c:otherwise><span class="disabled">&laquo; 上一页</span></c:otherwise>
            </c:choose>
            <c:forEach var="i" begin="1" end="${totalPages}">
                <c:choose>
                    <c:when test="${i == currentPage}"><span class="current">${i}</span></c:when>
                    <c:otherwise><a href="${pageContext.request.contextPath}/admin/customers?action=list&page=${i}">${i}</a></c:otherwise>
                </c:choose>
            </c:forEach>
            <c:choose>
                <c:when test="${currentPage < totalPages}">
                    <a href="${pageContext.request.contextPath}/admin/customers?action=list&page=${currentPage + 1}">下一页 &raquo;</a>
                </c:when>
                <c:otherwise><span class="disabled">下一页 &raquo;</span></c:otherwise>
            </c:choose>
        </nav>
    </c:if>
</div>

<c:set var="pageSpecificAdminStyles" scope="request">
    .admin-page-toolbar { margin-bottom: 20px; text-align: right; }
    .customer-admin-table td, .customer-admin-table th { vertical-align: middle; }
    .role-badge { display: inline-block; padding: .25em .6em; font-size: .75em; font-weight: 700; line-height: 1; text-align: center; white-space: nowrap; vertical-align: baseline; border-radius: .25rem; text-transform: capitalize; }
    .role-admin { background-color: #d9534f; color: white; }
    .role-user { background-color: #5bc0de; color: white; }
    .fas { margin-right: 3px; }
</c:set>

<jsp:include page="/WEB-INF/jspf/admin_footer.jspf" />
