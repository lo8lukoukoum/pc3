<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%-- 
    This is an example layout file. 
    In a real application, individual JSPs (like index.jsp, login.jsp) 
    would typically include the header and footer directly.
    This file demonstrates how such includes work.
--%>

<% 
    // Setting a page title for demonstration. 
    // Actual pages should set this attribute before including header.jspf.
    request.setAttribute("pageTitle", "布局示例"); 
%>

<jsp:include page="/WEB-INF/jspf/header.jspf" />

<%-- Start of main page content for this specific page --%>
<div class="container"> <%-- This specific container is for the content of this layout example page --%>
    <section id="showcase">
        <h1>欢迎来到小爱交易网站</h1>
        <p>这是一个使用 `main_layout.jsp` 包含 `header.jspf` 和 `footer.jspf` 的示例页面。</p>
        <p>在实际应用中，您会创建具体的页面 (例如 `index.jsp`, `product_list.jsp`) 并直接在这些文件中包含页眉和页脚片段。</p>
    </section>

    <section class="features">
        <div class="feature-box">
            <h3>高质量商品</h3>
            <p>我们提供各种精选的高质量商品，满足您的不同需求。</p>
        </div>
        <div class="feature-box">
            <h3>安全交易</h3>
            <p>安全的支付系统和用户数据保护，让您购物无忧。</p>
        </div>
        <div class="feature-box">
            <h3>优质客户服务</h3>
            <p>专业的客服团队随时为您提供帮助。</p>
        </div>
    </section>
</div>
<%-- End of main page content for this specific page --%>

<jsp:include page="/WEB-INF/jspf/footer.jspf" />
