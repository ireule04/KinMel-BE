package com.onlineshopping.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.onlineshopping.dto.AddToCartRequest;
import com.onlineshopping.dto.CartResponse;
import com.onlineshopping.dto.CommonApiResponse;
import com.onlineshopping.resource.CartResource;

@RestController
@RequestMapping("api/user/")
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

	@Autowired
	private CartResource cartResource;

	@PostMapping("cart/add")
	public ResponseEntity<CommonApiResponse> add(@RequestBody AddToCartRequest addToCartRequest) {
		return this.cartResource.add(addToCartRequest);
	}

	@GetMapping("mycart")
	public ResponseEntity<CartResponse> getMyCart(@RequestParam("userId") int userId) throws JsonProcessingException {
		return this.cartResource.getMyCart(userId);
	}

	@GetMapping("mycart/remove")
	public ResponseEntity<CommonApiResponse> removeCartItem(@RequestParam("cartId") int cartId)
			throws JsonProcessingException {

		return this.cartResource.removeCartItem(cartId);
	}

}
