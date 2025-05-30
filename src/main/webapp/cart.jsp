<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<% request.setAttribute("pageTitle", "我的购物车"); %>
<jsp:include page="/WEB-INF/jspf/header.jspf" />

<div class="container cart-page">
    <h2>我的购物车</h2>

    <c:if test="${empty cart || empty cart.items}">
        <div class="empty-cart-message">
            <p>您的购物车中还没有商品哦！</p>
            <a href="${pageContext.request.contextPath}/products" class="btn btn-primary">马上去购物 &raquo;</a>
        </div>
    </c:if>

    <c:if test="${not empty cart && not empty cart.items}">
        <form id="cart-form" action="${pageContext.request.contextPath}/cart" method="POST"> <%-- General form for updates/clear --%>
            <input type="hidden" name="action" id="cart-action" value=""> <%-- Action will be set by JS or specific buttons --%>
            <input type="hidden" name="productId" id="cart-product-id" value="">

            <table class="cart-table">
                <thead>
                    <tr>
                        <th colspan="2">商品</th>
                        <th>单价</th>
                        <th>数量</th>
                        <th>小计</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <c:set var="cartTotalAmount" value="0" />
                    <c:forEach var="item" items="${cart.items}">
                        <c:set var="product" value="${item.product}" />
                        <c:set var="itemSubtotal" value="${product.price * item.quantity}" />
                        <c:set var="cartTotalAmount" value="${cartTotalAmount + itemSubtotal}" />
                        <tr>
                            <td class="cart-item-image">
                                <a href="${pageContext.request.contextPath}/product_detail.jsp?productId=${product.productId}">
                                    <c:choose>
                                        <c:when test="${not empty product.imageUrl}">
                                            <img src="${pageContext.request.contextPath}/${fn:escapeXml(product.imageUrl)}" alt="${fn:escapeXml(product.name)}">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${pageContext.request.contextPath}/images/placeholder.png" alt="无图片">
                                        </c:otherwise>
                                    </c:choose>
                                </a>
                            </td>
                            <td class="cart-item-name">
                                <a href="${pageContext.request.contextPath}/product_detail.jsp?productId=${product.productId}">
                                    <c:out value="${product.name}"/>
                                </a>
                                <p class="text-muted small">库存: ${product.stockQuantity > 0 ? product.stockQuantity : '无货'}</p>
                            </td>
                            <td class="cart-item-price"><fmt:formatNumber value="${product.price}" type="currency" currencySymbol="¥"/></td>
                            <td class="cart-item-quantity">
                                <form action="${pageContext.request.contextPath}/cart" method="POST" style="display: inline;">
                                    <input type="hidden" name="action" value="update">
                                    <input type="hidden" name="productId" value="${product.productId}">
                                    <input type="number" name="quantity" value="${item.quantity}" min="1" 
                                           max="${product.stockQuantity > 0 ? product.stockQuantity : item.quantity}" 
                                           class="quantity-input" onchange="this.form.submit()">
                                    <%-- Small button for update, or rely on onchange --%>
                                    <%-- <button type="submit" class="btn btn-sm btn-update-qty">更新</button> --%>
                                </form>
                            </td>
                            <td class="cart-item-subtotal"><fmt:formatNumber value="${itemSubtotal}" type="currency" currencySymbol="¥"/></td>
                            <td class="cart-item-action">
                                <form action="${pageContext.request.contextPath}/cart" method="POST" style="display: inline;">
                                    <input type="hidden" name="action" value="remove">
                                    <input type="hidden" name="productId" value="${product.productId}">
                                    <button type="submit" class="btn btn-danger btn-sm" onclick="return confirmAction('确定要从购物车移除此商品吗？');">&times; 移除</button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                </tbody>
            </table>

            <div class="cart-summary">
                <div class="cart-total">
                    <strong>总计: <fmt:formatNumber value="${cartTotalAmount}" type="currency" currencySymbol="¥"/></strong>
                </div>
                <div class="cart-actions">
                    <button type="button" class="btn btn-warning" 
                            onclick="if(confirmAction('确定要清空购物车吗？')) { document.getElementById('cart-action').value='clear'; document.getElementById('cart-form').submit(); }">
                        清空购物车
                    </button>
                    <a href="${pageContext.request.contextPath}/order?action=checkout_page" class="btn btn-primary btn-checkout">去结算 &raquo;</a>
                </div>
            </div>
        </form>
    </c:if>
</div>

<c:set var="pageSpecificStyles" scope="request">
    .cart-page h2 { text-align: center; margin-bottom: 25px; }
    .empty-cart-message { text-align: center; padding: 40px 20px; background-color: #f9f9f9; border-radius: 8px; }
    .empty-cart-message p { font-size: 1.2em; margin-bottom: 20px; }
    .cart-table { width: 100%; border-collapse: collapse; margin-bottom: 25px; }
    .cart-table th, .cart-table td { border: 1px solid #e0e0e0; padding: 12px 15px; text-align: left; vertical-align: middle; }
    .cart-table th { background-color: #f8f8f8; font-weight: bold; }
    .cart-item-image img { max-width: 80px; height: auto; border-radius: 4px; }
    .cart-item-name a { font-weight: bold; color: #337ab7; text-decoration: none; }
    .cart-item-name a:hover { text-decoration: underline; }
    .cart-item-name .text-muted { font-size: 0.85em; }
    .cart-item-quantity .quantity-input { width: 70px; text-align: center; padding: 6px; border: 1px solid #ccc; border-radius: 4px; }
    .cart-item-subtotal, .cart-item-price { font-weight: 500; }
    .cart-summary { 
        margin-top: 20px; 
        padding: 20px; 
        background-color: #f9f9f9; 
        border-radius: 8px; 
        display: flex; 
        justify-content: space-between; 
        align-items: center;
        flex-wrap: wrap;
    }
    .cart-total { font-size: 1.5em; font-weight: bold; color: #e8491d; margin-bottom:10px; }
    .cart-actions .btn { margin-left: 10px; margin-bottom:10px;}
    @media (max-width: 768px) {
        .cart-table th:nth-child(3), .cart-table td:nth-child(3), /* Hide price on small screens */
        .cart-table th:nth-child(5), .cart-table td:nth-child(5) { /* Hide subtotal on small screens */
            display: none;
        }
        .cart-summary { flex-direction: column; align-items: flex-end; }
        .cart-total { margin-bottom: 15px; }
    }
</c:set>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
