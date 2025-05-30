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

@WebServlet("/register")
public class UserRegistrationServlet extends HttpServlet {
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
        // Forward to the registration page
        request.getRequestDispatcher("register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");
        String personalSignature = request.getParameter("personalSignature");

        // Basic validation for empty fields (can be enhanced)
        if (username == null || username.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.isEmpty() ||
            confirmPassword == null || confirmPassword.isEmpty()) {
            request.setAttribute("errorMessage", "所有必填字段都不能为空！");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        if (!password.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "两次输入的密码不匹配！");
            request.getRequestDispatcher("register.jsp").forward(request, response);
            return;
        }

        // More sophisticated email validation can be added here if needed
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
             request.setAttribute("errorMessage", "邮箱格式无效！");
             request.getRequestDispatcher("register.jsp").forward(request, response);
             return;
        }


        try {
            if (userDAO.isUsernameExists(username)) {
                request.setAttribute("errorMessage", "用户名 '" + username + "' 已存在！");
                request.getRequestDispatcher("register.jsp").forward(request, response);
                return;
            }

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(password); // Password not encrypted as per requirements
            newUser.setEmail(email);
            newUser.setPersonalSignature(personalSignature);
            newUser.setRole("user"); // Default role for new registrations
            newUser.setRegistrationDate(new Timestamp(System.currentTimeMillis()));

            boolean success = userDAO.addUser(newUser);

            if (success) {
                HttpSession session = request.getSession();
                session.setAttribute("successMessage", "注册成功！请登录。");
                response.sendRedirect(request.getContextPath() + "/login.jsp");
            } else {
                request.setAttribute("errorMessage", "注册失败，数据库错误或未知问题。请稍后再试。");
                request.getRequestDispatcher("register.jsp").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            request.setAttribute("errorMessage", "注册过程中发生意外错误。请稍后再试。");
            request.getRequestDispatcher("register.jsp").forward(request, response);
        }
    }
}
