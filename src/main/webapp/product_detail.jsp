<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="product" value="${requestScope.product}" />
<c:set var="pageTitle" scope="request" value="${not empty product ? fn:escapeXml(product.name) : '商品详情'}" />

<jsp:include page="/WEB-INF/jspf/header.jspf" />

<div class="container product-detail-page">
    <c:choose>
        <c:when test="${not empty product}">
            <div class="product-detail-grid">
                <div class="product-image-section">
                    <c:choose>
                        <c:when test="${not empty product.imageUrl}">
                            <img src="${pageContext.request.contextPath}/${fn:escapeXml(product.imageUrl)}" alt="${fn:escapeXml(product.name)}" class="main-product-image">
                        </c:when>
                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}/images/placeholder.png" alt="无图片" class="main-product-image placeholder-image">
                        </c:otherwise>
                    </c:choose>
                    <%-- Optional: Thumbnail images if multiple product images are supported --%>
                </div>

                <div class="product-info-section">
                    <h2><c:out value="${product.name}"/></h2>
                    <p class="product-price"><fmt:formatNumber value="${product.price}" type="currency" currencySymbol="¥"/></p>
                    
                    <div class="product-meta">
                        <p><strong>分类:</strong> <span class="badge category-badge"><c:out value="${product.category}"/></span></p>
                        <p><strong>状态:</strong> <span class="badge status-badge status-${fn:toLowerCase(product.status)}"><c:out value="${product.status}"/></span></p>
                        <p><strong>库存:</strong> 
                            <c:choose>
                                <c:when test="${product.stockQuantity > 10}">${product.stockQuantity} 件 (库存充足)</c:when>
                                <c:when test="${product.stockQuantity > 0}">${product.stockQuantity} 件 (库存紧张)</c:when>
                                <c:otherwise><span class="text-danger">无货</span></c:otherwise>
                            </c:choose>
                        </p>
                        <p><strong>浏览量:</strong> <c:out value="${product.views}"/> 次</p>
                    </div>

                    <div class="product-description">
                        <h4>商品描述:</h4>
                        <p><c:out value="${product.description}" escapeXml="false"/></p> <%-- Allow basic HTML if stored, otherwise true --%>
                    </div>

                    <form action="${pageContext.request.contextPath}/cart" method="POST" class="add-to-cart-form">
                        <input type="hidden" name="action" value="add">
                        <input type="hidden" name="productId" value="${product.productId}">
                        <div class="form-group">
                            <label for="quantity">数量:</label>
                            <input type="number" id="quantity" name="quantity" value="1" min="1" max="${product.stockQuantity > 0 ? product.stockQuantity : 1}" class="form-control quantity-input" <c:if test="${product.stockQuantity <= 0}">disabled</c:if>>
                        </div>
                        <button type="submit" class="btn btn-lg btn-add-to-cart-detail" <c:if test="${product.stockQuantity <= 0}">disabled</c:if>>
                             <c:choose><c:when test="${product.stockQuantity <= 0}">暂时缺货</c:when><c:otherwise>加入购物车</c:otherwise></c:choose>
                        </button>
                    </form>
                </div>
            </div>

            <hr class="section-divider">

            <%-- Product Reviews Section --%>
            <div class="product-reviews-section">
                <h3>商品评价</h3>
                <c:if test="${sessionScope.loggedInUser != null}">
                    <%-- Check if user is eligible to review (e.g., has purchased) --%>
                    <%-- This logic is now in ReviewServlet doGet action=show_form --%>
                    <a href="${pageContext.request.contextPath}/review?action=show_form&productId=${product.productId}" class="btn btn-secondary">撰写评价</a>
                </c:if>
                <c:if test="${sessionScope.loggedInUser == null}">
                     <p><a href="${pageContext.request.contextPath}/login.jsp?redirect=${fn:escapeXml(pageContext.request.requestURI)}%3FproductId%3D${product.productId}">登录</a>后可发表评价。</p>
                </c:if>
                
                <%-- Placeholder for displaying reviews --%>
                <%-- This part would typically be loaded by ReviewServlet or via AJAX --%>
                <div id="reviews-list-container" style="margin-top: 20px;">
                    <p><em>评价加载中... (或: 暂无评价)</em></p>
                    <%-- Example: <jsp:include page="/review?action=list_product_reviews&productId=${product.productId}" /> --%>
                    <%-- Or use AJAX to load reviews from /review?action=list_product_reviews&productId=${product.productId} --%>
                </div>
            </div>

        </c:when>
        <c:otherwise>
            <p class="alert alert-warning">无法加载商品详情。该商品可能已被下架或不存在。</p>
        </c:otherwise>
    </c:choose>
</div>

<c:set var="pageSpecificStyles" scope="request">
    .product-detail-page { padding-top: 20px; }
    .product-detail-grid { display: grid; grid-template-columns: 1fr 1.5fr; gap: 30px; margin-bottom: 30px; }
    @media (max-width: 768px) { .product-detail-grid { grid-template-columns: 1fr; } }
    .product-image-section .main-product-image { 
        width: 100%; 
        max-height: 450px; 
        object-fit: contain; 
        border: 1px solid #eee; 
        border-radius: 8px; 
        background-color: #f9f9f9;
    }
    .product-image-section .placeholder-image { padding: 20px; } /* Better visibility for placeholder */

    .product-info-section h2 { font-size: 2em; margin-bottom: 15px; color: #333; }
    .product-info-section .product-price { font-size: 1.8em; font-weight: bold; color: #e8491d; margin-bottom: 20px; }
    .product-meta p { margin-bottom: 8px; font-size: 0.95em; color: #555; }
    .product-meta strong { color: #333; }
    .product-meta .badge { font-size: 0.85em; padding: .3em .7em; margin-left: 5px; }
    .product-meta .text-danger { color: #d9534f; font-weight: bold; }
    .product-description { margin-top: 25px; margin-bottom: 25px; }
    .product-description h4 { margin-bottom: 10px; font-size: 1.2em; }
    .add-to-cart-form .form-group { margin-bottom: 15px; display: flex; align-items: center; }
    .add-to-cart-form .quantity-input { width: 80px; margin-right: 15px; text-align: center; }
    .btn-add-to-cart-detail { font-size: 1.1em; padding: 12px 25px; }
    .section-divider { margin-top: 30px; margin-bottom: 30px; border: 0; border-top: 1px solid #eee; }
    .product-reviews-section h3 { font-size: 1.5em; margin-bottom: 15px; }
</c:set>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
