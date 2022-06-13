package product.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import product.model.dto.ProductBrand;
import product.model.service.ProductService;

/**
 * Servlet implementation class ProductBrandNameFinderServlet
 */
@WebServlet("/product/findAutoComplete")
public class ProductBrandNameFinderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ProductService productService = new ProductService();
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 1. 사용자 입력값 처리
		String search = request.getParameter("search");
		System.out.println("search = " + search);
								
		// 2. 업무로직
		List<ProductBrand> brandList = productService.findAllBrandIds();
		List<String> searchResult = new ArrayList<>();
		
		for(int i = 0; i < brandList.size(); i++) {
			String findVal = brandList.get(i).getBrandName();
			if(findVal.contains(search)) {
				searchResult.add(brandList.get(i).getBrandId()+"_"+findVal);				
			}
		}

		// 3. 응답처리
		response.setContentType("text/csv; charset=utf-8");
		PrintWriter out = response.getWriter();
		for(int i = 0; i < searchResult.size(); i++) {
			out.append(searchResult.get(i));
			if(i != brandList.size() - 1) {
				out.append(",");
			}
		}
	}

}
