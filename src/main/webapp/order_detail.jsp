<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="order" value="${requestScope.orderDetails}" />
<c:set var="pageTitle" scope="request" value="订单详情 - ${not empty order ? order.orderId : '未找到'}" />

<jsp:include page="/WEB-INF/jspf/header.jspf" />

<div class="container order-detail-page">
    <c:choose>
        <c:when test="${not empty order}">
            <h2>订单详情: #<c:out value="${order.orderId}"/></h2>

            <div class="order-detail-grid">
                <div class="order-info-main">
                    <h4>订单信息</h4>
                    <p><strong>订单号:</strong> <c:out value="${order.orderId}"/></p>
                    <p><strong>下单日期:</strong> <fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd HH:mm:ss"/></p>
                    <p><strong>订单状态:</strong> 
                        <span class="status-badge status-${fn:toLowerCase(fn:replace(order.status, ' ', '_'))}">
                            <c:out value="${order.status}"/>
                        </span>
                    </p>
                    <p><strong>订单总金额:</strong> <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="¥"/></p>
                </div>

                <div class="order-shipping-info">
                    <h4>收货信息</h4>
                    <%-- Assuming OrderServlet might add User object to 'order' or provide 'orderUser' attribute --%>
                    <%-- For now, directly using shippingAddress from order. User details can be added if available --%>
                    <p><strong>用户ID:</strong> <c:out value="${order.userId}"/></p> 
                    <p><strong>收货地址:</strong> <c:out value="${order.shippingAddress}"/></p>
                    <%-- If User object is associated with Order (e.g., order.user.username) --%>
                    <c:if test="${not empty order.user.username}">
                         <p><strong>收货人:</strong> <c:out value="${order.user.username}"/></p>
                    </c:if>
                </div>
            </div>

            <hr class="section-divider">

            <h4>商品清单</h4>
            <c:if test="${empty order.items}">
                <p>此订单没有商品项信息。</p>
            </c:if>
            <c:if test="${not empty order.items}">
                <table class="order-items-table">
                    <thead>
                        <tr>
                            <th colspan="2">商品</th>
                            <th>购买单价</th>
                            <th>数量</th>
                            <th>小计</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="item" items="${order.items}">
                            <c:set var="product" value="${item.product}" />
                            <tr>
                                <td class="item-image">
                                    <c:if test="${not empty product.imageUrl}">
                                        <img src="${pageContext.request.contextPath}/${fn:escapeXml(product.imageUrl)}" alt="${fn:escapeXml(product.name)}">
                                    </c:if>
                                    <c:if test="${empty product.imageUrl}">
                                        <img src="${pageContext.request.contextPath}/images/placeholder.png" alt="无图片">
                                    </c:if>
                                </td>
                                <td class="item-name">
                                    <a href="${pageContext.request.contextPath}/product_detail.jsp?productId=${product.productId}"><c:out value="${product.name}"/></a>
                                </td>
                                <td><fmt:formatNumber value="${item.priceAtPurchase}" type="currency" currencySymbol="¥"/></td>
                                <td><c:out value="${item.quantity}"/></td>
                                <td><fmt:formatNumber value="${item.priceAtPurchase * item.quantity}" type="currency" currencySymbol="¥"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </c:if>
            
            <div class="order-actions-footer">
                <a href="${pageContext.request.contextPath}/order?action=list" class="btn btn-secondary">&laquo; 返回我的订单列表</a>
                <c:if test="${order.status == '已完成'}">
                    <%-- Example: Link to review products from this order --%>
                    <%-- This would require more complex logic to find which products can be reviewed --%>
                    <%-- <a href="#" class="btn btn-info">评价订单中的商品</a> --%>
                </c:if>
            </div>

        </c:when>
        <c:otherwise>
            <div class="alert alert-warning">
                <p>抱歉，无法找到指定的订单详情。</p>
                <a href="${pageContext.request.contextPath}/order?action=list" class="btn btn-secondary">查看我的订单列表</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<c:set var="pageSpecificStyles" scope="request">
    .order-detail-page h2 { text-align: center; margin-bottom: 25px; }
    .order-detail-page h4 { margin-top: 20px; margin-bottom: 10px; color: #35424a; border-bottom: 1px solid #eee; padding-bottom: 8px;}
    .order-detail-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 25px; margin-bottom: 20px; }
    .order-info-main, .order-shipping-info { background-color: #f9f9f9; padding: 20px; border-radius: 8px; }
    .order-info-main p, .order-shipping-info p { margin-bottom: 8px; line-height: 1.6; }
    .order-info-main strong, .order-shipping-info strong { color: #333; }
    .status-badge { display: inline-block; padding: .3em .7em; font-size: .9em; font-weight: 700; line-height: 1; text-align: center; white-space: nowrap; vertical-align: baseline; border-radius: .25rem; }
    .status-待付款 { background-color: #f0ad4e; color: white; }
    .status-待发货 { background-color: #5bc0de; color: white; }
    .status-已发货 { background-color: #337ab7; color: white; }
    .status-已完成 { background-color: #5cb85c; color: white; }
    .status-已取消 { background-color: #d9534f; color: white; }

    .order-items-table { width: 100%; border-collapse: collapse; margin-top: 15px; margin-bottom: 25px; }
    .order-items-table th, .order-items-table td { border: 1px solid #e0e0e0; padding: 10px 12px; text-align: left; vertical-align: middle; }
    .order-items-table th { background-color: #f8f8f8; font-weight: bold; }
    .order-items-table .item-image img { max-width: 60px; height: auto; border-radius: 4px; }
    .order-items-table .item-name a { color: #337ab7; text-decoration: none; }
    .order-items-table .item-name a:hover { text-decoration: underline; }
    .section-divider { margin-top: 25px; margin-bottom: 25px; border: 0; border-top: 1px solid #eee; }
    .order-actions-footer { margin-top: 30px; text-align: right; }
</c:set>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
