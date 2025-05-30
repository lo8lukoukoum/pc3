<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<% request.setAttribute("pageTitle", "数据统计与分析"); %>
<jsp:include page="/WEB-INF/jspf/admin_header.jspf" />

<div class="admin-page-container stats-dashboard">
    <div class="admin-page-toolbar">
        <a href="${pageContext.request.contextPath}/admin/stats?action=export_sales_report" class="btn btn-info"><i class="fas fa-file-csv"></i> 导出销售报告 (CSV)</a>
    </div>

    <%-- Key Metric Cards --%>
    <div class="dashboard-cards-row">
        <div class="dashboard-card stat-card">
            <h4>总销售额</h4>
            <p class="count"><fmt:formatNumber value="${totalSales}" type="currency" currencySymbol="¥" minFractionDigits="2" maxFractionDigits="2"/></p>
            <small>所有已完成订单</small>
        </div>
        <div class="dashboard-card stat-card">
            <h4>近30天销售额</h4>
            <p class="count"><fmt:formatNumber value="${salesLast30Days}" type="currency" currencySymbol="¥" minFractionDigits="2" maxFractionDigits="2"/></p>
            <small>已完成订单</small>
        </div>
        <div class="dashboard-card stat-card">
            <h4>近30天新注册用户</h4>
            <p class="count"><c:out value="${newCustomersLast30Days}"/></p>
            <small>新用户数量</small>
        </div>
    </div>

    <div class="dashboard-charts-row">
        <%-- Order Status Distribution --%>
        <div class="card chart-card">
            <div class="card-header">订单状态分布</div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty orderCountByStatus}">
                        <canvas id="orderStatusChart" height="180"></canvas> <%-- Adjusted height --%>
                    </c:when>
                    <c:otherwise><p class="text-muted">暂无订单状态数据。</p></c:otherwise>
                </c:choose>
            </div>
        </div>

        <%-- Top Selling Products --%>
        <div class="card list-card">
            <div class="card-header">热门销售商品 (Top 5)</div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty topSellingProducts}">
                        <ul class="admin-list-group">
                            <c:forEach var="product" items="${topSellingProducts}">
                                <li class="list-group-item">
                                    <c:out value="${product.name}"/>
                                    <span class="float-right">销量: <c:out value="${product.total_quantity_sold}"/> | 销售额: <fmt:formatNumber value="${product.total_revenue}" type="currency" currencySymbol="¥"/></span>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:when>
                    <c:otherwise><p class="text-muted">暂无热门销售商品数据。</p></c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    
    <div class="dashboard-lists-row">
         <%-- Top Viewed Products --%>
        <div class="card list-card">
            <div class="card-header">热门浏览商品 (Top 5)</div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty topViewedProducts}">
                        <ul class="admin-list-group">
                            <c:forEach var="product" items="${topViewedProducts}">
                                <li class="list-group-item">
                                    <c:out value="${product.name}"/>
                                    <span class="float-right">浏览量: <c:out value="${product.views}"/></span>
                                </li>
                            </c:forEach>
                        </ul>
                    </c:when>
                    <c:otherwise><p class="text-muted">暂无热门浏览商品数据。</p></c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</div>

<%-- Include Chart.js CDN and script for chart rendering --%>
<c:if test="${not empty orderCountByStatus}">
    <c:set var="pageSpecificAdminScripts" scope="request">
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script>
            document.addEventListener('DOMContentLoaded', function() {
                const ctx = document.getElementById('orderStatusChart').getContext('2d');
                const orderStatusData = {
                    labels: [<c:forEach items="${orderCountByStatus}" var="entry" varStatus="loop">"${fn:escapeXml(entry.key)}"<c:if test="${!loop.last}">,</c:if></c:forEach>],
                    datasets: [{
                        label: '订单数量',
                        data: [<c:forEach items="${orderCountByStatus}" var="entry" varStatus="loop">${entry.value}<c:if test="${!loop.last}">,</c:if></c:forEach>],
                        backgroundColor: [
                            'rgba(255, 99, 132, 0.7)',  // 待付款 (假设) - Red
                            'rgba(54, 162, 235, 0.7)', // 待发货 (假设) - Blue
                            'rgba(255, 206, 86, 0.7)', // 已发货 (假设) - Yellow
                            'rgba(75, 192, 192, 0.7)', // 已完成 (假设) - Green
                            'rgba(153, 102, 255, 0.7)',// 已取消 (假设) - Purple
                            'rgba(255, 159, 64, 0.7)'  // 其他状态 - Orange
                        ],
                        borderColor: [
                            'rgba(255, 99, 132, 1)',
                            'rgba(54, 162, 235, 1)',
                            'rgba(255, 206, 86, 1)',
                            'rgba(75, 192, 192, 1)',
                            'rgba(153, 102, 255, 1)',
                            'rgba(255, 159, 64, 1)'
                        ],
                        borderWidth: 1
                    }]
                };
                new Chart(ctx, {
                    type: 'pie', // or 'doughnut' or 'bar'
                    data: orderStatusData,
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: 'top',
                            },
                            title: {
                                display: false,
                                text: '订单状态分布图'
                            }
                        }
                    }
                });
            });
        </script>
    </c:set>
</c:if>

<c:set var="pageSpecificAdminStyles" scope="request">
    .stats-dashboard .admin-page-toolbar { margin-bottom: 20px; text-align: right; }
    .dashboard-cards-row { display: flex; flex-wrap: wrap; gap: 20px; margin-bottom: 25px; }
    .dashboard-card.stat-card {
        background-color: #fff;
        padding: 20px 25px;
        border-radius: 8px;
        box-shadow: 0 2px 5px rgba(0,0,0,0.08);
        flex: 1;
        min-width: 220px; /* Ensure cards don't get too small */
        text-align: left;
    }
    .stat-card h4 { margin-top: 0; font-size: 1rem; color: #666; margin-bottom: 8px; }
    .stat-card .count { font-size: 2.2rem; font-weight: bold; color: #333; margin-bottom: 5px; }
    .stat-card small { color: #888; font-size: 0.85rem; }

    .dashboard-charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: 25px; margin-bottom: 25px; }
    .dashboard-lists-row { display: grid; grid-template-columns: 1fr; gap: 25px; margin-bottom: 25px; } /* Example for single column list */

    @media (max-width: 992px) {
        .dashboard-charts-row { grid-template-columns: 1fr; } /* Stack charts on smaller screens */
    }
    .chart-card .card-body, .list-card .card-body { padding: 15px; min-height:300px} /* Ensure body has padding for canvas */
    #orderStatusChart { max-height: 300px; } /* Control chart height */
    .admin-list-group { list-style: none; padding: 0; margin: 0; }
    .admin-list-group .list-group-item { 
        padding: 10px 15px; 
        border-bottom: 1px solid #f0f0f0; 
        display: flex; 
        justify-content: space-between; 
        align-items: center;
        font-size: 0.9rem;
    }
    .admin-list-group .list-group-item:last-child { border-bottom: none; }
    .admin-list-group .list-group-item .float-right { color: #555; font-size:0.85rem; }
    .fas { margin-right: 5px; }
</c:set>

<jsp:include page="/WEB-INF/jspf/admin_footer.jspf" />
