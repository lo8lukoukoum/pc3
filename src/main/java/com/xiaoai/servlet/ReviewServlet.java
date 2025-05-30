package com.xiaoai.servlet;

import com.xiaoai.dao.OrderDAO; // Used to check if user purchased the product
import com.xiaoai.dao.ReviewDAO;
import com.xiaoai.entity.Review;
import com.xiaoai.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

@WebServlet("/review")
public class ReviewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReviewDAO reviewDAO;
    private OrderDAO orderDAO; // To verify if user purchased the product for review eligibility

    private static final int REVIEWS_PER_PAGE = 5;

    @Override
    public void init() throws ServletException {
        super.init();
        reviewDAO = new ReviewDAO();
        orderDAO = new OrderDAO(); // Initialize OrderDAO
    }

    private boolean ensureUserLoggedIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            String requestURI = request.getRequestURI();
            if (request.getQueryString() != null) {
                requestURI += "?" + request.getQueryString();
            }
            HttpSession tempSession = request.getSession(true);
            tempSession.setAttribute("redirectAfterLogin", requestURI);
            tempSession.setAttribute("warningMessage", "请先登录再进行操作！");
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        if (!ensureUserLoggedIn(request, response)) {
            return;
        }

        HttpSession session = request.getSession();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String action = request.getParameter("action");

        try {
            if ("show_form".equalsIgnoreCase(action)) {
                String productIdParam = request.getParameter("productId");
                if (productIdParam == null || productIdParam.isEmpty()) {
                    session.setAttribute("errorMessage", "未指定产品ID。");
                    response.sendRedirect(request.getContextPath() + "/products"); // Redirect to product list or index
                    return;
                }
                int productId = Integer.parseInt(productIdParam);

                // Check if user can review this product (purchased and order completed)
                boolean canReview = reviewDAO.checkIfUserCanReviewProduct(loggedInUser.getUserId(), productId);
                // Alternative check using OrderDAO if checkIfUserCanReviewProduct is not in ReviewDAO or needs different logic
                // boolean canReview = orderDAO.hasUserPurchasedProduct(loggedInUser.getUserId(), productId); // Assuming such a method exists in OrderDAO

                if (canReview) {
                    // Check if user has already submitted a review for this product
                    // This logic could be added to prevent multiple reviews from the same user for the same product
                    // List<Review> existingReviews = reviewDAO.getReviewsByUserIdAndProductId(loggedInUser.getUserId(), productId);
                    // if (!existingReviews.isEmpty()) {
                    //     session.setAttribute("warningMessage", "您已经评价过此商品了。");
                    //     response.sendRedirect(request.getContextPath() + "/products?action=detail&id=" + productId);
                    //     return;
                    // }

                    request.setAttribute("productIdToReview", productId);
                    request.getRequestDispatcher("submit_review.jsp").forward(request, response);
                } else {
                    session.setAttribute("errorMessage", "您需要购买并完成此商品的订单后才能评价！");
                    response.sendRedirect(request.getContextPath() + "/products?action=detail&id=" + productId); // Redirect to product detail page
                }

            } else if ("list_product_reviews".equalsIgnoreCase(action)) {
                String productIdParam = request.getParameter("productId");
                 if (productIdParam == null || productIdParam.isEmpty()) {
                    session.setAttribute("errorMessage", "未指定产品ID以显示评价。");
                    // Decide where to redirect - maybe an error display on current page or back
                    response.sendRedirect(request.getContextPath() + "/products"); 
                    return;
                }
                int productId = Integer.parseInt(productIdParam);
                String pageParam = request.getParameter("page");
                int pageNumber = 1;
                if (pageParam != null && !pageParam.isEmpty()) {
                    try {
                        pageNumber = Integer.parseInt(pageParam);
                        if (pageNumber < 1) pageNumber = 1;
                    } catch (NumberFormatException e) { pageNumber = 1; }
                }

                List<Review> reviews = reviewDAO.getReviewsByProductId(productId, pageNumber, REVIEWS_PER_PAGE);
                int totalReviews = reviewDAO.getTotalReviewCountByProductId(productId);
                int totalPages = (int) Math.ceil((double) totalReviews / REVIEWS_PER_PAGE);
                if (totalPages == 0 && totalReviews > 0) totalPages = 1;

                request.setAttribute("productReviews", reviews);
                request.setAttribute("reviewTotalPages", totalPages);
                request.setAttribute("reviewCurrentPage", pageNumber);
                request.setAttribute("reviewedProductId", productId); // For context in JSP
                // This typically forwards to a fragment included in product_detail.jsp or handled by AJAX
                request.getRequestDispatcher("product_reviews_fragment.jsp").forward(request, response);
            } else {
                session.setAttribute("warningMessage", "无效的评价操作。");
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "无效的参数格式。");
            response.sendRedirect(request.getContextPath() + "/products");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "处理评价操作时发生错误。");
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!ensureUserLoggedIn(request, response)) {
            return;
        }
        HttpSession session = request.getSession();
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        String action = request.getParameter("action");

        if (!"submit_review".equalsIgnoreCase(action)) {
            session.setAttribute("errorMessage", "无效的表单提交。");
            response.sendRedirect(request.getContextPath() + "/index.jsp");
            return;
        }

        String productIdParam = request.getParameter("productId");
        String ratingParam = request.getParameter("rating");
        String commentText = request.getParameter("commentText");
        boolean isAnonymous = "on".equalsIgnoreCase(request.getParameter("isAnonymous")); // Checkbox value
        String imageUrl = request.getParameter("imageUrl"); // Optional
        String videoUrl = request.getParameter("videoUrl"); // Optional

        // Validate required fields
        if (productIdParam == null || productIdParam.isEmpty() ||
            ratingParam == null || ratingParam.isEmpty() ||
            commentText == null || commentText.trim().isEmpty()) {
            session.setAttribute("errorMessage", "产品ID、评分和评论内容不能为空！");
            preserveReviewFormData(request, productIdParam, ratingParam, commentText, isAnonymous, imageUrl, videoUrl);
            request.getRequestDispatcher("submit_review.jsp").forward(request, response);
            return;
        }

        int productId;
        int rating;
        try {
            productId = Integer.parseInt(productIdParam);
            rating = Integer.parseInt(ratingParam);
            if (rating < 1 || rating > 5) {
                 session.setAttribute("errorMessage", "评分必须在1到5之间！");
                 preserveReviewFormData(request, productIdParam, ratingParam, commentText, isAnonymous, imageUrl, videoUrl);
                 request.getRequestDispatcher("submit_review.jsp").forward(request, response);
                 return;
            }
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "产品ID或评分格式无效！");
            preserveReviewFormData(request, productIdParam, ratingParam, commentText, isAnonymous, imageUrl, videoUrl);
            request.getRequestDispatcher("submit_review.jsp").forward(request, response);
            return;
        }

        try {
            // Verify again if user can review this product
            boolean canReview = reviewDAO.checkIfUserCanReviewProduct(loggedInUser.getUserId(), productId);
            if (!canReview) {
                session.setAttribute("errorMessage", "您无权评价此商品或订单尚未完成。");
                response.sendRedirect(request.getContextPath() + "/products?action=detail&id=" + productId);
                return;
            }
            
            // Optional: Check if already reviewed
            // List<Review> existingReviews = reviewDAO.getReviewsByUserIdAndProductId(loggedInUser.getUserId(), productId);
            // if (!existingReviews.isEmpty()) {
            //     session.setAttribute("warningMessage", "您已经评价过此商品了。");
            //     response.sendRedirect(request.getContextPath() + "/products?action=detail&id=" + productId);
            //     return;
            // }


            Review review = new Review();
            review.setProductId(productId);
            review.setUserId(loggedInUser.getUserId());
            review.setRating(rating);
            review.setCommentText(commentText);
            review.setAnonymous(isAnonymous);
            review.setImageUrl(imageUrl);
            review.setVideoUrl(videoUrl);
            review.setStatus("待审核"); // Initial status
            review.setReviewDate(new Timestamp(System.currentTimeMillis()));

            if (reviewDAO.addReview(review)) {
                session.setAttribute("successMessage", "评价提交成功！感谢您的评价，请等待审核。");
                response.sendRedirect(request.getContextPath() + "/products?action=detail&id=" + productId);
            } else {
                session.setAttribute("errorMessage", "评价提交失败，请稍后再试。");
                preserveReviewFormData(request, productIdParam, ratingParam, commentText, isAnonymous, imageUrl, videoUrl);
                request.getRequestDispatcher("submit_review.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "提交评价过程中发生错误。");
            preserveReviewFormData(request, productIdParam, ratingParam, commentText, isAnonymous, imageUrl, videoUrl);
            request.getRequestDispatcher("submit_review.jsp").forward(request, response);
        }
    }
    
    private void preserveReviewFormData(HttpServletRequest request, String productId, String rating, 
                                        String commentText, boolean isAnonymous, String imageUrl, String videoUrl) {
        if (productId != null && !productId.isEmpty()) {
            try {
                request.setAttribute("productIdToReview", Integer.parseInt(productId));
            } catch (NumberFormatException e) { /* ignore */ }
        }
        request.setAttribute("submittedRating", rating);
        request.setAttribute("submittedComment", commentText);
        request.setAttribute("submittedIsAnonymous", isAnonymous);
        request.setAttribute("submittedImageUrl", imageUrl);
        request.setAttribute("submittedVideoUrl", videoUrl);
    }
}
