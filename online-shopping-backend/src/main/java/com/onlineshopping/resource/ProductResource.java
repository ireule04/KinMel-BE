package com.onlineshopping.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.FileCopyUtils;

import com.onlineshopping.dao.CategoryDao;
import com.onlineshopping.dao.ProductDao;
import com.onlineshopping.dto.CommonApiResponse;
import com.onlineshopping.dto.ProductAddRequest;
import com.onlineshopping.dto.ProductResponse;
import com.onlineshopping.model.Category;
import com.onlineshopping.model.Product;
import com.onlineshopping.service.ProductService;
import com.onlineshopping.utility.StorageService;

@Component
public class ProductResource {

	@Autowired
	private ProductService productService;

	@Autowired
	private ProductDao productDao;

	@Autowired
	private CategoryDao categoryDao;

	@Autowired
	private StorageService storageService;

	public ResponseEntity<CommonApiResponse> addProduct(ProductAddRequest productDto) {
		CommonApiResponse response = new CommonApiResponse();

		if (productDto == null) {
			response.setResponseMessage("bad request - missing request");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		if (!ProductAddRequest.validateProduct(productDto)) {
			response.setResponseMessage("bad request - missing field");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);

		}

		Product product = ProductAddRequest.toEntity(productDto);

		if (product == null) {
			response.setResponseMessage("bad request - missing field");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);

		}

		Optional<Category> optional = categoryDao.findById(productDto.getCategoryId());
		Category category = null;
		if (optional.isPresent()) {
			category = optional.get();
		}

		if (category == null) {
			response.setResponseMessage("please select correct product category");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);

		}

		product.setCategory(category);

		try {
			productService.addProduct(product, productDto.getImage());

			response.setResponseMessage("Product Added Successfully!");
			response.setSuccess(true);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
		}

		response.setResponseMessage("Failed to add the Product");
		response.setSuccess(false);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	public ResponseEntity<ProductResponse> getAllProducts() {
		ProductResponse response = new ProductResponse();

		List<Product> products = productDao.findAll();

		if (CollectionUtils.isEmpty(products)) {
			response.setResponseMessage("Products not found!");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.OK);
		}

		response.setProducts(products);
		response.setResponseMessage("Products fetched successfully!");
		response.setSuccess(true);

		return new ResponseEntity<ProductResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<ProductResponse> getProductById(int productId) {
		ProductResponse response = new ProductResponse();

		if (productId == 0) {
			response.setResponseMessage("Product ID is missing");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<Product> optional = productDao.findById(productId);

		if (optional.isEmpty()) {
			response.setResponseMessage("Product not found");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.BAD_REQUEST);
		}

		response.setProducts(Arrays.asList(optional.get()));
		response.setResponseMessage("Product fetched successfully!");
		response.setSuccess(true);

		return new ResponseEntity<ProductResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<?> getProductsByCategories(int categoryId) {
		ProductResponse response = new ProductResponse();

		if (categoryId == 0) {
			response.setResponseMessage("Category ID is missing");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Optional<Category> optional = categoryDao.findById(categoryId);

		if (optional.isEmpty()) {
			response.setResponseMessage("Category not found!");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<Product> products = productDao.findByCategoryId(categoryId);

		if (CollectionUtils.isEmpty(products)) {
			response.setResponseMessage("Products not found!");
			response.setSuccess(false);

			return new ResponseEntity<ProductResponse>(response, HttpStatus.OK);
		}

		response.setProducts(products);
		response.setResponseMessage("Products fetched successfully!");
		response.setSuccess(true);

		return new ResponseEntity<ProductResponse>(response, HttpStatus.OK);
	}

	public void fetchProductImage(String productImageName, HttpServletResponse resp) {
		Resource resource = storageService.load(productImageName);
		if (resource != null) {
			try (InputStream in = resource.getInputStream()) {
				ServletOutputStream out = resp.getOutputStream();
				FileCopyUtils.copy(in, out);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// New delete product by ID method
	public ResponseEntity<CommonApiResponse> deleteProductById(int productId) {
		CommonApiResponse response = new CommonApiResponse();

		Optional<Product> optionalProduct = productDao.findById(productId);
		if (optionalProduct.isEmpty()) {
			response.setResponseMessage("Product not found");
			response.setSuccess(false);

			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}

		try {
			productDao.deleteById(productId);
			response.setResponseMessage("Product deleted successfully!");
			response.setSuccess(true);

			return new ResponseEntity<>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.setResponseMessage("Failed to delete the product");
			response.setSuccess(false);

			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}