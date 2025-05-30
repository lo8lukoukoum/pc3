package com.xiaoai.servlet.admin;

import com.xiaoai.dao.OrderDAO;
import com.xiaoai.entity.Order;
import com.xiaoai.entity.User; // For isAdmin check

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList; // For initializing list

@WebServlet("/admin/orders")
public class AdminOrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private OrderDAO orderDAO;
    private static final int ORDERS_PER_PAGE = 15;

    @Override
    public void init() throws ServletException {
        super.init();
        orderDAO = new OrderDAO();
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

        HttpSession session = request.getSession(); // For setting messages
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
        
        try {
            if (action == null || action.isEmpty() || "list".equalsIgnoreCase(action)) {
                List<Order> orderList = new ArrayList<>();
                int totalOrders = 0;

                if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                    orderList = orderDAO.getOrdersByStatus(statusFilter, pageNumber, ORDERS_PER_PAGE);
                    totalOrders = orderDAO.getTotalOrderCountByStatus(statusFilter);
                } else {
                    // Assuming these methods exist or will be added to OrderDAO
                    // orderList = orderDAO.getAllOrders(pageNumber, ORDERS_PER_PAGE);
                    // totalOrders = orderDAO.getTotalOrderCount();
                    // Fallback if getAllOrders is not yet implemented in provided OrderDAO:
                    // For now, if no status filter, this might not show all orders unless OrderDAO.getOrdersByStatus handles null/empty status
                    // Or, we can redirect to a default filter or show an error.
                    // Let's assume getOrdersByStatus can handle a null/empty filter to show all, or we list by a default status.
                    // For a robust solution, OrderDAO needs getAllOrders().
                    // Temporary work-around: if statusFilter is empty, list e.g. "待付款" or don't filter.
                    // The provided OrderDAO has getOrdersByStatus which expects a status.
                    // This servlet will behave according to current DAO.
                    // If no filter, no list is loaded unless OrderDAO is adapted or a default filter applied.
                    // To make it work with current DAO if no status is selected, we could pick a common status
                    // or simply provide no list until a filter is chosen.
                    // For now, if no specific status filter, we won't load any specific list through this path.
                    // Admin would need to select a status or a new DAO method for "all" would be required.
                    // To fulfill the requirement of listing all if no filter, we'd need getAllOrders in OrderDAO.
                    // Since I can't modify OrderDAO, I'll make a note that listing "all" without a filter
                    // depends on OrderDAO.getAllOrders() being implemented.
                    // For now, let's make it so if statusFilter is null/empty, it doesn't try to load.
                    // The JSP should prompt to select a filter.
                    if (statusFilter == null || statusFilter.trim().isEmpty()){
                         request.setAttribute("promptForFilter", true);
                    }
                }

                int totalPages = (int) Math.ceil((double) totalOrders / ORDERS_PER_PAGE);
                if (totalPages == 0 && totalOrders > 0) totalPages = 1;

                request.setAttribute("orderList", orderList);
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("currentPage", pageNumber);
                if (statusFilter != null) request.setAttribute("currentStatusFilter", statusFilter);
                request.getRequestDispatcher("/admin/admin_order_list.jsp").forward(request, response);

            } else if ("detail".equalsIgnoreCase(action)) {
                String orderIdParam = request.getParameter("orderId");
                 if (orderIdParam == null || orderIdParam.isEmpty()) {
                    session.setAttribute("errorMessage", "未指定订单ID。");
                    response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
                    return;
                }
                int orderId = Integer.parseInt(orderIdParam);
                Order orderDetails = orderDAO.getOrderById(orderId); // This should fetch items and user info
                 if (orderDetails == null) {
                    session.setAttribute("errorMessage", "找不到订单详情。");
                    response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
                    return;
                }
                request.setAttribute("orderDetails", orderDetails);
                request.getRequestDispatcher("/admin/admin_order_detail.jsp").forward(request, response);

            } else if ("delete".equalsIgnoreCase(action)) { // GET delete
                String orderIdParam = request.getParameter("orderId");
                 if (orderIdParam == null || orderIdParam.isEmpty()) {
                    session.setAttribute("errorMessage", "未指定要删除的订单ID。");
                } else {
                    int orderId = Integer.parseInt(orderIdParam);
                    if (orderDAO.deleteOrder(orderId)) { // Assumes OrderDAO.deleteOrder exists and handles cascade
                        session.setAttribute("successMessage", "订单 ID: " + orderId + " 已成功删除。");
                    } else {
                        session.setAttribute("errorMessage", "删除订单 ID: " + orderId + " 失败。");
                    }
                }
                response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
            } else {
                session.setAttribute("warningMessage", "无效的管理员订单操作。");
                response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "无效的ID格式。");
            response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "处理管理员订单操作时出错：" + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
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
        String orderIdParam = request.getParameter("orderId");
        int orderId = 0;

        if (orderIdParam != null && !orderIdParam.isEmpty()){
            try {
                orderId = Integer.parseInt(orderIdParam);
            } catch (NumberFormatException e) {
                session.setAttribute("errorMessage", "无效的订单ID格式。");
                response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
                return;
            }
        }


        try {
            if ("update_status".equalsIgnoreCase(action)) {
                if (orderId == 0) {
                     session.setAttribute("errorMessage", "更新状态需要订单ID。");
                     response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
                     return;
                }
                String newStatus = request.getParameter("newStatus");
                if (newStatus == null || newStatus.trim().isEmpty()) {
                    session.setAttribute("errorMessage", "新状态不能为空。");
                } else {
                    if (orderDAO.updateOrderStatus(orderId, newStatus)) {
                        session.setAttribute("successMessage", "订单 ID: " + orderId + " 状态已更新为 " + newStatus + "。");
                    } else {
                        session.setAttribute("errorMessage", "更新订单 ID: " + orderId + " 状态失败。");
                    }
                }
                response.sendRedirect(request.getContextPath() + "/admin/orders?action=detail&orderId=" + orderId);
            
            } else if ("delete".equalsIgnoreCase(action)) { // POST delete
                 if (orderId == 0) {
                     session.setAttribute("errorMessage", "删除需要订单ID。");
                     response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
                     return;
                }
                if (orderDAO.deleteOrder(orderId)) {
                    session.setAttribute("successMessage", "订单 ID: " + orderId + " 已成功删除。");
                } else {
                    session.setAttribute("errorMessage", "删除订单 ID: " + orderId + " 失败。");
                }
                response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
            } else {
                session.setAttribute("warningMessage", "无效的POST操作。");
                response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "处理管理员POST操作时出错：" + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/orders?action=list");
        }
    }
}
