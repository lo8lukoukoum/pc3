<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> <%-- For formatting numbers, dates --%>

<c:set var="pageTitle" scope="request">
    <c:choose>
        <c:when test="${not empty param.searchTerm}">搜索结果: <c:out value="${param.searchTerm}"/></c:when>
        <c:when test="${not empty param.category}">分类: <c:out value="${param.category}"/></c:when>
        <c:when test="${not empty param.status}">状态: <c:out value="${param.status}"/></c:when>
        <c:otherwise>商品列表</c:otherwise>
    </c:choose>
</c:set>
<jsp:include page="/WEB-INF/jspf/header.jspf" />

<div class="container product-listing-page">
    <%-- Optional: Search and Filter Bar --%>
    <div class="filter-bar">
        <form action="${pageContext.request.contextPath}/products" method="GET" class="form-inline">
            <div class="form-group">
                <label for="searchTerm">搜索:</label>
                <input type="text" id="searchTerm" name="searchTerm" value="<c:out value='${searchTerm}'/>" placeholder="商品名称或描述">
            </div>
            <div class="form-group">
                <label for="category">分类:</label>
                <input type="text" id="category" name="category" value="<c:out value='${selectedCategory}'/>" placeholder="例如: 笔记本电脑">
                <%-- Better as a dropdown populated from DB categories in a real app --%>
            </div>
            <div class="form-group">
                <label for="status">状态:</label>
                <select id="status" name="status">
                    <option value="">所有状态</option>
                    <option value="上架" ${selectedStatus == '上架' ? 'selected' : ''}>上架</option>
                    <option value="新品" ${selectedStatus == '新品' ? 'selected' : ''}>新品</option>
                    <option value="热销" ${selectedStatus == '热销' ? 'selected' : ''}>热销</option>
                    <option value="下架" ${selectedStatus == '下架' ? 'selected' : ''}>下架</option>
                </select>
            </div>
            <button type="submit" class="btn">筛选</button>
        </form>
    </div>

    <h2>${pageTitle}</h2>

    <c:if test="${empty products}">
        <p class="empty-list-message">未找到符合条件的商品。</p>
    </c:if>

    <div class="product-grid">
        <c:forEach var="product" items="${products}">
            <div class="product-card">
                <a href="${pageContext.request.contextPath}/product_detail.jsp?productId=${product.productId}" class="product-link">
                    <c:choose>
                        <c:when test="${not empty product.imageUrl}">
                            <img src="${pageContext.request.contextPath}/${fn:escapeXml(product.imageUrl)}" alt="${fn:escapeXml(product.name)}">
                        </c:when>
                        <c:otherwise>
                            <img src="${pageContext.request.contextPath}/images/placeholder.png" alt="无图片"> <%-- Default placeholder --%>
                        </c:otherwise>
                    </c:choose>
                    <h3><c:out value="${product.name}"/></h3>
                </a>
                <p class="price"><fmt:formatNumber value="${product.price}" type="currency" currencySymbol="¥"/> </p>
                <p class="stock">库存: <c:out value="${product.stockQuantity > 0 ? product.stockQuantity : '无货'}"/></p>
                <p class="category-status">
                    <span class="badge category-badge"><c:out value="${product.category}"/></span>
                    <span class="badge status-badge status-${fn:toLowerCase(product.status)}"><c:out value="${product.status}"/></span>
                </p>

                <form action="${pageContext.request.contextPath}/cart" method="POST">
                    <input type="hidden" name="action" value="add">
                    <input type="hidden" name="productId" value="${product.productId}">
                    <div class="form-group-inline">
                         <label for="quantity-${product.productId}">数量:</label>
                         <input type="number" id="quantity-${product.productId}" name="quantity" value="1" min="1" max="${product.stockQuantity > 0 ? product.stockQuantity : 1}" style="width: 60px;" <c:if test="${product.stockQuantity <= 0}">disabled</c:if>>
                    </div>
                    <button type="submit" class="btn btn-add-to-cart" <c:if test="${product.stockQuantity <= 0}">disabled</c:if>>
                        <c:choose><c:when test="${product.stockQuantity <= 0}">缺货</c:when><c:otherwise>加入购物车</c:otherwise></c:choose>
                    </button>
                </form>
            </div>
        </c:forEach>
    </div>

    <%-- Pagination Controls --%>
    <c:if test="${totalPages > 1}">
        <nav class="pagination">
            <%-- Previous Page --%>
            <c:choose>
                <c:when test="${currentPage > 1}">
                    <a href="${pageContext.request.contextPath}/products?page=${currentPage - 1}&searchTerm=${fn:escapeXml(searchTerm)}&category=${fn:escapeXml(selectedCategory)}&status=${fn:escapeXml(selectedStatus)}">&laquo; 上一页</a>
                </c:when>
                <c:otherwise>
                    <span class="disabled">&laquo; 上一页</span>
                </c:otherwise>
            </c:choose>

            <%-- Page Numbers --%>
            <c:forEach var="i" begin="1" end="${totalPages}">
                <c:choose>
                    <c:when test="${i == currentPage}">
                        <span class="current">${i}</span>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/products?page=${i}&searchTerm=${fn:escapeXml(searchTerm)}&category=${fn:escapeXml(selectedCategory)}&status=${fn:escapeXml(selectedStatus)}">${i}</a>
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <%-- Next Page --%>
            <c:choose>
                <c:when test="${currentPage < totalPages}">
                    <a href="${pageContext.request.contextPath}/products?page=${currentPage + 1}&searchTerm=${fn:escapeXml(searchTerm)}&category=${fn:escapeXml(selectedCategory)}&status=${fn:escapeXml(selectedStatus)}">下一页 &raquo;</a>
                </c:when>
                <c:otherwise>
                    <span class="disabled">下一页 &raquo;</span>
                </c:otherwise>
            </c:choose>
        </nav>
    </c:if>
