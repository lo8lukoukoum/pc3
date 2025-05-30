<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<% request.setAttribute("pageTitle", "订单管理"); %>
<jsp:include page="/WEB-INF/jspf/admin_header.jspf" />

<div class="admin-page-container">
    <%-- Order Filter Section --%>
    <div class="admin-filter-bar card">
        <form action="${pageContext.request.contextPath}/admin/orders" method="GET" class="form-inline">
            <input type="hidden" name="action" value="list">
            <div class="form-group">
                <label for="status_filter">筛选订单状态:</label>
                <select name="status_filter" id="status_filter" class="form-control" onchange="this.form.submit()">
                    <option value="" ${empty currentStatusFilter ? 'selected' : ''}>所有订单</option>
                    <option value="待付款" ${currentStatusFilter == '待付款' ? 'selected' : ''}>待付款</option>
                    <option value="待发货" ${currentStatusFilter == '待发货' ? 'selected' : ''}>待发货</option>
                    <option value="已发货" ${currentStatusFilter == '已发货' ? 'selected' : ''}>已发货</option>
                    <option value="已完成" ${currentStatusFilter == '已完成' ? 'selected' : ''}>已完成</option>
                    <option value="已取消" ${currentStatusFilter == '已取消' ? 'selected' : ''}>已取消</option>
                </select>
            </div>
            <%-- <button type="submit" class="btn btn-primary btn-sm">筛选</button> --%>
            <c:if test="${not empty currentStatusFilter}">
                 <a href="${pageContext.request.contextPath}/admin/orders?action=list" class="btn btn-secondary btn-sm">清除筛选</a>
            </c:if>
        </form>
        <c:if test="${promptForFilter}">
            <p class="text-info" style="margin-top:10px;">提示: 请选择一个状态进行筛选，或在OrderDAO中实现无筛选获取所有订单的功能。</p>
        </c:if>
    </div>

    <%-- Orders Table --%>
    <div class="card">
        <div class="card-header">
            订单列表 <c:if test="${not empty currentStatusFilter}">(${currentStatusFilter})</c:if>
        </div>
        <div class="card-body">
            <c:choose>
                <c:when test="${not empty orderList}">
                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>订单ID</th>
                                    <th>用户ID</th>
                                    <th>下单日期</th>
                                    <th>总金额</th>
                                    <th>状态</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="order" items="${orderList}">
                                    <tr>
                                        <td><c:out value="${order.orderId}"/></td>
                                        <td><c:out value="${order.userId}"/></td> <%-- TODO: Link to customer details or show username --%>
                                        <td><fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd HH:mm"/></td>
                                        <td><fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="¥"/></td>
                                        <td>
                                            <span class="status-badge status-${fn:toLowerCase(fn:replace(order.status, ' ', '_'))}">
                                                <c:out value="${order.status}"/>
                                            </span>
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/orders?action=detail&orderId=${order.orderId}" class="btn btn-info btn-sm">详情</a>
                                            <form action="${pageContext.request.contextPath}/admin/orders" method="POST" style="display:inline;" onsubmit="return confirmAction('确定要删除订单 #${order.orderId} 吗？这也会删除关联的订单项。');">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="orderId" value="${order.orderId}">
                                                <input type="hidden" name="pageRet" value="${currentPage}">
                                                <input type="hidden" name="statusRet" value="${currentStatusFilter}">
                                                <button type="submit" class="btn btn-danger btn-sm">删除</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="text-muted">没有找到符合条件的订单。</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- Pagination Controls --%>
    <c:if test="${totalPages > 1}">
        <nav class="pagination admin-pagination">
            <c:choose>
                <c:when test="${currentPage > 1}">
                    <a href="${pageContext.request.contextPath}/admin/orders?action=list&status_filter=${fn:escapeXml(currentStatusFilter)}&page=${currentPage - 1}">&laquo; 上一页</a>
                </c:when>
                <c:otherwise><span class="disabled">&laquo; 上一页</span></c:otherwise>
            </c:choose>
            <c:forEach var="i" begin="1" end="${totalPages}">
                <c:choose>
                    <c:when test="${i == currentPage}"><span class="current">${i}</span></c:when>
                    <c:otherwise><a href="${pageContext.request.contextPath}/admin/orders?action=list&status_filter=${fn:escapeXml(currentStatusFilter)}&page=${i}">${i}</a></c:otherwise>
                </c:choose>
            </c:forEach>
            <c:choose>
                <c:when test="${currentPage < totalPages}">
                    <a href="${pageContext.request.contextPath}/admin/orders?action=list&status_filter=${fn:escapeXml(currentStatusFilter)}&page=${currentPage + 1}">下一页 &raquo;</a>
                </c:when>
                <c:otherwise><span class="disabled">下一页 &raquo;</span></c:otherwise>
            </c:choose>
        </nav>
    </c:if>
</div>

<c:set var="pageSpecificAdminStyles" scope="request">
    .admin-filter-bar { margin-bottom: 20px; padding: 15px; }
    .admin-filter-bar .form-inline .form-group { margin-right: 15px; }
    .admin-filter-bar .form-control { height: calc(1.5em + .75rem + 2px); padding: .375rem .75rem; } /* Align select height */
    .card { border: 1px solid #e0e0e0; border-radius: .25rem; margin-bottom: 20px; box-shadow: 0 0.125rem 0.25rem rgba(0,0,0,.075); }
    .card-header { padding: .75rem 1.25rem; margin-bottom: 0; background-color: rgba(0,0,0,.03); border-bottom: 1px solid rgba(0,0,0,.125); font-weight: bold; }
    .card-body { padding: 1.25rem; }
    .table-responsive { display: block; width: 100%; overflow-x: auto; -webkit-overflow-scrolling: touch; }
    .status-badge { display: inline-block; padding: .3em .7em; font-size: .85em; font-weight: 700; line-height: 1; text-align: center; white-space: nowrap; vertical-align: baseline; border-radius: .25rem; }
    .status-待付款 { background-color: #f0ad4e; color: white; }
    .status-待发货 { background-color: #5bc0de; color: white; }
    .status-已发货 { background-color: #337ab7; color: white; }
    .status-已完成 { background-color: #5cb85c; color: white; }
    .status-已取消 { background-color: #d9534f; color: white; }
    .admin-pagination { margin-top: 20px; }
</c:set>

<jsp:include page="/WEB-INF/jspf/admin_footer.jspf" />
