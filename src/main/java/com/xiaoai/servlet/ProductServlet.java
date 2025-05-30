package com.xiaoai.servlet;

import com.xiaoai.dao.ProductDAO;
import com.xiaoai.entity.Product;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList; // Import if initializing products to empty list

@WebServlet("/products")
public class ProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProductDAO productDAO;
    private static final int PAGE_SIZE = 12; // Products per page

    @Override
    public void init() throws ServletException {
        super.init();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=UTF-8");

        String searchTerm = request.getParameter("searchTerm");
        String category = request.getParameter("category");
        String status = request.getParameter("status"); // e.g., "新品", "热销"
        String pageParam = request.getParameter("page");

        int pageNumber = 1;
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                pageNumber = Integer.parseInt(pageParam);
                if (pageNumber < 1) {
                    pageNumber = 1;
                }
            } catch (NumberFormatException e) {
                pageNumber = 1; // Default to page 1 if parsing fails
                e.printStackTrace();
            }
        }

        List<Product> products = new ArrayList<>(); // Initialize to avoid null pointers
        int totalProducts = 0;

        try {
            // Priority: Status > Search/Category > All
            if (status != null && !status.trim().isEmpty()) {
                products = productDAO.getProductsByStatus(status, pageNumber, PAGE_SIZE);
                totalProducts = productDAO.getTotalProductCountByStatus(status);
            } else if ((searchTerm != null && !searchTerm.trim().isEmpty()) || (category != null && !category.trim().isEmpty())) {
                // Current ProductDAO.searchProducts takes (searchTerm, category, pageNumber, pageSize)
                // If status needs to be integrated here, ProductDAO.searchProducts would need modification.
                // For now, status is handled as a separate, higher-priority filter.
                products = productDAO.searchProducts(searchTerm, category, pageNumber, PAGE_SIZE);
                totalProducts = productDAO.getTotalSearchProductCount(searchTerm, category);
            } else {
                products = productDAO.getAllProducts(pageNumber, PAGE_SIZE);
                totalProducts = productDAO.getTotalProductCount();
            }
        } catch (Exception e) {
            // Log error, maybe set an error message for the JSP
            e.printStackTrace();
            request.setAttribute("errorMessage", "加载产品数据时出错，请稍后再试。");
            // products will remain an empty list
            totalProducts = 0;
        }


        int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);
        if (totalPages == 0 && totalProducts > 0) { // Ensure at least one page if there are products
             totalPages = 1;
        }


        request.setAttribute("products", products);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("currentPage", pageNumber);
        
        // Persist search/filter parameters for the view
        if (searchTerm != null) request.setAttribute("searchTerm", searchTerm);
        if (category != null) request.setAttribute("selectedCategory", category);
        if (status != null) request.setAttribute("selectedStatus", status);
        // Add other parameters like sortOrder if implemented

        request.getRequestDispatcher("product_list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Most product browsing/searching actions can be handled via GET for bookmarkability
        // POST might be used if search criteria are very complex or sensitive,
        // but for typical e-commerce search, GET is common.
        doGet(request, response);
    }
}
