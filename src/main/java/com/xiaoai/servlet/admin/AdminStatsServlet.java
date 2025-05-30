package com.xiaoai.servlet.admin;

import com.xiaoai.dao.StatsDAO;
import com.xiaoai.dao.OrderDAO; // For CSV export of orders
import com.xiaoai.entity.User;
import com.xiaoai.entity.Order; // For CSV export

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.ArrayList; // Required if initializing lists

@WebServlet("/admin/stats")
public class AdminStatsServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private StatsDAO statsDAO;
    private OrderDAO orderDAO; // For CSV export

    @Override
    public void init() throws ServletException {
        super.init();
        statsDAO = new StatsDAO();
        orderDAO = new OrderDAO(); // Initialize OrderDAO
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
        // Note: ContentType might be set later for CSV export

        if (!isAdmin(request)) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        try {
            if (action == null || action.isEmpty() || "view".equalsIgnoreCase(action)) {
                response.setContentType("text/html; charset=UTF-8");
                
                BigDecimal totalSales = statsDAO.getTotalSalesAmount();
                BigDecimal salesLast30Days = statsDAO.getSalesAmountLastNDays(30);
                List<Map<String, Object>> topSellingProducts = statsDAO.getTopSellingProducts(5);
                List<Map<String, Object>> topViewedProducts = statsDAO.getTopViewedProducts(5);
                int newCustomersLast30Days = statsDAO.getNewCustomersLastNDays(30);
                Map<String, Integer> orderCountByStatus = statsDAO.getOrderCountByStatus();

                request.setAttribute("totalSales", totalSales);
                request.setAttribute("salesLast30Days", salesLast30Days);
                request.setAttribute("topSellingProducts", topSellingProducts);
                request.setAttribute("topViewedProducts", topViewedProducts);
                request.setAttribute("newCustomersLast30Days", newCustomersLast30Days);
                request.setAttribute("orderCountByStatus", orderCountByStatus);

                request.getRequestDispatcher("/admin/admin_stats_dashboard.jsp").forward(request, response);

            } else if ("export_sales_report".equalsIgnoreCase(action)) {
                response.setContentType("text/csv; charset=UTF-8"); // Ensure UTF-8 for CSV
                response.setHeader("Content-Disposition", "attachment; filename=\"sales_report_" + System.currentTimeMillis() + ".csv\"");
                
                // Fetch completed orders - Assuming OrderDAO has getOrdersByStatus(status, page, pageSize)
                // And it can handle a very large pageSize for "all" or has a specific method.
                // For simplicity, using a large number for pageSize. A dedicated method in DAO would be better.
                // The current OrderDAO.getOrdersByStatus is paginated, so this is a simplification.
                // A proper implementation might need a new DAO method or iterate through pages.
                // For this example, let's assume we get all completed orders by one call (not ideal for huge data).
                List<Order> completedOrders = orderDAO.getOrdersByStatus("已完成", 1, Integer.MAX_VALUE); 
                                                                                                   
                PrintWriter out = response.getWriter();
                // CSV Header
                out.println("\"订单ID\",\"用户ID\",\"订单日期\",\"总金额\",\"状态\",\"收货地址\"");

                // CSV Data
                if (completedOrders != null) {
                    for (Order order : completedOrders) {
                        out.print(csvEscape(String.valueOf(order.getOrderId())) + ",");
                        out.print(csvEscape(String.valueOf(order.getUserId())) + ",");
                        out.print(csvEscape(order.getOrderDate() != null ? order.getOrderDate().toString() : "") + ",");
                        out.print(csvEscape(order.getTotalAmount() != null ? order.getTotalAmount().toString() : "0.00") + ",");
                        out.print(csvEscape(order.getStatus()) + ",");
                        out.println(csvEscape(order.getShippingAddress()));
                    }
                }
                out.flush();
                out.close();
            } else {
                response.setContentType("text/html; charset=UTF-8");
                session.setAttribute("warningMessage", "无效的统计操作。");
                response.sendRedirect(request.getContextPath() + "/admin/stats?action=view");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html; charset=UTF-8");
            session.setAttribute("errorMessage", "处理统计操作时出错: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/stats?action=view");
        }
    }
    
    // Simple CSV field escaping for values containing comma, quote or newline
    private String csvEscape(String data) {
        if (data == null) return "";
        String escapedData = data.replace("\"", "\"\""); // Escape quotes
        if (escapedData.contains(",") || escapedData.contains("\n") || escapedData.contains("\"")) {
            escapedData = "\"" + escapedData + "\""; // Enclose in quotes
        }
        return escapedData;
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Most stats views are GET, POST could be for date range submissions etc.
        doGet(request, response);
    }
}
