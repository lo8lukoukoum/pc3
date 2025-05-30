<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<% request.setAttribute("pageTitle", "提交商品评价"); %>
<jsp:include page="/WEB-INF/jspf/header.jspf" />

<div class="container submit-review-page">
    <h2>为商品提交评价</h2>

    <c:if test="${empty requestScope.productIdToReview}">
        <div class="alert alert-warning">
            无法确定要评价的商品。请从商品详情页或您的订单发起评价。
            <a href="${pageContext.request.contextPath}/products" class="btn btn-secondary">返回商品列表</a>
        </div>
    </c:if>

    <c:if test="${not empty requestScope.productIdToReview}">
        <%-- Optional: Display product information if available from servlet --%>
        <c:if test="${not empty requestScope.productToReview}">
            <div class="product-info-for-review">
                <h4>您正在评价: <c:out value="${productToReview.name}"/></h4>
                <c:if test="${not empty productToReview.imageUrl}">
                    <img src="${pageContext.request.contextPath}/${fn:escapeXml(productToReview.imageUrl)}" alt="${fn:escapeXml(productToReview.name)}" style="max-height: 100px; border-radius: 4px; margin-bottom: 15px;">
                </c:if>
            </div>
        </c:if>

        <form action="${pageContext.request.contextPath}/review" method="POST" class="review-form">
            <input type="hidden" name="action" value="submit_review">
            <input type="hidden" name="productId" value="${fn:escapeXml(requestScope.productIdToReview)}">

            <div class="form-group rating-group">
                <label for="rating">您的评分 (1-5星):</label>
                <div class="star-rating">
                    <c:forEach var="i" begin="5" end="1" step="-1">
                        <input type="radio" id="star${i}" name="rating" value="${i}" 
                               <c:if test="${(not empty param.rating && param.rating == i) || (not empty requestScope.submittedRating && requestScope.submittedRating == i)}">checked</c:if> required>
                        <label for="star${i}" title="${i}星">&#9733;</label>
                    </c:forEach>
                </div>
            </div>

            <div class="form-group">
                <label for="commentText">评价内容:</label>
                <textarea id="commentText" name="commentText" rows="6" class="form-control" required placeholder="请分享您对该商品的使用感受、优点或缺点等。">${fn:escapeXml(not empty param.commentText ? param.commentText : requestScope.submittedComment)}</textarea>
            </div>

            <div class="form-group form-check">
                <input type="checkbox" id="isAnonymous" name="isAnonymous" value="true" class="form-check-input" 
                       <c:if test="${(not empty param.isAnonymous && param.isAnonymous == 'true') || (not empty requestScope.submittedIsAnonymous && requestScope.submittedIsAnonymous)}">checked</c:if>>
                <label for="isAnonymous" class="form-check-label">匿名评价</label>
            </div>

            <div class="form-group optional-fields">
                <p>可选信息 (提供链接):</p>
                <label for="imageUrl">图片链接:</label>
                <input type="text" id="imageUrl" name="imageUrl" class="form-control" 
                       value="${fn:escapeXml(not empty param.imageUrl ? param.imageUrl : requestScope.submittedImageUrl)}" 
                       placeholder="例如: http://example.com/image.jpg">
            </div>

            <div class="form-group">
                <label for="videoUrl">视频链接:</label>
                <input type="text" id="videoUrl" name="videoUrl" class="form-control" 
                       value="${fn:escapeXml(not empty param.videoUrl ? param.videoUrl : requestScope.submittedVideoUrl)}" 
                       placeholder="例如: http://example.com/video.mp4">
            </div>

            <div class="form-group form-submit-actions">
                <button type="submit" class="btn btn-primary">提交评价</button>
                <a href="${pageContext.request.contextPath}/product_detail.jsp?productId=${requestScope.productIdToReview}" class="btn btn-secondary">取消</a>
            </div>
        </form>
    </c:if>
</div>

<c:set var="pageSpecificStyles" scope="request">
    .submit-review-page h2 { text-align: center; margin-bottom: 25px; }
    .review-form { max-width: 700px; margin: 20px auto; padding: 25px; background-color: #fff; border: 1px solid #ddd; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.05); }
    .product-info-for-review { margin-bottom:20px; padding-bottom:15px; border-bottom:1px solid #eee; text-align:center; }
    .product-info-for-review h4 { margin-bottom:10px; }

    .rating-group .star-rating {
        display: inline-block;
        direction: rtl; /* Right to left to select stars easily by hovering from right */
    }
    .rating-group .star-rating input[type="radio"] {
        display: none; /* Hide actual radio buttons */
    }
    .rating-group .star-rating label {
        font-size: 2.5em; /* Size of stars */
        color: #ddd; /* Default star color (empty) */
        cursor: pointer;
        padding: 0 0.05em;
        transition: color 0.2s;
    }
    /* CSS for star selection: when a radio button is checked, all previous (in RTL) labels change color */
    .rating-group .star-rating input[type="radio"]:checked ~ label,
    .rating-group .star-rating label:hover,
    .rating-group .star-rating label:hover ~ label { /* Hover effect */
        color: #f5c518; /* Gold color for selected/hovered stars */
    }
    /* Reset hover effect if a star is already checked and user hovers over a lesser star */
    .rating-group .star-rating input[type="radio"]:checked + label:hover ~ label {
        color: #ddd; /* Keep subsequent stars grey */
    }
    .rating-group .star-rating input[type="radio"]:checked + label:hover {
         color: #f5c518; /* Ensure the hovered one over a checked one remains gold */
    }


    .form-check-label { margin-left: 5px; }
    .optional-fields { margin-top: 20px; padding-top: 15px; border-top: 1px solid #eee;}
    .optional-fields p { font-weight: bold; margin-bottom: 10px; }
    .form-submit-actions { margin-top: 20px; text-align: right; }
    .form-submit-actions .btn { margin-left: 10px; }
</c:set>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
