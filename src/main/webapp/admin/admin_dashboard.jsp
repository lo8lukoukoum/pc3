<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<% 
    // This page might not set a specific title if admin_header.jspf defaults to "仪表盘"
    // or if the StatsServlet sets it when action=view is for dashboard.
    // If this page is directly accessed, "仪表盘" is a good default.
    request.setAttribute("pageTitle", "管理仪表盘"); 
%>
<jsp:include page="/WEB-INF/jspf/admin_header.jspf" />

<%-- Admin Dashboard Content --%>
<div class="dashboard-intro">
    <p>欢迎回来, <strong><c:out value="${sessionScope.loggedInUser.username}"/></strong>! 请从左侧导航栏选择一项开始管理。</p>
</div>

<%-- 
    This dashboard can be populated with actual stats by AdminStatsServlet
    if this JSP is the target of /admin/stats?action=view.
    Alternatively, it can make AJAX calls or just have links.
    For now, it's a simple welcome page with links.
--%>

<div class="dashboard-cards">
    <div class="dashboard-card">
        <h4>总销售额</h4>
        <p class="count"><c:out value="${not empty totalSales ? totalSales : 'N/A'}"/></p>
        <a href="${pageContext.request.contextPath}/admin/stats?action=view">查看销售统计</a>
    </div>
    <div class="dashboard-card">
        <h4>待处理订单</h4>
        <%-- Example: Assuming orderCountByStatus is available from StatsServlet if this is the stats view --%>
        <p class="count"><c:out value="${not empty orderCountByStatus['待付款'] ? orderCountByStatus['待付款'] : (not empty orderCountByStatus['待发货'] ? orderCountByStatus['待发货'] : 'N/A')}"/></p>
        <a href="${pageContext.request.contextPath}/admin/orders?action=list&status_filter=待付款">管理订单</a>
    </div>
    <div class="dashboard-card">
        <h4>待审核评价</h4>
        <%-- This would require ReviewDAO.getTotalReviewCountByStatus("待审核") passed by a servlet --%>
        <p class="count">${not empty pendingReviewCount ? pendingReviewCount : 'N/A'}</p>
        <a href="${pageContext.request.contextPath}/admin/moderate?action=list_reviews&status_filter=待审核">审核评价</a>
    </div>
    <div class="dashboard-card">
        <h4>商品总数</h4>
        <%-- This would require ProductDAO.getTotalProductCount() passed by a servlet --%>
        <p class="count">${not empty totalProductCount ? totalProductCount : 'N/A'}</p>
        <a href="${pageContext.request.contextPath}/admin/products?action=list">管理商品</a>
    </div>
</div>

<div class="quick-links">
    <h3>快速操作</h3>
    <ul>
        <li><a href="${pageContext.request.contextPath}/admin/products?action=add_form" class="btn btn-primary">添加新商品</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/orders?action=list&status_filter=待发货" class="btn">查看待发货订单</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/customers?action=list" class="btn">客户列表</a></li>
    </ul>
</div>


<%-- If this page is specifically the target for /admin/stats?action=view, 
     then the attributes set by AdminStatsServlet would be directly available here.
     The structure above assumes some data might be present.
     If this is just a generic dashboard, it might only contain links.
--%>
<c:if test="${param.action == 'view' && fn:contains(pageContext.request.servletPath, '/admin/stats')}">
    <%-- This message indicates that the stats data should be on this page from AdminStatsServlet --%>
    <div class="alert alert-info" style="margin-top: 20px;">
        详细统计数据已加载。 (totalSales: ${totalSales}, salesLast30Days: ${salesLast30Days}, etc.)
    </div>
</c:if>


<jsp:include page="/WEB-INF/jspf/admin_footer.jspf" />
