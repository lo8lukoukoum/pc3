<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="order" value="${requestScope.orderDetails}" />
<c:set var="pageTitle" scope="request" value="订单详情 #${not empty order ? order.orderId : '未找到'}" />

<jsp:include page="/WEB-INF/jspf/admin_header.jspf" />

<div class="admin-page-container">
    <c:choose>
        <c:when test="${not empty order}">
            <div class="card">
                <div class="card-header">
                    订单 #${order.orderId} - 详细信息
                    <a href="${pageContext.request.contextPath}/admin/orders?action=list&status_filter=${fn:escapeXml(param.statusRet)}&page=${fn:escapeXml(param.pageRet)}" class="btn btn-secondary btn-sm float-right">&laquo; 返回订单列表</a>
                </div>
                <div class="card-body">
                    <div class="order-detail-grid-admin">
                        <div class="order-info-main-admin">
                            <h4><i class="fas fa-file-invoice-dollar"></i> 订单概要</h4>
                            <p><strong>订单ID:</strong> <c:out value="${order.orderId}"/></p>
                            <p><strong>用户ID:</strong> <c:out value="${order.userId}"/> 
                                <%-- Optional: Link to customer detail if available 
                                <a href="${pageContext.request.contextPath}/admin/customers?action=edit_form&userId=${order.userId}">(查看用户)</a> 
                                --%>
                            </p>
                            <p><strong>下单日期:</strong> <fmt:formatDate value="${order.orderDate}" pattern="yyyy-MM-dd HH:mm:ss"/></p>
                            <p><strong>订单总金额:</strong> <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="¥"/></p>
                            <p><strong>当前状态:</strong> 
                                <span class="status-badge status-${fn:toLowerCase(fn:replace(order.status, ' ', '_'))}">
                                    <c:out value="${order.status}"/>
                                </span>
                            </p>
                        </div>

                        <div class="order-shipping-info-admin">
                            <h4><i class="fas fa-shipping-fast"></i> 收货信息</h4>
                            <p><strong>收货地址:</strong> <c:out value="${order.shippingAddress}"/></p>
                            <%-- Assuming other shipping details like name, phone might be part of shippingAddress or separate fields if added to Order entity --%>
                        </div>
                    </div>

                    <hr class="section-divider-admin">
                    
                    <div class="order-status-update-admin">
                        <h4><i class="fas fa-edit"></i> 更新订单状态</h4>
                        <form action="${pageContext.request.contextPath}/admin/orders" method="POST" class="form-inline">
                            <input type="hidden" name="action" value="update_status">
                            <input type="hidden" name="orderId" value="${order.orderId}">
                            <input type="hidden" name="pageRet" value="${param.pageRet}"> <%-- For redirecting back to same list page --%>
                            <input type="hidden" name="statusRet" value="${param.statusRet}">
                            <div class="form-group">
                                <label for="newStatus">新状态:</label>
                                <select name="newStatus" id="newStatus" class="form-control">
                                    <option value="待付款" ${order.status == '待付款' ? 'selected' : ''}>待付款</option>
                                    <option value="待发货" ${order.status == '待发货' ? 'selected' : ''}>待发货</option>
                                    <option value="已发货" ${order.status == '已发货' ? 'selected' : ''}>已发货</option>
                                    <option value="已完成" ${order.status == '已完成' ? 'selected' : ''}>已完成</option>
                                    <option value="已取消" ${order.status == '已取消' ? 'selected' : ''}>已取消</option>
                                </select>
                            </div>
                            <button type="submit" class="btn btn-primary btn-sm">更新状态</button>
                        </form>
                    </div>

                    <hr class="section-divider-admin">

                    <h4><i class="fas fa-boxes"></i> 商品清单</h4>
                    <c:if test="${empty order.items}">
                        <p class="text-muted">此订单似乎没有商品项。</p>
                    </c:if>
                    <c:if test="${not empty order.items}">
                        <div class="table-responsive">
                            <table class="admin-table order-items-table-admin">
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
                                            <td class="item-image-admin">
                                                <c:if test="${not empty product.imageUrl}">
                                                    <img src="${pageContext.request.contextPath}/${fn:escapeXml(product.imageUrl)}" alt="${fn:escapeXml(product.name)}">
                                                </c:if>
                                                <c:if test="${empty product.imageUrl}">
                                                    <img src="${pageContext.request.contextPath}/images/placeholder.png" alt="无图片">
                                                </c:if>
                                            </td>
                                            <td class="item-name-admin">
                                                <c:out value="${product.name}"/> (ID: ${product.productId})
                                            </td>
                                            <td><fmt:formatNumber value="${item.priceAtPurchase}" type="currency" currencySymbol="¥"/></td>
                                            <td><c:out value="${item.quantity}"/></td>
                                            <td><fmt:formatNumber value="${item.priceAtPurchase * item.quantity}" type="currency" currencySymbol="¥"/></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:if>
                </div>
            </div>
        </c:when>
        <c:otherwise>
            <div class="alert alert-warning">无法加载订单详情。该订单可能不存在。</div>
        </c:otherwise>
    </c:choose>
</div>

<c:set var="pageSpecificAdminStyles" scope="request">
    .order-detail-grid-admin { display: grid; grid-template-columns: repeat(auto-fit, minmax(320px, 1fr)); gap: 20px; margin-bottom: 20px; }
    .order-info-main-admin, .order-shipping-info-admin, .order-status-update-admin { background-color: #fff; padding: 20px; border-radius: 5px; box-shadow: 0 1px 2px rgba(0,0,0,0.05); }
    .order-info-main-admin h4, .order-shipping-info-admin h4, .order-status-update-admin h4 { margin-top: 0; margin-bottom: 15px; color: #333; border-bottom: 1px solid #f0f0f0; padding-bottom: 10px; font-size: 1.1rem; }
    .order-info-main-admin p, .order-shipping-info-admin p { margin-bottom: 8px; line-height: 1.6; font-size: 0.9rem; }
    .order-info-main-admin strong, .order-shipping-info-admin strong { color: #555; }
    .status-badge { display: inline-block; padding: .3em .7em; font-size: .9em; font-weight: 700; line-height: 1; text-align: center; white-space: nowrap; vertical-align: baseline; border-radius: .25rem; }
    .status-待付款 { background-color: #f0ad4e; color: white; }
    .status-待发货 { background-color: #5bc0de; color: white; }
    .status-已发货 { background-color: #337ab7; color: white; }
    .status-已完成 { background-color: #5cb85c; color: white; }
    .status-已取消 { background-color: #d9534f; color: white; }
    .order-status-update-admin .form-inline .form-group { margin-right: 10px; }
    .order-status-update-admin .form-control { height: calc(1.5em + .75rem + 2px); }
    .section-divider-admin { margin: 25px 0; border: 0; border-top: 1px solid #e5e5e5; }
    .order-items-table-admin .item-image-admin img { max-width: 50px; height: auto; border-radius: 3px; }
    .order-items-table-admin .item-name-admin { font-size: 0.9rem; }
    .float-right { float: right; }
    .card .card-header .btn-sm { margin-top: -4px; } /* Align button in card header */
    .fas { margin-right: 5px; } /* FontAwesome icon spacing */
</c:set>

<jsp:include page="/WEB-INF/jspf/admin_footer.jspf" />
