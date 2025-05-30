<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<% request.setAttribute("pageTitle", "商品管理"); %>
<jsp:include page="/WEB-INF/jspf/admin_header.jspf" />

<div class="admin-page-container">
    <div class="admin-page-toolbar">
        <a href="${pageContext.request.contextPath}/admin/products?action=add_form" class="btn btn-success"><i class="fas fa-plus"></i> 添加新商品</a>
    </div>

    <div class="card">
        <div class="card-header">
            商品列表
        </div>
        <div class="card-body">
            <c:choose>
                <c:when test="${not empty productList}">
                    <div class="table-responsive">
                        <table class="admin-table product-admin-table">
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>图片</th>
                                    <th>名称</th>
                                    <th>价格</th>
                                    <th>库存</th>
                                    <th>分类</th>
                                    <th>状态</th>
                                    <th>浏览量</th>
                                    <th>创建日期</th>
                                    <th>操作</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="product" items="${productList}">
                                    <tr>
                                        <td><c:out value="${product.productId}"/></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty product.imageUrl}">
                                                    <img src="${pageContext.request.contextPath}/${fn:escapeXml(product.imageUrl)}" alt="${fn:escapeXml(product.name)}" class="table-product-image">
                                                </c:when>
                                                <c:otherwise>
                                                    <img src="${pageContext.request.contextPath}/images/placeholder.png" alt="无图片" class="table-product-image placeholder">
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td><c:out value="${product.name}"/></td>
                                        <td><fmt:formatNumber value="${product.price}" type="currency" currencySymbol="¥"/></td>
                                        <td><c:out value="${product.stockQuantity}"/></td>
                                        <td><c:out value="${product.category}"/></td>
                                        <td>
                                            <span class="status-badge status-${fn:toLowerCase(fn:replace(product.status, ' ', '_'))}">
                                                <c:out value="${product.status}"/>
                                            </span>
                                        </td>
                                        <td><c:out value="${product.views}"/></td>
                                        <td><fmt:formatDate value="${product.creationDate}" pattern="yyyy-MM-dd"/></td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/products?action=edit_form&productId=${product.productId}" class="btn btn-primary btn-sm"><i class="fas fa-edit"></i> 编辑</a>
                                            <form action="${pageContext.request.contextPath}/admin/products" method="POST" style="display:inline;" onsubmit="return confirmAction('确定要删除商品 #${product.productId} (${fn:escapeXml(product.name)}) 吗？');">
                                                <input type="hidden" name="action" value="delete_product_post">
                                                <input type="hidden" name="productId" value="${product.productId}">
                                                <input type="hidden" name="pageRet" value="${currentPage}">
                                                <button type="submit" class="btn btn-danger btn-sm"><i class="fas fa-trash"></i> 删除</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <p class="text-muted">当前没有商品。</p>
                </c:otherwise>
            </c:choose>
        </div>
    </div>

    <%-- Pagination Controls --%>
    <c:if test="${totalPages > 1}">
        <nav class="pagination admin-pagination">
            <c:choose>
                <c:when test="${currentPage > 1}">
                    <a href="${pageContext.request.contextPath}/admin/products?action=list&page=${currentPage - 1}">&laquo; 上一页</a>
                </c:when>
                <c:otherwise><span class="disabled">&laquo; 上一页</span></c:otherwise>
            </c:choose>
            <c:forEach var="i" begin="1" end="${totalPages}">
                <c:choose>
                    <c:when test="${i == currentPage}"><span class="current">${i}</span></c:when>
                    <c:otherwise><a href="${pageContext.request.contextPath}/admin/products?action=list&page=${i}">${i}</a></c:otherwise>
                </c:choose>
            </c:forEach>
            <c:choose>
                <c:when test="${currentPage < totalPages}">
                    <a href="${pageContext.request.contextPath}/admin/products?action=list&page=${currentPage + 1}">下一页 &raquo;</a>
                </c:when>
                <c:otherwise><span class="disabled">下一页 &raquo;</span></c:otherwise>
            </c:choose>
        </nav>
    </c:if>
</div>

<c:set var="pageSpecificAdminStyles" scope="request">
    .admin-page-toolbar { margin-bottom: 20px; text-align: right; }
    .product-admin-table .table-product-image { max-width: 60px; height: auto; border-radius: 3px; }
    .product-admin-table .table-product-image.placeholder { background-color: #f0f0f0; padding: 5px; }
    .product-admin-table td, .product-admin-table th { vertical-align: middle; }
    .status-badge { display: inline-block; padding: .3em .7em; font-size: .85em; font-weight: 700; line-height: 1; text-align: center; white-space: nowrap; vertical-align: baseline; border-radius: .25rem; }
    .status-上架 { background-color: #5cb85c; color: white; }
    .status-新品 { background-color: #f0ad4e; color: white; }
    .status-热销 { background-color: #d9534f; color: white; }
    .status-下架 { background-color: #777; color: white; }
    .fas { margin-right: 3px; } /* FontAwesome icon spacing */
</c:set>

<jsp:include page="/WEB-INF/jspf/admin_footer.jspf" />
