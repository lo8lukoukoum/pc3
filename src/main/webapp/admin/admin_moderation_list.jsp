<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="itemType" value="${requestScope.itemType}" />
<c:set var="moderationTitle" value="${itemType == 'review' ? '评价审核' : '留言审核'}" />
<c:set var_default_status_filter = "${itemType == 'review' ? '待审核' : '待审核'}" />
<c:set var="currentStatusFilter" value="${not empty requestScope.currentStatusFilter ? requestScope.currentStatusFilter : var_default_status_filter}" />
<c:set var="pageAction" value="${requestScope.pageAction}" /> <%-- e.g., list_reviews or list_messages --%>

<% request.setAttribute("pageTitle", (String)pageContext.getAttribute("moderationTitle")); %>
<jsp:include page="/WEB-INF/jspf/admin_header.jspf" />

<div class="admin-page-container moderation-page">
    <%-- Filter Tabs/Links --%>
    <div class="admin-filter-tabs">
        <a href="${pageContext.request.contextPath}/admin/moderate?action=list_reviews" 
           class="${itemType == 'review' ? 'active' : ''}">评价审核</a>
        <a href="${pageContext.request.contextPath}/admin/moderate?action=list_messages" 
           class="${itemType == 'message' ? 'active' : ''}">留言审核</a>
    </div>
    
    <div class="admin-filter-bar card">
        <form action="${pageContext.request.contextPath}/admin/moderate" method="GET" class="form-inline">
            <input type="hidden" name="action" value="${pageAction}">
            <div class="form-group">
                <label for="status_filter">筛选状态:</label>
                <select name="status_filter" id="status_filter" class="form-control" onchange="this.form.submit()">
                    <option value="待审核" ${currentStatusFilter == '待审核' ? 'selected' : ''}>待审核</option>
                    <option value="已批准" ${currentStatusFilter == '已批准' ? 'selected' : ''}>已批准</option>
                    <c:if test="${itemType == 'review'}">
                        <option value="已拒绝" ${currentStatusFilter == '已拒绝' ? 'selected' : ''}>已拒绝</option>
                    </c:if>
                    <c:if test="${itemType == 'message'}">
                         <%-- Messages might have other statuses like "已忽略" if implemented --%>
                    </c:if>
                </select>
            </div>
        </form>
    </div>

    <div class="card">
        <div class="card-header">
            ${moderationTitle} - ${currentStatusFilter}
        </div>
        <div class="card-body">
            <c:choose>
                <c:when test="${not empty itemList}">
                    <div class="table-responsive">
                        <table class="admin-table moderation-table">
                            <thead>
                                <c:if test="${itemType == 'review'}">
                                    <tr>
                                        <th>ID</th>
                                        <th>商品ID</th>
                                        <th>用户ID</th>
                                        <th>评分</th>
                                        <th>评价内容</th>
                                        <th>提交日期</th>
                                        <th>状态</th>
                                        <th>操作</th>
                                    </tr>
                                </c:if>
                                <c:if test="${itemType == 'message'}">
                                    <tr>
                                        <th>ID</th>
                                        <th>用户ID</th>
                                        <th>留言者名称</th>
                                        <th>邮箱</th>
                                        <th>留言内容</th>
                                        <th>提交日期</th>
                                        <th>状态</th>
                                        <th>操作</th>
                                    </tr>
                                </c:if>
                            </thead>
                            <tbody>
                                <c:forEach var="item" items="${itemList}">
                                    <c:if test="${itemType == 'review'}">
                                        <tr>
                                            <td><c:out value="${item.reviewId}"/></td>
                                            <td><a href="${pageContext.request.contextPath}/product_detail.jsp?productId=${item.productId}" target="_blank"><c:out value="${item.productId}"/> <i class="fas fa-external-link-alt"></i></a></td>
                                            <td><c:out value="${item.userId}"/></td> <%-- TODO: Show username --%>
                                            <td><c:out value="${item.rating}"/> 星</td>
                                            <td title="${fn:escapeXml(item.commentText)}">${fn:escapeXml(fn:substring(item.commentText, 0, 50))}<c:if test="${fn:length(item.commentText) > 50}">...</c:if></td>
                                            <td><fmt:formatDate value="${item.reviewDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                            <td><span class="status-badge status-${fn:toLowerCase(fn:replace(item.status, ' ', '_'))}"><c:out value="${item.status}"/></span></td>
                                            <td>
                                                <form action="${pageContext.request.contextPath}/admin/moderate" method="POST" style="display:inline;">
                                                    <input type="hidden" name="itemId" value="${item.reviewId}">
                                                    <input type="hidden" name="itemType" value="review">
                                                    <input type="hidden" name="pageRet" value="${currentPage}">
                                                    <input type="hidden" name="statusRet" value="${currentStatusFilter}">
                                                    <c:if test="${item.status == '待审核'}">
                                                        <button type="submit" name="action" value="approve_review" class="btn btn-success btn-sm"><i class="fas fa-check"></i> 批准</button>
                                                        <button type="submit" name="action" value="reject_review" class="btn btn-warning btn-sm"><i class="fas fa-times"></i> 拒绝</button>
                                                    </c:if>
                                                    <button type="submit" name="action" value="delete_review" class="btn btn-danger btn-sm" onclick="return confirmAction('确定要删除此评价吗？');"><i class="fas fa-trash"></i> 删除</button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:if>
                                    <c:if test="${itemType == 'message'}">
                                        <tr>
                                            <td><c:out value="${item.messageId}"/></td>
                                            <td><c:out value="${not empty item.userId ? item.userId : '访客'}"/></td>
                                            <td><c:out value="${item.name}"/></td>
                                            <td><c:out value="${item.email}"/></td>
                                            <td title="${fn:escapeXml(item.messageText)}">${fn:escapeXml(fn:substring(item.messageText, 0, 50))}<c:if test="${fn:length(item.messageText) > 50}">...</c:if></td>
                                            <td><fmt:formatDate value="${item.messageDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                            <td><span class="status-badge status-${fn:toLowerCase(fn:replace(item.status, ' ', '_'))}"><c:out value="${item.status}"/></span></td>
                                            <td>
                                                <form action="${pageContext.request.contextPath}/admin/moderate" method="POST" style="display:inline;">
                                                    <input type="hidden" name="itemId" value="${item.messageId}">
                                                    <input type="hidden" name="itemType" value="message">
                                                    <input type="hidden" name="pageRet" value="${currentPage}">
                                                    <input type="hidden" name="statusRet" value="${currentStatusFilter}">
                                                    <c:if test="${item.status == '待审核'}">
                                                        <button type="submit" name="action" value="approve_message" class="btn btn-success btn-sm"><i class="fas fa-check"></i> 批准</button>
                                                    </c:if>
                                                    <%-- Reject for message often means delete or mark as spam --%>
                                                    <button type="submit" name="action" value="delete_message" class="btn btn-danger btn-sm" onclick="return confirmAction('确定要删除此留言吗？');"><i class="fas fa-trash"></i> 删除</button>
                                                </form>
                                            </td>
                                        </tr>
                                    </c:if>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="text-muted">没有找到符合条件的 ${itemType == 'review' ? '评价' : '留言'}。</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- Pagination Controls --%>
    <c:if test="${totalPages > 1}">
        <nav class="pagination admin-pagination">
             <c:url var="pageUrl" value="/admin/moderate">
                <c:param name="action" value="${pageAction}" />
                <c:param name="status_filter" value="${currentStatusFilter}" />
            </c:url>
            <c:choose>
                <c:when test="${currentPage > 1}"><a href="${pageUrl}&page=${currentPage - 1}">&laquo; 上一页</a></c:when>
                <c:otherwise><span class="disabled">&laquo; 上一页</span></c:otherwise>
            </c:choose>
            <c:forEach var="i" begin="1" end="${totalPages}">
                <c:choose>
                    <c:when test="${i == currentPage}"><span class="current">${i}</span></c:when>
                    <c:otherwise><a href="${pageUrl}&page=${i}">${i}</a></c:otherwise>
                </c:choose>
            </c:forEach>
            <c:choose>
                <c:when test="${currentPage < totalPages}"><a href="${pageUrl}&page=${currentPage + 1}">下一页 &raquo;</a></c:when>
                <c:otherwise><span class="disabled">下一页 &raquo;</span></c:otherwise>
            </c:choose>
        </nav>
    </c:if>
