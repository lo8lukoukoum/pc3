<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="isEditMode" value="${not empty productToEdit && not empty productToEdit.productId}" />
<c:set var="formTitle" value="${isEditMode ? '编辑商品' : '添加新商品'}" />
<% request.setAttribute("pageTitle", (String)pageContext.getAttribute("formTitle")); %>

<jsp:include page="/WEB-INF/jspf/admin_header.jspf" />

<div class="admin-page-container">
    <div class="card admin-form-container">
        <div class="card-header">
            ${formTitle}
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/admin/products" method="POST" enctype="multipart/form-data">
                <c:choose>
                    <c:when test="${isEditMode}">
                        <input type="hidden" name="action" value="edit_product">
                        <input type="hidden" name="productId" value="${productToEdit.productId}">
                    </c:when>
                    <c:otherwise>
                        <input type="hidden" name="action" value="add_product">
                    </c:otherwise>
                </c:choose>

                <div class="form-group">
                    <label for="name">商品名称 <span class="required-field">*</span></label>
                    <input type="text" id="name" name="name" class="form-control" value="${fn:escapeXml(productToEdit.name)}" required>
                </div>

                <div class="form-group">
                    <label for="description">商品描述 <span class="required-field">*</span></label>
                    <textarea id="description" name="description" rows="6" class="form-control" required>${fn:escapeXml(productToEdit.description)}</textarea>
                </div>
                
                <div class="form-row">
                    <div class="form-group col-md-6">
                        <label for="price">价格 (¥) <span class="required-field">*</span></label>
                        <input type="number" id="price" name="price" class="form-control" value="${productToEdit.price}" step="0.01" min="0.01" required>
                    </div>
                    <div class="form-group col-md-6">
                        <label for="stockQuantity">库存数量 <span class="required-field">*</span></label>
                        <input type="number" id="stockQuantity" name="stockQuantity" class="form-control" value="${productToEdit.stockQuantity}" min="0" required>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group col-md-6">
                        <label for="category">分类</label>
                        <input type="text" id="category" name="category" class="form-control" value="${fn:escapeXml(productToEdit.category)}" placeholder="例如: 笔记本电脑">
                    </div>
                    <div class="form-group col-md-6">
                        <label for="status">商品状态 <span class="required-field">*</span></label>
                        <select id="status" name="status" class="form-control" required>
                            <option value="上架" ${productToEdit.status == '上架' ? 'selected' : ''}>上架</option>
                            <option value="下架" ${productToEdit.status == '下架' ? 'selected' : ''}>下架</option>
                            <option value="新品" ${productToEdit.status == '新品' ? 'selected' : ''}>新品</option>
                            <option value="热销" ${productToEdit.status == '热销' ? 'selected' : ''}>热销</option>
                        </select>
                    </div>
                </div>
                
                <div class="form-group">
                    <label for="imageFile">商品图片</label>
                    <c:if test="${isEditMode && not empty productToEdit.imageUrl}">
                        <div class="current-image-preview">
                            <p>当前图片:</p>
                            <img src="${pageContext.request.contextPath}/${fn:escapeXml(productToEdit.imageUrl)}" alt="当前图片" style="max-width: 150px; max-height: 150px; margin-bottom: 10px; border: 1px solid #ddd; padding: 5px;">
                            <input type="hidden" name="existingImageUrl" value="${fn:escapeXml(productToEdit.imageUrl)}">
                            <p class="text-muted small">${isEditMode ? '如需更换图片，请选择下面的文件。不选择则保留原图。' : ''}</p>
                        </div>
                    </c:if>
                    <input type="file" id="imageFile" name="imageFile" class="form-control-file" accept="image/jpeg, image/png, image/gif">
                </div>

                <div class="form-actions">
                    <button type="submit" class="btn btn-primary">${isEditMode ? '更新商品' : '保存商品'}</button>
                    <a href="${pageContext.request.contextPath}/admin/products?action=list" class="btn btn-secondary">取消并返回列表</a>
                </div>
            </form>
        </div>
    </div>
</div>

<c:set var="pageSpecificAdminStyles" scope="request">
    .admin-form-container .card-header { font-size: 1.2rem; }
    .required-field { color: red; margin-left: 2px;}
    .form-row { display: flex; flex-wrap: wrap; margin-right: -5px; margin-left: -5px; }
    .form-row > .col-md-6 { position: relative; width: 100%; padding-right: 5px; padding-left: 5px; flex: 0 0 50%; max-width: 50%; }
    @media (max-width: 768px) { .form-row > .col-md-6 { flex: 0 0 100%; max-width: 100%; } }
    .form-control-file { display: block; width: 100%; }
    .current-image-preview img { display: block; border-radius: 4px; }
    .form-actions { margin-top: 25px; padding-top: 15px; border-top: 1px solid #eee; text-align: right;}
    .form-actions .btn { margin-left: 10px; }
</c:set>

<jsp:include page="/WEB-INF/jspf/admin_footer.jspf" />
