package com.xiaoai.servlet.admin;

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
import java.util.List;
import java.util.ArrayList; // Required for initializing list

@WebServlet("/admin/customers")
public class AdminCustomerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UserDAO userDAO;
    private static final int CUSTOMERS_PER_PAGE = 15;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAO();
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

        try {
            if (action == null || action.isEmpty() || "list".equalsIgnoreCase(action)) {
                String pageParam = request.getParameter("page");
                int pageNumber = 1;
                if (pageParam != null && !pageParam.isEmpty()) {
                    try {
                        pageNumber = Integer.parseInt(pageParam);
                        if (pageNumber < 1) pageNumber = 1;
                    } catch (NumberFormatException e) { pageNumber = 1; }
                }

                // Assume UserDAO.getAllUsers() is modified to support pagination
                // Or a new UserDAO.getAllUsers(page, size) is created.
                // For now, using existing getAllUsers() and noting limitation.
                // List<User> customerList = userDAO.getAllUsers(pageNumber, CUSTOMERS_PER_PAGE);
                // int totalCustomers = userDAO.getTotalUserCount();
                // Fallback: use existing getAllUsers and note limitation
                List<User> allUsers = userDAO.getAllUsers(); // This gets all users, not paginated by current DAO
                List<User> customerList = new ArrayList<>();
                int totalCustomers = allUsers.size();
                
                int totalPages = (int) Math.ceil((double) totalCustomers / CUSTOMERS_PER_PAGE);
                 if (totalPages == 0 && totalCustomers > 0) totalPages = 1;


                // Manual pagination if UserDAO isn't updated
                int start = (pageNumber - 1) * CUSTOMERS_PER_PAGE;
                int end = Math.min(start + CUSTOMERS_PER_PAGE, totalCustomers);
                if(start < totalCustomers) {
                    customerList = allUsers.subList(start, end);
                }


                request.setAttribute("customerList", customerList);
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("currentPage", pageNumber);
                request.getRequestDispatcher("/admin/admin_customer_list.jsp").forward(request, response);

            } else if ("add_form".equalsIgnoreCase(action)) {
                request.setAttribute("customerToEdit", new User()); // For form model
                request.setAttribute("formAction", "add_customer");
                request.getRequestDispatcher("/admin/admin_customer_form.jsp").forward(request, response);

            } else if ("edit_form".equalsIgnoreCase(action)) {
                String userIdParam = request.getParameter("userId");
                if (userIdParam == null || userIdParam.isEmpty()) {
                    session.setAttribute("errorMessage", "未指定用户ID进行编辑。");
                    response.sendRedirect(request.getContextPath() + "/admin/customers?action=list");
                    return;
                }
                int userId = Integer.parseInt(userIdParam);
                User customer = userDAO.getUserById(userId);
                if (customer != null) {
                    request.setAttribute("customerToEdit", customer);
                    request.setAttribute("formAction", "edit_customer");
                    request.getRequestDispatcher("/admin/admin_customer_form.jsp").forward(request, response);
                } else {
                    session.setAttribute("errorMessage", "找不到要编辑的用户。");
                    response.sendRedirect(request.getContextPath() + "/admin/customers?action=list");
                }
            } else if ("delete".equalsIgnoreCase(action)) { // GET delete
                String userIdParam = request.getParameter("userId");
                if (userIdParam == null || userIdParam.isEmpty()) {
                    session.setAttribute("errorMessage", "未指定用户ID进行删除。");
                } else {
                    int userId = Integer.parseInt(userIdParam);
                     // Prevent admin from deleting themselves via GET link
                    User loggedInAdmin = (User) session.getAttribute("loggedInUser");
                    if (loggedInAdmin.getUserId() == userId) {
                        session.setAttribute("errorMessage", "管理员不能删除自己的账户。");
                    } else if (userDAO.deleteUser(userId)) {
                        session.setAttribute("successMessage", "用户 ID: " + userId + " 已成功删除。");
                    } else {
                        session.setAttribute("errorMessage", "删除用户 ID: " + userId + " 失败。");
                    }
                }
                response.sendRedirect(request.getContextPath() + "/admin/customers?action=list");
            } else {
                session.setAttribute("warningMessage", "无效的管理员客户操作。");
                response.sendRedirect(request.getContextPath() + "/admin/customers?action=list");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "无效的用户ID格式。");
            response.sendRedirect(request.getContextPath() + "/admin/customers?action=list");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "处理管理员客户操作时出错: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/customers?action=list");
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
        String redirectToList = request.getContextPath() + "/admin/customers?action=list";

        try {
            if ("add_customer".equalsIgnoreCase(action) || "edit_customer".equalsIgnoreCase(action)) {
                String username = request.getParameter("username");
                String email = request.getParameter("email");
                String role = request.getParameter("role");
                String personalSignature = request.getParameter("personalSignature");
                String userIdStr = request.getParameter("userId"); // For editing

                // Validation
                if (username == null || username.trim().isEmpty() ||
                    email == null || email.trim().isEmpty() ||
                    role == null || role.trim().isEmpty()) {
                    session.setAttribute("errorMessage", "用户名、邮箱和角色不能为空。");
                    // Forward back to form with preserved data (simplified redirect here)
                    if ("add_customer".equalsIgnoreCase(action)) response.sendRedirect(request.getContextPath() + "/admin/customers?action=add_form");
                    else response.sendRedirect(request.getContextPath() + "/admin/customers?action=edit_form&userId=" + userIdStr);
                    return;
                }


                if ("add_customer".equalsIgnoreCase(action)) {
                    String password = request.getParameter("password");
                    if (password == null || password.isEmpty()) {
                        session.setAttribute("errorMessage", "新增用户时密码不能为空。");
                        response.sendRedirect(request.getContextPath() + "/admin/customers?action=add_form");
                        return;
                    }
                    if (userDAO.isUsernameExists(username)) {
                        session.setAttribute("errorMessage", "用户名 '" + username + "' 已存在。");
                        response.sendRedirect(request.getContextPath() + "/admin/customers?action=add_form");
                        return;
                    }

                    User newUser = new User();
                    newUser.setUsername(username);
                    newUser.setPassword(password); // No encryption as per earlier requirements
                    newUser.setEmail(email);
                    newUser.setRole(role);
                    newUser.setPersonalSignature(personalSignature);
                    newUser.setRegistrationDate(new Timestamp(System.currentTimeMillis()));
                    
                    if (userDAO.addUser(newUser)) {
                        session.setAttribute("successMessage", "用户 '" + newUser.getUsername() + "' 添加成功！");
                    } else {
                        session.setAttribute("errorMessage", "添加用户失败。");
                    }
                } else { // edit_customer
                    int userId = Integer.parseInt(userIdStr);
                    User userToUpdate = userDAO.getUserById(userId);
                    if (userToUpdate == null) {
                        session.setAttribute("errorMessage", "找不到要更新的用户。");
                        response.sendRedirect(redirectToList);
                        return;
                    }
                    // Check if username is being changed and if new username already exists (if username change is allowed)
                    if (!userToUpdate.getUsername().equals(username) && userDAO.isUsernameExists(username)) {
                         session.setAttribute("errorMessage", "用户名 '" + username + "' 已被其他用户占用。");
                         response.sendRedirect(request.getContextPath() + "/admin/customers?action=edit_form&userId=" + userIdStr);
                         return;
                    }
                    userToUpdate.setUsername(username); // If username change is allowed
                    userToUpdate.setEmail(email);
                    userToUpdate.setRole(role);
                    userToUpdate.setPersonalSignature(personalSignature);
                    // Password is not updated here. Use reset_password action.
                    
                    if (userDAO.updateUser(userToUpdate)) { // Assumes updateUser doesn't change password if not explicitly set
                        session.setAttribute("successMessage", "用户 '" + userToUpdate.getUsername() + "' 更新成功！");
                    } else {
                        session.setAttribute("errorMessage", "更新用户失败。");
                    }
                }
            } else if ("delete_customer_post".equalsIgnoreCase(action)) {
                String userIdStr = request.getParameter("userId");
                 if (userIdStr == null || userIdStr.isEmpty()) {
                    session.setAttribute("errorMessage", "删除用户时缺少用户ID。");
                } else {
                    int userId = Integer.parseInt(userIdStr);
                    User loggedInAdmin = (User) session.getAttribute("loggedInUser");
                    if (loggedInAdmin.getUserId() == userId) {
                        session.setAttribute("errorMessage", "管理员不能通过此方式删除自己的账户。");
                    } else if (userDAO.deleteUser(userId)) {
                        session.setAttribute("successMessage", "用户 ID: " + userId + " 已成功删除。");
                    } else {
                        session.setAttribute("errorMessage", "删除用户 ID: " + userId + " 失败。");
                    }
                }
            } else if ("reset_password".equalsIgnoreCase(action)) {
                String userIdStr = request.getParameter("userId");
                String newPassword = request.getParameter("newPassword");
                 if (userIdStr == null || userIdStr.isEmpty() || newPassword == null || newPassword.isEmpty()) {
                    session.setAttribute("errorMessage", "重置密码时用户ID和新密码均不能为空。");
                } else {
                    int userId = Integer.parseInt(userIdStr);
                    if (userDAO.updatePassword(userId, newPassword)) {
                        session.setAttribute("successMessage", "用户 ID: " + userId + " 的密码已成功重置。");
                    } else {
                        session.setAttribute("errorMessage", "重置用户 ID: " + userId + " 的密码失败。");
                    }
                }
                // Redirect back to edit form or list
                if (userIdStr != null && !userIdStr.isEmpty()) {
                    redirectToList = request.getContextPath() + "/admin/customers?action=edit_form&userId=" + userIdStr;
                }
            } else {
                session.setAttribute("warningMessage", "无效的POST客户管理操作。");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "用户ID格式无效。");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "处理管理员客户操作时发生严重错误: " + e.getMessage());
        }
        response.sendRedirect(redirectToList);
    }
}
