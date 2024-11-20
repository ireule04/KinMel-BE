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
import com.onlineshopping.dto.CommonApiResponse;
import com.onlineshopping.dto.UpdateDeliveryStatusRequest;
import com.onlineshopping.dto.UserOrderResponse;
import com.onlineshopping.resource.OrderResource;

@RestController
@RequestMapping("api/user/")
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class OrderController {

	@Autowired
	private OrderResource orderResource;

	@PostMapping("order")
	public ResponseEntity<CommonApiResponse> customerOrder(@RequestParam("userId") int userId)
			throws JsonProcessingException {
		return this.orderResource.customerOrder(userId);
	}

	@GetMapping("myorder")
	public ResponseEntity<UserOrderResponse> getMyOrder(@RequestParam("userId") int userId)
			throws JsonProcessingException {
		return this.orderResource.getMyOrder(userId);
	}

	@GetMapping("admin/allorder")
	public ResponseEntity<UserOrderResponse> getAllOrder() throws JsonProcessingException {
		return this.orderResource.getAllOrder();
	}

	@GetMapping("admin/showorder")
	public ResponseEntity<UserOrderResponse> getOrdersByOrderId(@RequestParam("orderId") String orderId)
			throws JsonProcessingException {
		return this.orderResource.getOrdersByOrderId(orderId);
	}

	@PostMapping("admin/order/deliveryStatus/update")
	public ResponseEntity<UserOrderResponse> updateOrderDeliveryStatus(
			@RequestBody UpdateDeliveryStatusRequest deliveryRequest) throws JsonProcessingException {
		return this.orderResource.updateOrderDeliveryStatus(deliveryRequest);
	}

	@PostMapping("admin/order/assignDelivery")
	public ResponseEntity<UserOrderResponse> assignDeliveryPersonForOrder(
			@RequestBody UpdateDeliveryStatusRequest deliveryRequest) throws JsonProcessingException {
		return this.orderResource.assignDeliveryPersonForOrder(deliveryRequest);
	}

	@GetMapping("delivery/myorder")
	public ResponseEntity<UserOrderResponse> getMyDeliveryOrders(@RequestParam("deliveryPersonId") int deliveryPersonId)
			throws JsonProcessingException {
		return this.orderResource.getMyDeliveryOrders(deliveryPersonId);
	}

}
