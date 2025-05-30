<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<% request.setAttribute("pageTitle", "我的订单"); %>
<jsp:include page="/WEB-INF/jspf/header.jspf" />

<div class="container order-list-page">
    <h2>我的订单历史</h2>

    <c:if test="${empty orderList}">
        <div class="empty-list-message">
            <p>您目前还没有任何订单。</p>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">去逛逛商品 &raquo;</a>
        </div>
    </c:if>

    <c:if test="${not empty orderList}">
        <div class="orders-table-container">
            <table class="orders-table">
                <thead>
                    <tr>
                        <th>订单号</th>
                        <th>下单日期</th>
                        <th>订单总金额</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="order" items="${orderList}">
                        <tr>
                            <td><a href="${pageContext.request.contextPath}/order?action=detail&orderId=${order.orderId}"><c:out value="${order.orderId}"/></a></td>
                            <td><fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                            <td><fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="¥"/></td>
                            <td>
                                <span class="status-badge status-${fn:toLowerCase(fn:replace(order.status, ' ', '_'))}">
                                    <c:out value="${order.status}"/>
                                </span>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/order?action=detail&orderId=${order.orderId}" class="btn btn-sm btn-info">查看详情</a>
                                <%-- Example for conditional buttons based on status --%>
                                <c:if test="${order.status == '待付款'}">
                                    <%-- <a href="#" class="btn btn-sm btn-success">去支付</a> --%>
                                </c:if>
                                <c:if test="${order.status == '已发货'}">
                                     <%-- <a href="#" class="btn btn-sm btn-warning">确认收货</a> --%>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>
        </div>

        <%-- Pagination Controls --%>
        <c:if test="${totalPages > 1}">
            <nav class="pagination">
                <c:choose>
                    <c:when test="${currentPage > 1}">
                        <a href="${pageContext.request.contextPath}/order?action=list&page=${currentPage - 1}">&laquo; 上一页</a>
                    </c:when>
                    <c:otherwise>
                        <span class="disabled">&laquo; 上一页</span>
                    </c:otherwise>
                </c:choose>

                <c:forEach var="i" begin="1" end="${totalPages}">
                    <c:choose>
                        <c:when test="${i == currentPage}">
                            <span class="current">${i}</span>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/order?action=list&page=${i}">${i}</a>
                        </c:otherwise>
                    </c:choose>
                </c:forEach>

                <c:choose>
                    <c:when test="${currentPage < totalPages}">
                        <a href="${pageContext.request.contextPath}/order?action=list&page=${currentPage + 1}">下一页 &raquo;</a>
                    </c:when>
                    <c:otherwise>
                        <span class="disabled">下一页 &raquo;</span>
                    </c:otherwise>
                </c:choose>
            </nav>
        </c:if>
    </c:if>
</div>

<c:set var="pageSpecificStyles" scope="request">
    .order-list-page h2 { text-align: center; margin-bottom: 25px; }
    .empty-list-message { text-align: center; padding: 40px 20px; background-color: #f9f9f9; border-radius: 8px; }
    .empty-list-message p { font-size: 1.2em; margin-bottom: 20px; }
    .orders-table-container { overflow-x: auto; /* For responsiveness on small screens */ }
    .orders-table { width: 100%; border-collapse: collapse; margin-bottom: 25px; min-width: 600px; /* Ensure table has min width */ }
    .orders-table th, .orders-table td { border: 1px solid #e0e0e0; padding: 12px 15px; text-align: left; vertical-align: middle; }
    .orders-table th { background-color: #f8f8f8; font-weight: bold; }
    .orders-table td a { color: #337ab7; text-decoration: none; }
    .orders-table td a:hover { text-decoration: underline; }
    .orders-table .btn-sm { padding: 5px 10px; font-size: 0.85em; margin-right: 5px; }
    .status-badge { display: inline-block; padding: .3em .7em; font-size: .85em; font-weight: 700; line-height: 1; text-align: center; white-space: nowrap; vertical-align: baseline; border-radius: .25rem; }
    .status-待付款 { background-color: #f0ad4e; color: white; } /* Orange */
    .status-待发货 { background-color: #5bc0de; color: white; } /* Blue */
    .status-已发货 { background-color: #337ab7; color: white; } /* Darker Blue */
    .status-已完成 { background-color: #5cb85c; color: white; } /* Green */
    .status-已取消 { background-color: #d9534f; color: white; } /* Red */
</c:set>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
