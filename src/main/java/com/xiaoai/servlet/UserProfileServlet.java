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

@WebServlet("/profile")
public class UserProfileServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
    }

    private boolean ensureUserLoggedIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("loggedInUser") == null) {
            String requestURI = request.getRequestURI();
             HttpSession tempSession = request.getSession(true); // Ensure session exists for messages
             tempSession.setAttribute("redirectAfterLogin", requestURI);
             tempSession.setAttribute("warningMessage", "请先登录再访问个人资料页面！");
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

        // Refresh user data from DB in case it changed elsewhere, or rely on session version
        // For profile page, it's good practice to show the latest data.
        User freshUserProfile = userDAO.getUserById(loggedInUser.getUserId());
        if (freshUserProfile != null) {
             // Update session with fresh data, especially if UserDAO.updateUser only updates specific fields.
             session.setAttribute("loggedInUser", freshUserProfile); 
             request.setAttribute("userProfile", freshUserProfile);
        } else {
            // Handle case where user might have been deleted, though unlikely if session is valid
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login.jsp?errorMessage=用户数据异常，请重新登录");
            return;
        }
        
        request.getRequestDispatcher("profile.jsp").forward(request, response);
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

        if ("update_signature".equalsIgnoreCase(action)) {
            String newPersonalSignature = request.getParameter("personalSignature");

            // Basic validation (e.g., length) - can be added here
            if (newPersonalSignature == null) { // Allow empty signature
                newPersonalSignature = "";
            }
            
            // Max length example (e.g. TEXT column might allow much more, but UI might limit)
            if (newPersonalSignature.length() > 500) { 
                request.setAttribute("errorMessage", "个人签名过长，最多500字符。");
                request.setAttribute("userProfile", loggedInUser); // Send back current user for form repopulation
                request.getRequestDispatcher("profile.jsp").forward(request, response);
                return;
            }

            // Fetch the latest user object from DB to avoid overwriting other concurrent changes
            User userFromDB = userDAO.getUserById(loggedInUser.getUserId());
            if (userFromDB == null) {
                session.setAttribute("errorMessage", "无法获取用户信息，请稍后再试。");
                response.sendRedirect(request.getContextPath() + "/profile");
                return;
            }

            userFromDB.setPersonalSignature(newPersonalSignature);
            
            // The UserDAO.updateUser updates: email, personal_signature, last_login, role.
            // This means if we only want to update signature, other fields from userFromDB must be accurate.
            // If UserDAO.updateUser was more granular or used dynamic SQL, this would be cleaner.
            // For now, this approach is taken as instructed if a dedicated DAO method isn't added.
            boolean success = userDAO.updateUser(userFromDB);

            if (success) {
                // Update the user object in the session
                session.setAttribute("loggedInUser", userFromDB);
                session.setAttribute("successMessage", "个人签名更新成功！");
            } else {
                // Use request attribute for error message on POST-redirect-GET pattern if forwarding,
                // but since we redirect, session is better for messages.
                session.setAttribute("errorMessage", "个人签名更新失败，请稍后再试。");
            }
        } else {
            session.setAttribute("warningMessage", "无效的操作。");
        }
        response.sendRedirect(request.getContextPath() + "/profile"); // Redirect back to profile page (GET)
    }
}
