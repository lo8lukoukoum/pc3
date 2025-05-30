package com.xiaoai.servlet;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class UserLogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false); // Do not create session if it doesn't exist

        if (session != null) {
            session.removeAttribute("loggedInUser"); // Remove user attribute
            session.invalidate(); // Invalidate the session
        }

        // Set a success message for the login page (this will create a new session)
        HttpSession newSession = request.getSession(true); 
        newSession.setAttribute("successMessage", "您已成功退出登录。");

        // Redirect to login page
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Delegate POST requests to doGet for logout
        doGet(request, response);
    }
}
