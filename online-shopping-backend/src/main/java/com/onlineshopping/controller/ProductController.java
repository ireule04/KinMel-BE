package com.onlineshopping.controller;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onlineshopping.dto.CommonApiResponse;
import com.onlineshopping.dto.ProductAddRequest;
import com.onlineshopping.dto.ProductResponse;
import com.onlineshopping.resource.ProductResource;

@RestController
@RequestMapping("api/product")
@CrossOrigin(origins = "http://localhost:3000")
public class ProductController {

	@Autowired
	private ProductResource productResource;

	@PostMapping("add")
	public ResponseEntity<CommonApiResponse> addProduct(ProductAddRequest productDto) {
		return this.productResource.addProduct(productDto);
	}

	@DeleteMapping("delete/{productId}")
	public ResponseEntity<CommonApiResponse> deleteProduct(@PathVariable("productId") int productId) {
		return this.productResource.deleteProductById(productId);
	}

	@GetMapping("all")
	public ResponseEntity<ProductResponse> getAllProducts() {
		return this.productResource.getAllProducts();
	}

	@GetMapping("id")
	public ResponseEntity<ProductResponse> getProductById(@RequestParam("productId") int productId) {
		return this.productResource.getProductById(productId);
	}

	@GetMapping("category")
	public ResponseEntity<?> getProductsByCategories(@RequestParam("categoryId") int categoryId) {
		return this.productResource.getProductsByCategories(categoryId);
	}

	@GetMapping(value = "/{productImageName}", produces = "image/*")
	public void fetchProductImage(@PathVariable("productImageName") String productImageName, HttpServletResponse resp) {
		this.productResource.fetchProductImage(productImageName, resp);
	}
	
	
	
	
	@GetMapping("similar")
	public ResponseEntity<?> getSimilarProducts(@RequestParam("categoryId") int categoryId) {
	    return this.productResource.getProductsByCategories(categoryId);
	}


}
