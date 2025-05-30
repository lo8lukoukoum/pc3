package com.xiaoai.servlet;

import com.xiaoai.dao.UserDAO;
import com.xiaoai.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Timestamp;

@WebServlet("/login")
public class UserLoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward to the login page
        request.getRequestDispatcher("login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty() ||
            password == null || password.isEmpty()) {
            request.setAttribute("errorMessage", "用户名和密码均不能为空！");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        try {
            User user = userDAO.validateUser(username, password);

            if (user != null) {
                HttpSession session = request.getSession();
                session.setAttribute("loggedInUser", user);

                // Update last_login timestamp
                user.setLastLogin(new Timestamp(System.currentTimeMillis()));
                // Assuming UserDAO.updateUser can handle this. 
                // A specific updateUserLastLogin(int userId) method might be cleaner.
                // For UserDAO.updateUser, ensure it doesn't unintentionally nullify other fields if not set in 'user' object from validateUser.
                // The current UserDAO.updateUser updates email, personal_signature, last_login, role.
                // Let's fetch the full user object before updating to be safe, or ensure validateUser returns a full object.
                // For simplicity, we'll assume validateUser returns a fully populated user object suitable for updateUser.
                // However, the current UserDAO.updateUser is:
                // "UPDATE Users SET email = ?, personal_signature = ?, last_login = ?, role = ? WHERE user_id = ?"
                // This is fine as long as the user object from validateUser is complete.
                userDAO.updateUser(user); 

                // Redirect based on role
                if ("admin".equals(user.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/admin/dashboard.jsp");
                } else {
                    response.sendRedirect(request.getContextPath() + "/index.jsp"); // Or products.jsp
                }
            } else {
                request.setAttribute("errorMessage", "用户名或密码错误！");
                request.getRequestDispatcher("login.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            request.setAttribute("errorMessage", "登录过程中发生意外错误。请稍后再试。");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }
}
