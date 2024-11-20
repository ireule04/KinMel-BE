package com.onlineshopping.resource;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.onlineshopping.dao.CategoryDao;
import com.onlineshopping.dto.CategoryResponse;
import com.onlineshopping.dto.CommonApiResponse;
import com.onlineshopping.exception.CategorySaveFailedException;
import com.onlineshopping.model.Category;

@Component
public class CategoryResource {

	@Autowired
	private CategoryDao categoryDao;

	public ResponseEntity<CategoryResponse> getAllCategories() {
		CategoryResponse response = new CategoryResponse();

		List<Category> categories = this.categoryDao.findAll();

		if (CollectionUtils.isEmpty(categories)) {
			response.setResponseMessage("Product Categories not found");
			response.setSuccess(false);

			return new ResponseEntity<CategoryResponse>(response, HttpStatus.BAD_REQUEST);
		}

		response.setCategories(categories);
		response.setResponseMessage("Categories fetched successful!!!");
		response.setSuccess(true);

		return new ResponseEntity<CategoryResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<CommonApiResponse> add(Category category) {
		CommonApiResponse response = new CommonApiResponse();

		if (category == null) {
			response.setResponseMessage("bad request - missing input");
			response.setSuccess(true);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		Category savedCategory = categoryDao.save(category);

		if (savedCategory == null) {
			throw new CategorySaveFailedException("Failed to save the Category");
		}

		else {
			response.setResponseMessage("Category added successful");
			response.setSuccess(true);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
		}

	}

}
