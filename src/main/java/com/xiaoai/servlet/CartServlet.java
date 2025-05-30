package com.xiaoai.servlet;

import com.xiaoai.dao.CartDAO;
import com.xiaoai.dao.ProductDAO; // For stock checking
import com.xiaoai.entity.Cart;
import com.xiaoai.entity.User;
import com.xiaoai.entity.Product; // For stock checking

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private CartDAO cartDAO;
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        cartDAO = new CartDAO();
        productDAO = new ProductDAO();
    }

    private boolean ensureUserLoggedIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            // Store the intended URL to redirect back after login
            String requestURI = request.getRequestURI();
            if (request.getQueryString() != null) {
                requestURI += "?" + request.getQueryString();
            }
            if(session != null) { // if session exists but no user, can still set message
                 session.setAttribute("redirectAfterLogin", requestURI);
                 session.setAttribute("warningMessage", "请先登录再操作购物车！");
            } else { // if no session at all, create one for messages
                 HttpSession tempSession = request.getSession(true);
                 tempSession.setAttribute("redirectAfterLogin", requestURI);
                 tempSession.setAttribute("warningMessage", "请先登录再操作购物车！");
            }
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

        if (action == null || action.isEmpty() || "view".equalsIgnoreCase(action)) {
            try {
                Cart cart = cartDAO.getCartByUserId(loggedInUser.getUserId());
                request.setAttribute("cart", cart); // CartDAO's getCartByUserId should load items with product details
                request.getRequestDispatcher("cart.jsp").forward(request, response);
            } catch (Exception e) {
                e.printStackTrace();
                session.setAttribute("errorMessage", "查看购物车失败，请稍后再试。");
                response.sendRedirect(request.getContextPath() + "/index.jsp"); // Or some error page
            }
        } else {
            // Handle other GET actions if any, or redirect
            session.setAttribute("warningMessage", "无效的购物车操作。");
            response.sendRedirect(request.getContextPath() + "/cart?action=view");
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
        String redirectUrl = request.getContextPath() + "/cart?action=view"; // Default redirect

        if (action == null || action.trim().isEmpty()) {
            session.setAttribute("errorMessage", "无效的操作。");
            response.sendRedirect(redirectUrl);
            return;
        }

        try {
            int productId = 0;
            int quantity = 0;

            if (action.equals("add") || action.equals("update") || action.equals("remove")) {
                String productIdParam = request.getParameter("productId");
                if (productIdParam != null && !productIdParam.isEmpty()) {
                    productId = Integer.parseInt(productIdParam);
                } else if (!action.equals("clear")){ // clear action doesn't need productId
                    session.setAttribute("errorMessage", "缺少产品ID。");
                    response.sendRedirect(redirectUrl);
                    return;
                }
            }
             if (action.equals("add") || action.equals("update")) {
                String quantityParam = request.getParameter("quantity");
                 if (quantityParam != null && !quantityParam.isEmpty()) {
                    quantity = Integer.parseInt(quantityParam);
                } else {
                    session.setAttribute("errorMessage", "缺少数量参数。");
                    response.sendRedirect(redirectUrl);
                    return;
                }
            }


            switch (action) {
                case "add":
                    Product product = productDAO.getProductById(productId);
                    if (product == null) {
                        session.setAttribute("errorMessage", "找不到要添加的产品。");
                        break;
                    }
                    if (product.getStockQuantity() < quantity) {
                        session.setAttribute("errorMessage", "产品 '" + product.getName() + "' 库存不足 (仅剩 " + product.getStockQuantity() + " 件)。");
                        // Redirect back to product page or referer
                        String referer = request.getHeader("Referer");
                        redirectUrl = (referer != null && !referer.isEmpty()) ? referer : request.getContextPath() + "/products";
                        break;
                    }
                    if (cartDAO.addCartItem(loggedInUser.getUserId(), productId, quantity)) {
                        session.setAttribute("successMessage", "商品 '" + product.getName() + "' 已添加到购物车！");
                    } else {
                        session.setAttribute("errorMessage", "添加商品到购物车失败。");
                    }
                    // Redirect back to the page where 'add to cart' was clicked, if possible
                    String refererAdd = request.getHeader("Referer");
                    if (refererAdd != null && !refererAdd.contains("/cart")) { // Avoid redirecting to cart if already on cart page
                        redirectUrl = refererAdd;
                    } else {
                         redirectUrl = request.getContextPath() + "/products"; // Default to products page
                    }
                    break;
                case "update":
                    if (cartDAO.updateCartItemQuantity(loggedInUser.getUserId(), productId, quantity)) {
                        session.setAttribute("successMessage", "购物车已更新。");
                    } else {
                        session.setAttribute("errorMessage", "更新购物车失败。");
                    }
                    break;
                case "remove":
                    if (cartDAO.removeCartItem(loggedInUser.getUserId(), productId)) {
                        session.setAttribute("successMessage", "商品已从购物车移除。");
                    } else {
                        session.setAttribute("errorMessage", "从购物车移除商品失败。");
                    }
                    break;
                case "clear":
                    if (cartDAO.clearCart(loggedInUser.getUserId())) {
                        session.setAttribute("successMessage", "购物车已清空。");
                    } else {
                        session.setAttribute("errorMessage", "清空购物车失败。");
                    }
                    break;
                default:
                    session.setAttribute("errorMessage", "无效的购物车操作：" + action);
                    break;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "无效的产品ID或数量格式。");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "处理购物车操作时发生错误，请稍后再试。");
        }
        response.sendRedirect(redirectUrl);
    }
}