</div>

<c:set var="pageSpecificAdminStyles" scope="request">
    .admin-filter-tabs { margin-bottom: 15px; border-bottom: 2px solid #ddd; padding-bottom: 0; }
    .admin-filter-tabs a { display: inline-block; padding: 10px 20px; text-decoration: none; color: #555; font-weight: bold; border-bottom: 2px solid transparent; margin-bottom: -2px; /* Overlap border */ }
    .admin-filter-tabs a.active, .admin-filter-tabs a:hover { color: #2c3e50; border-bottom-color: #2c3e50; }
    .admin-filter-bar { margin-bottom: 20px; padding: 15px; }
    .moderation-table td, .moderation-table th { vertical-align: middle; font-size:0.9rem; }
    .moderation-table .btn-sm { margin-bottom: 5px; margin-right: 5px; }
    .status-badge { display: inline-block; padding: .3em .7em; font-size: .85em; font-weight: 700; line-height: 1; text-align: center; white-space: nowrap; vertical-align: baseline; border-radius: .25rem; }
    .status-待审核 { background-color: #f0ad4e; color: white; } /* Orange */
    .status-已批准 { background-color: #5cb85c; color: white; } /* Green */
    .status-已拒绝 { background-color: #d9534f; color: white; } /* Red */
    .fas { margin-right: 3px; }
</c:set>

<jsp:include page="/WEB-INF/jspf/admin_footer.jspf" />
