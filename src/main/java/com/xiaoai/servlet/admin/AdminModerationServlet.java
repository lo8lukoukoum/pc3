package com.xiaoai.servlet.admin;

import com.xiaoai.dao.MessageDAO;
import com.xiaoai.dao.ReviewDAO;
import com.xiaoai.entity.Message; // For type casting if needed, though list is of Message
import com.xiaoai.entity.Review;  // For type casting if needed, though list is of Review
import com.xiaoai.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList; // Required for initializing lists

@WebServlet("/admin/moderate")
public class AdminModerationServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ReviewDAO reviewDAO;
    private MessageDAO messageDAO;
    private static final int ITEMS_PER_PAGE = 10;

    @Override
    public void init() throws ServletException {
        super.init();
        reviewDAO = new ReviewDAO();
        messageDAO = new MessageDAO();
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            return false;
        }
        User user = (User) session.getAttribute("loggedInUser");
        return user != null && "admin".equals(user.getRole());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        String pageParam = request.getParameter("page");
        String statusFilter = request.getParameter("status_filter");

        int pageNumber = 1;
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                pageNumber = Integer.parseInt(pageParam);
                if (pageNumber < 1) pageNumber = 1;
            } catch (NumberFormatException e) { pageNumber = 1; }
        }
        
        String currentStatus = (statusFilter == null || statusFilter.trim().isEmpty()) ? "待审核" : statusFilter;

        try {
            if (action == null || action.isEmpty() || "list_reviews".equalsIgnoreCase(action)) {
                List<Review> reviewList = reviewDAO.getReviewsByStatus(currentStatus, pageNumber, ITEMS_PER_PAGE);
                int totalItems = reviewDAO.getTotalReviewCountByStatus(currentStatus);
                int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
                if (totalPages == 0 && totalItems > 0) totalPages = 1;

                request.setAttribute("itemList", reviewList);
                request.setAttribute("itemType", "review"); // For JSP to differentiate
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("currentPage", pageNumber);
                request.setAttribute("currentStatusFilter", currentStatus);
                request.setAttribute("pageAction", "list_reviews"); // For pagination links
                request.getRequestDispatcher("/admin/admin_moderation_list.jsp").forward(request, response);

            } else if ("list_messages".equalsIgnoreCase(action)) {
                List<Message> messageList = messageDAO.getMessagesByStatus(currentStatus, pageNumber, ITEMS_PER_PAGE);
                int totalItems = messageDAO.getTotalMessageCountByStatus(currentStatus);
                int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
                 if (totalPages == 0 && totalItems > 0) totalPages = 1;

                request.setAttribute("itemList", messageList);
                request.setAttribute("itemType", "message"); // For JSP to differentiate
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("currentPage", pageNumber);
                request.setAttribute("currentStatusFilter", currentStatus);
                request.setAttribute("pageAction", "list_messages"); // For pagination links
                request.getRequestDispatcher("/admin/admin_moderation_list.jsp").forward(request, response);
            } else {
                session.setAttribute("warningMessage", "无效的审核操作请求。");
                response.sendRedirect(request.getContextPath() + "/admin/moderate?action=list_reviews");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "加载审核列表时出错: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp"); // Redirect to admin dashboard
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (!isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            return;
        }

        HttpSession session = request.getSession();
        String action = request.getParameter("action");
        String itemIdParam = request.getParameter("itemId");
        String itemType = request.getParameter("itemType"); // "review" or "message" to know which DAO to use

        String redirectUrl = request.getContextPath();
        if ("review".equals(itemType)) {
            redirectUrl += "/admin/moderate?action=list_reviews";
        } else if ("message".equals(itemType)) {
            redirectUrl += "/admin/moderate?action=list_messages";
        } else {
             redirectUrl += "/admin/dashboard.jsp"; // Fallback
        }
        // Append previous page and filter for better UX, if available
        String page = request.getParameter("pageRet");
        String status = request.getParameter("statusRet");
        if (page != null) redirectUrl += "&page=" + page;
        if (status != null) redirectUrl += "&status_filter=" + status;


        if (itemIdParam == null || itemIdParam.isEmpty()) {
            session.setAttribute("errorMessage", "操作失败：未提供项目ID。");
            response.sendRedirect(redirectUrl);
            return;
        }
        int itemId;
        try {
            itemId = Integer.parseInt(itemIdParam);
        } catch (NumberFormatException e) {
            session.setAttribute("errorMessage", "操作失败：项目ID格式无效。");
            response.sendRedirect(redirectUrl);
            return;
        }

        boolean success = false;
        String successMessage = "";
        String errorMessage = "";

        try {
            if ("review".equals(itemType)) {
                if ("approve_review".equalsIgnoreCase(action)) {
                    success = reviewDAO.updateReviewStatus(itemId, "已批准");
                    successMessage = "评价 ID: " + itemId + " 已批准。";
                    errorMessage = "批准评价 ID: " + itemId + " 失败。";
                } else if ("reject_review".equalsIgnoreCase(action)) {
                    success = reviewDAO.updateReviewStatus(itemId, "已拒绝");
                     successMessage = "评价 ID: " + itemId + " 已拒绝。";
                    errorMessage = "拒绝评价 ID: " + itemId + " 失败。";
                } else if ("delete_review".equalsIgnoreCase(action)) {
                    success = reviewDAO.deleteReview(itemId);
                    successMessage = "评价 ID: " + itemId + " 已删除。";
                    errorMessage = "删除评价 ID: " + itemId + " 失败。";
                } else {
                     errorMessage = "对评价的无效操作：" + action;
                }
            } else if ("message".equals(itemType)) {
                if ("approve_message".equalsIgnoreCase(action)) {
                    success = messageDAO.updateMessageStatus(itemId, "已批准");
                    successMessage = "留言 ID: " + itemId + " 已批准。";
                    errorMessage = "批准留言 ID: " + itemId + " 失败。";
                } else if ("reject_message".equalsIgnoreCase(action)) { 
                    // Typically, messages might be deleted or marked as 'Archived' or 'Spam' rather than 'Rejected'
                    // For this example, let's use delete for "reject"
                    success = messageDAO.deleteMessage(itemId);
                    successMessage = "留言 ID: " + itemId + " 已作为拒绝处理（删除）。";
                    errorMessage = "拒绝（删除）留言 ID: " + itemId + " 失败。";
                } else if ("delete_message".equalsIgnoreCase(action)) {
                    success = messageDAO.deleteMessage(itemId);
                     successMessage = "留言 ID: " + itemId + " 已删除。";
                    errorMessage = "删除留言 ID: " + itemId + " 失败。";
                } else {
                    errorMessage = "对留言的无效操作：" + action;
                }
            } else {
                errorMessage = "无效的项目类型。";
            }

            if (success) {
                session.setAttribute("successMessage", successMessage);
            } else {
                session.setAttribute("errorMessage", errorMessage.isEmpty() ? "操作失败。" : errorMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "处理审核操作时发生错误: " + e.getMessage());
        }
        response.sendRedirect(redirectUrl);
    }
}
