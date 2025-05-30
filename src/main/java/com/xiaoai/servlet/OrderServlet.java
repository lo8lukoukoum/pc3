package com.xiaoai.servlet;

import com.xiaoai.dao.CartDAO;
import com.xiaoai.dao.OrderDAO;
import com.xiaoai.dao.ProductDAO;
import com.xiaoai.entity.Cart;
import com.xiaoai.entity.CartItem;
import com.xiaoai.entity.Order;
import com.xiaoai.entity.OrderItem;
import com.xiaoai.entity.Product;
import com.xiaoai.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/order")
public class OrderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private OrderDAO orderDAO;
    private CartDAO cartDAO;
    private ProductDAO productDAO;
    private static final int ORDERS_PER_PAGE = 10;

    @Override
    public void init() throws ServletException {
        super.init();
        orderDAO = new OrderDAO();
        cartDAO = new CartDAO();
        productDAO = new ProductDAO();
    }

    private boolean ensureUserLoggedIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            String requestURI = request.getRequestURI();
            if (request.getQueryString() != null) {
                requestURI += "?" + request.getQueryString();
            }
             HttpSession tempSession = request.getSession(true); // Ensure session exists for messages
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
            if (action == null || action.isEmpty() || "list".equalsIgnoreCase(action)) { // View user's order list
                String pageParam = request.getParameter("page");
                int pageNumber = 1;
                if (pageParam != null && !pageParam.isEmpty()) {
                    try {
                        pageNumber = Integer.parseInt(pageParam);
                        if (pageNumber < 1) pageNumber = 1;
                    } catch (NumberFormatException e) { pageNumber = 1; }
                }

                List<Order> orderList = orderDAO.getOrdersByUserId(loggedInUser.getUserId(), pageNumber, ORDERS_PER_PAGE);
                int totalOrders = orderDAO.getTotalOrderCountByUserId(loggedInUser.getUserId());
                int totalPages = (int) Math.ceil((double) totalOrders / ORDERS_PER_PAGE);
                 if (totalPages == 0 && totalOrders > 0) totalPages = 1;


                request.setAttribute("orderList", orderList);
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("currentPage", pageNumber);
                request.getRequestDispatcher("order_list.jsp").forward(request, response);

            } else if ("detail".equalsIgnoreCase(action)) { // View a specific order's details
                String orderIdParam = request.getParameter("orderId");
                if (orderIdParam == null || orderIdParam.isEmpty()) {
                    session.setAttribute("errorMessage", "未指定订单ID。");
                    response.sendRedirect(request.getContextPath() + "/order?action=list");
                    return;
                }
                int orderId = Integer.parseInt(orderIdParam);
                Order orderDetails = orderDAO.getOrderById(orderId);

                if (orderDetails == null) {
                    session.setAttribute("errorMessage", "找不到指定的订单。");
                    response.sendRedirect(request.getContextPath() + "/order?action=list");
                    return;
                }
                // Security check: Ensure the order belongs to the logged-in user (or user is admin - not handled here)
                if (orderDetails.getUserId() != loggedInUser.getUserId() && !"admin".equals(loggedInUser.getRole())) {
                    session.setAttribute("errorMessage", "您无权查看此订单。");
                    response.sendRedirect(request.getContextPath() + "/order?action=list");
                    return;
                }
                request.setAttribute("orderDetails", orderDetails);
                request.getRequestDispatcher("order_detail.jsp").forward(request, response);

            } else if ("checkout_page".equalsIgnoreCase(action)) { // Show the checkout page
                Cart cartForCheckout = cartDAO.getCartByUserId(loggedInUser.getUserId());
                if (cartForCheckout == null || cartForCheckout.getItems() == null || cartForCheckout.getItems().isEmpty()) {
                    session.setAttribute("warningMessage", "您的购物车是空的，无法结算。");
                    response.sendRedirect(request.getContextPath() + "/cart?action=view");
                    return;
                }
                request.setAttribute("cartForCheckout", cartForCheckout);
                request.getRequestDispatcher("checkout.jsp").forward(request, response);
            } else {
                session.setAttribute("warningMessage", "无效的订单操作。");
                response.sendRedirect(request.getContextPath() + "/index.jsp");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "无效的参数格式。");
            response.sendRedirect(request.getContextPath() + "/order?action=list");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "处理订单操作时发生错误。");
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

        if (!"create_order".equalsIgnoreCase(action)) {
            session.setAttribute("errorMessage", "无效的表单提交。");
            response.sendRedirect(request.getContextPath() + "/cart?action=view");
            return;
        }

        try {
            String shippingAddress = request.getParameter("shippingAddress");
            if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
                session.setAttribute("errorMessage", "收货地址不能为空！");
                request.setAttribute("shippingAddress", shippingAddress); // Preserve input for re-display
                // Re-populate cart for checkout page
                Cart cartForCheckout = cartDAO.getCartByUserId(loggedInUser.getUserId());
                request.setAttribute("cartForCheckout", cartForCheckout);
                request.getRequestDispatcher("checkout.jsp").forward(request, response);
                return;
            }

            Cart userCart = cartDAO.getCartByUserId(loggedInUser.getUserId());
            if (userCart == null || userCart.getItems() == null || userCart.getItems().isEmpty()) {
                session.setAttribute("errorMessage", "您的购物车是空的，无法创建订单。");
                response.sendRedirect(request.getContextPath() + "/cart?action=view");
                return;
            }

            // Stock check before creating order
            List<OrderItem> orderItems = new ArrayList<>();
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (CartItem cartItem : userCart.getItems()) {
                Product product = productDAO.getProductById(cartItem.getProductId());
                if (product == null) {
                     session.setAttribute("errorMessage", "购物车中的产品 ID: " + cartItem.getProductId() + " 无效。");
                     response.sendRedirect(request.getContextPath() + "/cart?action=view");
                     return;
                }
                if (product.getStockQuantity() < cartItem.getQuantity()) {
                    session.setAttribute("errorMessage", "抱歉，产品 '" + product.getName() + "' 库存不足 (仅剩 " + product.getStockQuantity() + " 件)。请修改购物车后重试。");
                    response.sendRedirect(request.getContextPath() + "/cart?action=view");
                    return;
                }
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(product.getProductId());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setPriceAtPurchase(product.getPrice()); // Price at time of order
                orderItems.add(orderItem);
                totalAmount = totalAmount.add(product.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
            }

            Order newOrder = new Order();
            newOrder.setUserId(loggedInUser.getUserId());
            newOrder.setShippingAddress(shippingAddress);
            newOrder.setStatus("待付款"); // Initial status
            newOrder.setOrderDate(new Timestamp(System.currentTimeMillis()));
            newOrder.setTotalAmount(totalAmount);
            newOrder.setItems(orderItems);

            int orderId = orderDAO.createOrder(newOrder); // This DAO method handles transaction

            if (orderId > 0) {
                cartDAO.clearCart(loggedInUser.getUserId()); // Clear cart after successful order
                session.setAttribute("successMessage", "订单创建成功！您的订单号是：" + orderId);
                response.sendRedirect(request.getContextPath() + "/order?action=detail&orderId=" + orderId);
            } else {
                session.setAttribute("errorMessage", "订单创建失败，请检查购物车或稍后再试。");
                // Re-populate cart for checkout page if redirecting there
                Cart cartForCheckout = cartDAO.getCartByUserId(loggedInUser.getUserId());
                request.setAttribute("cartForCheckout", cartForCheckout);
                request.setAttribute("shippingAddress", shippingAddress); // Preserve input
                request.getRequestDispatcher("checkout.jsp").forward(request, response);
            }

        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "创建订单过程中发生意外错误。");
            response.sendRedirect(request.getContextPath() + "/checkout.jsp"); // Or cart page
        }
    }
}
