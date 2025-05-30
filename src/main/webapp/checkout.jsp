<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<% request.setAttribute("pageTitle", "订单确认与结算"); %>
<jsp:include page="/WEB-INF/jspf/header.jspf" />

<div class="container checkout-page">
    <h2>订单确认与结算</h2>

    <c:if test="${empty cartForCheckout || empty cartForCheckout.items}">
        <div class="alert alert-warning" role="alert">
            您的购物车是空的，或者无法加载购物车信息进行结算。
            <a href="${pageContext.request.contextPath}/cart?action=view">返回购物车</a>
        </div>
    </c:if>

    <c:if test="${not empty cartForCheckout && not empty cartForCheckout.items}">
        <div class="checkout-grid">
            <div class="order-summary-section">
                <h3>订单概要</h3>
                <table class="order-summary-table">
                    <thead>
                        <tr>
                            <th>商品名称</th>
                            <th>数量</th>
                            <th>单价</th>
                            <th>小计</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:set var="checkoutTotalAmount" value="0" />
                        <c:forEach var="item" items="${cartForCheckout.items}">
                            <c:set var="product" value="${item.product}" />
                            <c:set var="itemSubtotal" value="${product.price * item.quantity}" />
                            <c:set var="checkoutTotalAmount" value="${checkoutTotalAmount + itemSubtotal}" />
                            <tr>
                                <td><c:out value="${product.name}"/></td>
                                <td><c:out value="${item.quantity}"/></td>
                                <td><fmt:formatNumber value="${product.price}" type="currency" currencySymbol="¥"/></td>
                                <td><fmt:formatNumber value="${itemSubtotal}" type="currency" currencySymbol="¥"/></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                    <tfoot>
                        <tr>
                            <td colspan="3" style="text-align:right; font-weight:bold;">订单总金额:</td>
                            <td style="font-weight:bold;"><fmt:formatNumber value="${checkoutTotalAmount}" type="currency" currencySymbol="¥"/></td>
                        </tr>
                    </tfoot>
                </table>
            </div>

            <div class="shipping-info-section">
                <h3>收货信息</h3>
                 <%-- Display any error messages related to form submission --%>
                <c:if test="${not empty requestScope.errorMessage && fn:contains(requestScope.errorMessage, '收货地址')}">
                    <div class="alert alert-danger" role="alert">
                        <c:out value="${requestScope.errorMessage}"/>
                    </div>
                </c:if>

                <form action="${pageContext.request.contextPath}/order" method="POST" id="checkout-form">
                    <input type="hidden" name="action" value="create_order">
                    
                    <div class="form-group">
                        <label for="fullName">收货人姓名:</label>
                        <input type="text" id="fullName" name="fullName" class="form-control" 
                               value="${fn:escapeXml(sessionScope.loggedInUser.username)}" required 
                               placeholder="例如：张三">
                    </div>

                    <div class="form-group">
                        <label for="contactPhone">联系电话:</label>
                        <input type="tel" id="contactPhone" name="contactPhone" class="form-control" 
                               placeholder="例如：13800138000 (可选)">
                    </div>
                    
                    <div class="form-group">
                        <label for="shippingAddress">详细收货地址:</label>
                        <textarea id="shippingAddress" name="shippingAddress" rows="4" class="form-control" 
                                  required placeholder="请输入省、市、区、街道名称及门牌号">${fn:escapeXml(requestScope.shippingAddress)}</textarea>
                    </div>

                    <div class="form-group">
                        <label for="orderNotes">订单备注 (可选):</label>
                        <textarea id="orderNotes" name="orderNotes" rows="3" class="form-control" 
                                  placeholder="如有特殊要求，请在此填写"></textarea>
                    </div>
                    
                    <%-- Payment method (simplified) --%>
                    <div class="form-group">
                        <label>支付方式:</label>
                        <div class="payment-method-display">
                            在线支付 (模拟 - 订单将标记为“待付款”)
                        </div>
                    </div>

                    <div class="form-group-submit">
                        <button type="submit" class="btn btn-primary btn-lg btn-submit-order">提交订单</button>
                    </div>
                </form>
            </div>
        </div>
    </c:if>
</div>

<c:set var="pageSpecificStyles" scope="request">
    .checkout-page h2 { text-align: center; margin-bottom: 25px; }
    .checkout-grid { display: grid; grid-template-columns: 1.2fr 1fr; gap: 30px; }
    @media (max-width: 992px) { .checkout-grid { grid-template-columns: 1fr; } }
    
    .order-summary-section h3, .shipping-info-section h3 { margin-bottom: 20px; color: #333; border-bottom: 1px solid #eee; padding-bottom: 10px;}
    .order-summary-table { width: 100%; border-collapse: collapse; font-size: 0.95em; }
    .order-summary-table th, .order-summary-table td { padding: 10px 8px; border-bottom: 1px solid #f0f0f0; text-align: left; }
    .order-summary-table th { background-color: #f9f9f9; }
    .order-summary-table tfoot td { font-size: 1.1em; padding-top: 15px; }

    #checkout-form .form-control {
        width: 100%;
        padding: 10px;
        margin-bottom: 15px;
        border: 1px solid #ccc;
        border-radius: 4px;
        box-sizing: border-box;
    }
    #checkout-form textarea.form-control { min-height: 80px; }
    .payment-method-display { padding: 10px; background-color: #f0f0f0; border-radius: 4px; color: #555; }
    .form-group-submit { text-align: right; margin-top: 20px; }
    .btn-submit-order { padding: 12px 30px; font-size: 1.1em; }
</c:set>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
