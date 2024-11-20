package com.onlineshopping.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onlineshopping.dto.AddUserRequest;
import com.onlineshopping.dto.UserLoginRequest;
import com.onlineshopping.dto.UserResponse;
import com.onlineshopping.resource.UserResource;

@RestController
@RequestMapping("api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

	@Autowired
	UserResource userResource;

	@PostMapping("register")
	public ResponseEntity<UserResponse> registerUser(@RequestBody AddUserRequest userRequest) {
		return this.userResource.registerUser(userRequest);
	}

	@PostMapping("login")
	public ResponseEntity<UserResponse> loginUser(@RequestBody UserLoginRequest loginRequest) {
		return this.userResource.loginUser(loginRequest);
	}

	@GetMapping("deliveryperson/all")
	public ResponseEntity<UserResponse> getAllDeliveryPersons() {
		return this.userResource.getAllDeliveryPersons();
	} 
	
	@PostMapping("forget-password")
	public ResponseEntity<UserResponse> forgetPassword(@RequestBody UserLoginRequest request) {
		return this.userResource.forgetPassword(request);
	}

}