</div>

<c:set var="pageSpecificStyles" scope="request">
    .filter-bar { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
    .filter-bar .form-inline .form-group { margin-right: 15px; margin-bottom: 10px; display: inline-block; }
    .filter-bar .form-inline label { margin-right: 5px; }
    .filter-bar .form-inline input[type="text"], .filter-bar .form-inline select { padding: 8px; border: 1px solid #ccc; border-radius: 4px; }
    .product-listing-page h2 { text-align: center; margin-bottom: 25px; color: #333; }
    .empty-list-message { text-align: center; font-size: 1.2em; color: #777; margin-top: 30px; }
    .product-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 25px; }
    .product-card {
        border: 1px solid #e0e0e0;
        border-radius: 8px;
        padding: 20px;
        text-align: center;
        background-color: #fff;
        box-shadow: 0 4px 8px rgba(0,0,0,0.08);
        transition: box-shadow 0.3s ease;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
    }
    .product-card:hover { box-shadow: 0 6px 12px rgba(0,0,0,0.12); }
    .product-card img {
        max-width: 100%;
        height: 200px; /* Fixed height for uniform look */
        object-fit: contain; /* Was 'cover', 'contain' might be better for product images */
        margin-bottom: 15px;
        border-radius: 4px;
        background-color: #f8f8f8; /* Light background for images */
    }
    .product-card h3 { font-size: 1.25rem; margin-bottom: 8px; color: #35424a; min-height: 2.5em; /* Ensure space for two lines */ }
    .product-card .product-link { text-decoration: none; color: inherit; }
    .product-card .price { font-size: 1.3em; font-weight: bold; color: #e8491d; margin-bottom: 8px; }
    .product-card .stock { font-size: 0.9em; color: #555; margin-bottom: 12px; }
    .product-card .category-status { margin-bottom: 15px; }
    .product-card .badge { display: inline-block; padding: .25em .6em; font-size: .75em; font-weight: 700; line-height: 1; text-align: center; white-space: nowrap; vertical-align: baseline; border-radius: .25rem; margin-right: 5px; }
    .product-card .category-badge { background-color: #5bc0de; color: white; }
    .product-card .status-badge.status-上架 { background-color: #5cb85c; color: white; }
    .product-card .status-badge.status-新品 { background-color: #f0ad4e; color: white; }
    .product-card .status-badge.status-热销 { background-color: #d9534f; color: white; }
    .product-card .status-badge.status-下架 { background-color: #777; color: white; }
    .product-card .form-group-inline { display: inline-block; margin-right: 10px; }
    .product-card .form-group-inline label { font-size: 0.9em; }
    .btn-add-to-cart { width: auto; padding: 8px 15px; font-size: 0.9em; }
</c:set>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
