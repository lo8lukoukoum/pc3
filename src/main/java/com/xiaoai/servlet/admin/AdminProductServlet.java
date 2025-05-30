package com.xiaoai.servlet.admin;

import com.xiaoai.dao.ProductDAO;
import com.xiaoai.entity.Product;
import com.xiaoai.entity.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList; // Required for initializing list

@WebServlet("/admin/products")
@MultipartConfig // For handling file uploads
public class AdminProductServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ProductDAO productDAO;
    private static final int PRODUCTS_PER_PAGE = 10;
    private static final String UPLOAD_DIRECTORY = "images" + File.separator + "products"; // Relative path for storing images

    @Override
    public void init() throws ServletException {
        super.init();
        productDAO = new ProductDAO();
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
                List<Product> productList = productDAO.getAllProducts(pageNumber, PRODUCTS_PER_PAGE);
                int totalProducts = productDAO.getTotalProductCount();
                int totalPages = (int) Math.ceil((double) totalProducts / PRODUCTS_PER_PAGE);
                if (totalPages == 0 && totalProducts > 0) totalPages = 1;

                request.setAttribute("productList", productList);
                request.setAttribute("totalPages", totalPages);
                request.setAttribute("currentPage", pageNumber);
                request.getRequestDispatcher("/admin/admin_product_list.jsp").forward(request, response);

            } else if ("add_form".equalsIgnoreCase(action)) {
                request.setAttribute("productToEdit", new Product()); // For form model binding
                request.setAttribute("formAction", "add_product");
                request.getRequestDispatcher("/admin/admin_product_form.jsp").forward(request, response);

            } else if ("edit_form".equalsIgnoreCase(action)) {
                String productIdParam = request.getParameter("productId");
                if (productIdParam == null || productIdParam.isEmpty()) {
                    session.setAttribute("errorMessage", "未指定产品ID进行编辑。");
                    response.sendRedirect(request.getContextPath() + "/admin/products?action=list");
                    return;
                }
                int productId = Integer.parseInt(productIdParam);
                Product product = productDAO.getProductById(productId);
                if (product != null) {
                    request.setAttribute("productToEdit", product);
                    request.setAttribute("formAction", "edit_product");
                    request.getRequestDispatcher("/admin/admin_product_form.jsp").forward(request, response);
                } else {
                    session.setAttribute("errorMessage", "找不到要编辑的产品。");
                    response.sendRedirect(request.getContextPath() + "/admin/products?action=list");
                }
            } else if ("delete".equalsIgnoreCase(action)) { // GET delete
                String productIdParam = request.getParameter("productId");
                 if (productIdParam == null || productIdParam.isEmpty()) {
                    session.setAttribute("errorMessage", "未指定产品ID进行删除。");
                } else {
                    int productId = Integer.parseInt(productIdParam);
                    Product productToDelete = productDAO.getProductById(productId); // Get product to delete its image
                    if (productToDelete != null && productToDelete.getImageUrl() != null && !productToDelete.getImageUrl().isEmpty()) {
                        deleteProductImage(productToDelete.getImageUrl(), request);
                    }
                    if (productDAO.deleteProduct(productId)) {
                        session.setAttribute("successMessage", "产品 ID: " + productId + " 已成功删除。");
                    } else {
                        session.setAttribute("errorMessage", "删除产品 ID: " + productId + " 失败。");
                    }
                }
                response.sendRedirect(request.getContextPath() + "/admin/products?action=list");
            } else {
                 session.setAttribute("warningMessage", "无效的管理员产品操作。");
                 response.sendRedirect(request.getContextPath() + "/admin/products?action=list");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "无效的产品ID格式。");
            response.sendRedirect(request.getContextPath() + "/admin/products?action=list");
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "处理管理员产品操作时出错: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/products?action=list");
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
        String redirectToList = request.getContextPath() + "/admin/products?action=list";

        try {
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String priceStr = request.getParameter("price");
            String stockStr = request.getParameter("stockQuantity");
            String category = request.getParameter("category");
            String status = request.getParameter("status");
            String productIdStr = request.getParameter("productId"); // For editing

            // Basic Validation
            if (name == null || name.trim().isEmpty() ||
                priceStr == null || priceStr.trim().isEmpty() ||
                stockStr == null || stockStr.trim().isEmpty() ||
                category == null || category.trim().isEmpty() ||
                status == null || status.trim().isEmpty()) {
                session.setAttribute("errorMessage", "所有必填字段（名称、价格、库存、分类、状态）都不能为空。");
                // Preserve form data and forward back (more complex for POST, usually handled by client-side or re-displaying form with values)
                if ("add_product".equalsIgnoreCase(action)) {
                     response.sendRedirect(request.getContextPath() + "/admin/products?action=add_form");
                } else if ("edit_product".equalsIgnoreCase(action) && productIdStr != null){
                     response.sendRedirect(request.getContextPath() + "/admin/products?action=edit_form&productId=" + productIdStr);
                } else {
                    response.sendRedirect(redirectToList);
                }
                return;
            }

            BigDecimal price;
            int stockQuantity;
            try {
                price = new BigDecimal(priceStr);
                stockQuantity = Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                session.setAttribute("errorMessage", "价格或库存数量格式无效。");
                 if ("add_product".equalsIgnoreCase(action)) {
                     response.sendRedirect(request.getContextPath() + "/admin/products?action=add_form");
                } else if ("edit_product".equalsIgnoreCase(action) && productIdStr != null){
                     response.sendRedirect(request.getContextPath() + "/admin/products?action=edit_form&productId=" + productIdStr);
                } else {
                    response.sendRedirect(redirectToList);
                }
                return;
            }

            Product product = new Product();
            product.setName(name);
            product.setDescription(description);
            product.setPrice(price);
            product.setStockQuantity(stockQuantity);
            product.setCategory(category);
            product.setStatus(status);
            product.setCreationDate(new Timestamp(System.currentTimeMillis())); // Set for new, or update if needed

            // Handle file upload
            Part filePart = request.getPart("imageFile");
            String fileName = null;
            if (filePart != null && filePart.getSize() > 0) {
                 fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
            }

            String existingImageUrl = request.getParameter("existingImageUrl"); // For edit mode

            if (fileName != null && !fileName.isEmpty()) {
                // Delete old image if updating and new image is provided
                if ("edit_product".equalsIgnoreCase(action) && existingImageUrl != null && !existingImageUrl.isEmpty()) {
                    deleteProductImage(existingImageUrl, request);
                }

                String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
                String appPath = request.getServletContext().getRealPath("");
                String uploadFilePath = appPath + File.separator + UPLOAD_DIRECTORY;
                
                File uploadDir = new File(uploadFilePath);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }
                filePart.write(uploadFilePath + File.separator + uniqueFileName);
                product.setImageUrl(UPLOAD_DIRECTORY.replace(File.separator, "/") + "/" + uniqueFileName); // Store relative path
            } else if ("edit_product".equalsIgnoreCase(action) && existingImageUrl != null) {
                product.setImageUrl(existingImageUrl); // Keep old image if no new one uploaded
            }


            if ("add_product".equalsIgnoreCase(action)) {
                if (productDAO.addProduct(product)) {
                    session.setAttribute("successMessage", "产品 '" + product.getName() + "' 添加成功！");
                } else {
                    session.setAttribute("errorMessage", "添加产品失败。");
                }
            } else if ("edit_product".equalsIgnoreCase(action)) {
                if (productIdStr == null || productIdStr.isEmpty()) {
                     session.setAttribute("errorMessage", "编辑产品时缺少产品ID。");
                     response.sendRedirect(redirectToList);
                     return;
                }
                product.setProductId(Integer.parseInt(productIdStr));
                // For edit, creationDate is usually not updated unless intended.
                // ProductDAO.addProduct sets it, ProductDAO.updateProduct doesn't touch it.
                // If getProductById was used to populate, it would have original creationDate.
                Product pExisting = productDAO.getProductById(product.getProductId());
                if(pExisting != null) product.setCreationDate(pExisting.getCreationDate());


                if (productDAO.updateProduct(product)) {
                    session.setAttribute("successMessage", "产品 '" + product.getName() + "' 更新成功！");
                } else {
                    session.setAttribute("errorMessage", "更新产品失败。");
                }
            } else if ("delete_product_post".equalsIgnoreCase(action)) {
                 if (productIdStr == null || productIdStr.isEmpty()) {
                    session.setAttribute("errorMessage", "删除产品时缺少产品ID。");
                } else {
                    int pid = Integer.parseInt(productIdStr);
                    Product productToDelete = productDAO.getProductById(pid);
                    if (productToDelete != null && productToDelete.getImageUrl() != null && !productToDelete.getImageUrl().isEmpty()) {
                        deleteProductImage(productToDelete.getImageUrl(), request);
                    }
                    if (productDAO.deleteProduct(pid)) {
                        session.setAttribute("successMessage", "产品 ID: " + pid + " 已成功删除。");
                    } else {
                        session.setAttribute("errorMessage", "删除产品 ID: " + pid + " 失败。");
                    }
                }
            } else {
                 session.setAttribute("warningMessage", "无效的POST产品操作。");
            }

        } catch (NumberFormatException e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "价格、库存或产品ID格式无效。");
        } catch (ServletException e) { // For file upload issues
            e.printStackTrace();
            session.setAttribute("errorMessage", "文件上传处理失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "处理管理员产品操作时发生严重错误: " + e.getMessage());
        }
        response.sendRedirect(redirectToList);
    }

    private void deleteProductImage(String imageUrl, HttpServletRequest request) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) return;
        try {
            String appPath = request.getServletContext().getRealPath("");
            String filePath = appPath + File.separator + imageUrl.replace("/", File.separator);
            File imageFile = new File(filePath);
            if (imageFile.exists()) {
                imageFile.delete();
                System.out.println("Deleted image file: " + filePath);
            }
        } catch (Exception e) {
            System.err.println("Error deleting image file " + imageUrl + ": " + e.getMessage());
            // Log this error but don't let it stop the main DB operation
        }
    }
}
