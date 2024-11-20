package com.onlineshopping.resource;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.onlineshopping.dao.CartDao;
import com.onlineshopping.dao.OrderDao;
import com.onlineshopping.dao.ProductDao;
import com.onlineshopping.dao.UserDao;
import com.onlineshopping.dto.CommonApiResponse;
import com.onlineshopping.dto.MyOrderResponse;
import com.onlineshopping.dto.UpdateDeliveryStatusRequest;
import com.onlineshopping.dto.UserOrderResponse;
import com.onlineshopping.exception.OrderSaveFailedException;
import com.onlineshopping.model.Cart;
import com.onlineshopping.model.Orders;
import com.onlineshopping.model.User;
import com.onlineshopping.utility.Constants.DeliveryStatus;
import com.onlineshopping.utility.Constants.DeliveryTime;
import com.onlineshopping.utility.Constants.IsDeliveryAssigned;
import com.onlineshopping.utility.Helper;

@Component
@Transactional
public class OrderResource {

	@Autowired
	private OrderDao orderDao;

	@Autowired
	private CartDao cartDao;

	@Autowired
	private UserDao userDao;

	@Autowired
	private ProductDao productDao;

	public ResponseEntity<CommonApiResponse> customerOrder(int userId) {
		CommonApiResponse response = new CommonApiResponse();

		if (userId == 0) {
			response.setResponseMessage("bad request - missing field");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		String orderId = Helper.getAlphaNumericOrderId();

		List<Cart> userCarts = cartDao.findByUser_id(userId);

		if (CollectionUtils.isEmpty(userCarts)) {
			response.setResponseMessage("Your Cart is Empty!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.BAD_REQUEST);
		}

		LocalDateTime currentDateTime = LocalDateTime.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
		String formatDateTime = currentDateTime.format(formatter);

		try {

			for (Cart cart : userCarts) {

				Orders order = new Orders();
				order.setOrderId(orderId);
				order.setUser(cart.getUser());
				order.setProduct(cart.getProduct());
				order.setQuantity(cart.getQuantity());
				order.setOrderDate(formatDateTime);
				order.setDeliveryDate(DeliveryStatus.PENDING.value());
				order.setDeliveryStatus(DeliveryStatus.PENDING.value());
				order.setDeliveryTime(DeliveryTime.DEFAULT.value());
				order.setDeliveryAssigned(IsDeliveryAssigned.NO.value());

				Orders savedOrder = orderDao.save(order);
				
				if(savedOrder == null) {
					throw new OrderSaveFailedException("Failed to save the Order");
				}
				cartDao.delete(cart);
			}

		} catch (Exception e) {
			response.setResponseMessage("Failed to Order Products!!!");
			response.setSuccess(false);

			return new ResponseEntity<CommonApiResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

		response.setResponseMessage("Your Order Placed, Order Id: " + orderId);
		response.setSuccess(true);

		return new ResponseEntity<CommonApiResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> getMyOrder(int userId) {
		UserOrderResponse response = new UserOrderResponse();

		if (userId == 0) {
			response.setResponseMessage("User Id missing");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<Orders> userOrder = orderDao.findByUser_id(userId);

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		if (CollectionUtils.isEmpty(userOrder)) {
			response.setResponseMessage("Orders not found");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
		}

		for (Orders order : userOrder) {
			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(order.getProduct().getDescription());
			orderData.setProductName(order.getProduct().getTitle());
			orderData.setProductImage(order.getProduct().getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(order.getProduct().getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(order.getProduct().getPrice().toString())));
			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {

				User deliveryPerson = null;

				Optional<User> optionalDeliveryPerson = this.userDao.findById(order.getDeliveryPersonId());

				deliveryPerson = optionalDeliveryPerson.get();

				orderData.setDeliveryPersonContact(deliveryPerson.getPhoneNo());
				orderData.setDeliveryPersonName(deliveryPerson.getFirstName());
			}
			orderDatas.add(orderData);
		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> getAllOrder() {
		UserOrderResponse response = new UserOrderResponse();

		List<Orders> userOrder = orderDao.findAll();

		if (CollectionUtils.isEmpty(userOrder)) {
			response.setResponseMessage("Orders not found");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
		}

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		for (Orders order : userOrder) {
			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(order.getProduct().getDescription());
			orderData.setProductName(order.getProduct().getTitle());
			orderData.setProductImage(order.getProduct().getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(order.getProduct().getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(order.getProduct().getPrice().toString())));
			orderData.setUserId(order.getUser().getId());
			orderData.setUserName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
			orderData.setUserPhone(order.getUser().getPhoneNo());
			orderData.setAddress(order.getUser().getAddress());
			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {
				User deliveryPerson = null;

				Optional<User> optionalDeliveryPerson = this.userDao.findById(order.getDeliveryPersonId());

				deliveryPerson = optionalDeliveryPerson.get();

				orderData.setDeliveryPersonContact(deliveryPerson.getPhoneNo());
				orderData.setDeliveryPersonName(deliveryPerson.getFirstName());
			}
			orderDatas.add(orderData);

		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> getOrdersByOrderId(String orderId) {
		UserOrderResponse response = new UserOrderResponse();

		if (orderId == null) {
			response.setResponseMessage("Orders not found");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
		}

		List<Orders> userOrder = orderDao.findByOrderId(orderId);

		if (CollectionUtils.isEmpty(userOrder)) {
			response.setResponseMessage("Orders not found");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
		}

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		for (Orders order : userOrder) {
			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(order.getProduct().getDescription());
			orderData.setProductName(order.getProduct().getTitle());
			orderData.setProductImage(order.getProduct().getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(order.getProduct().getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(order.getProduct().getPrice().toString())));
			orderData.setUserId(order.getUser().getId());
			orderData.setUserName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
			orderData.setUserPhone(order.getUser().getPhoneNo());
			orderData.setAddress(order.getUser().getAddress());
			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {
				User deliveryPerson = null;

				Optional<User> optionalDeliveryPerson = this.userDao.findById(order.getDeliveryPersonId());

				deliveryPerson = optionalDeliveryPerson.get();

				orderData.setDeliveryPersonContact(deliveryPerson.getPhoneNo());
				orderData.setDeliveryPersonName(deliveryPerson.getFirstName());
			}
			orderDatas.add(orderData);

		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> updateOrderDeliveryStatus(UpdateDeliveryStatusRequest deliveryRequest) {
		UserOrderResponse response = new UserOrderResponse();

		if (deliveryRequest == null) {
			response.setResponseMessage("bad request - missing request");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<Orders> orders = orderDao.findByOrderId(deliveryRequest.getOrderId());

		if (CollectionUtils.isEmpty(orders)) {
			response.setResponseMessage("Orders not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		for (Orders order : orders) {
			order.setDeliveryDate(deliveryRequest.getDeliveryDate());
			order.setDeliveryStatus(deliveryRequest.getDeliveryStatus());
			order.setDeliveryTime(deliveryRequest.getDeliveryTime());
			orderDao.save(order);
		}

		List<Orders> userOrder = orderDao.findByOrderId(deliveryRequest.getOrderId());

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		for (Orders order : userOrder) {
			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(order.getProduct().getDescription());
			orderData.setProductName(order.getProduct().getTitle());
			orderData.setProductImage(order.getProduct().getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(order.getProduct().getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(order.getProduct().getPrice().toString())));
			orderData.setUserId(order.getUser().getId());
			orderData.setUserName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
			orderData.setUserPhone(order.getUser().getPhoneNo());
			orderData.setAddress(order.getUser().getAddress());
			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {
				User deliveryPerson = null;

				Optional<User> optionalDeliveryPerson = this.userDao.findById(order.getDeliveryPersonId());

				deliveryPerson = optionalDeliveryPerson.get();

				orderData.setDeliveryPersonContact(deliveryPerson.getPhoneNo());
				orderData.setDeliveryPersonName(deliveryPerson.getFirstName());
			}
			orderDatas.add(orderData);

		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> assignDeliveryPersonForOrder(UpdateDeliveryStatusRequest deliveryRequest) {
		UserOrderResponse response = new UserOrderResponse();

		if (deliveryRequest == null) {
			response.setResponseMessage("bad request - missing request");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<Orders> orders = orderDao.findByOrderId(deliveryRequest.getOrderId());

		if (CollectionUtils.isEmpty(orders)) {
			response.setResponseMessage("Orders not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		for (Orders order : orders) {
			order.setDeliveryAssigned(IsDeliveryAssigned.YES.value());
			order.setDeliveryPersonId(deliveryRequest.getDeliveryId());
			orderDao.save(order);
		}

		List<Orders> userOrder = orderDao.findByOrderId(deliveryRequest.getOrderId());

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		for (Orders order : userOrder) {
			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(order.getProduct().getDescription());
			orderData.setProductName(order.getProduct().getTitle());
			orderData.setProductImage(order.getProduct().getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(order.getProduct().getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(order.getProduct().getPrice().toString())));
			orderData.setUserId(order.getUser().getId());
			orderData.setUserName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
			orderData.setUserPhone(order.getUser().getPhoneNo());
			orderData.setAddress(order.getUser().getAddress());

			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {
				User dPerson = null;

				Optional<User> optionalPerson = this.userDao.findById(order.getDeliveryPersonId());

				dPerson = optionalPerson.get();

				orderData.setDeliveryPersonContact(dPerson.getPhoneNo());
				orderData.setDeliveryPersonName(dPerson.getFirstName());
			}

			orderDatas.add(orderData);

		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

	public ResponseEntity<UserOrderResponse> getMyDeliveryOrders(int deliveryPersonId) {
		UserOrderResponse response = new UserOrderResponse();

		if (deliveryPersonId == 0) {
			response.setResponseMessage("bad request - missing field");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		User person = null;

		Optional<User> oD = this.userDao.findById(deliveryPersonId);

		if (oD.isPresent()) {
			person = oD.get();
		}

		List<Orders> userOrder = orderDao.findByDeliveryPersonId(deliveryPersonId);

		if (CollectionUtils.isEmpty(userOrder)) {
			response.setResponseMessage("Orders not found!!!");
			response.setSuccess(false);

			return new ResponseEntity<UserOrderResponse>(response, HttpStatus.BAD_REQUEST);
		}

		List<MyOrderResponse> orderDatas = new ArrayList<>();

		for (Orders order : userOrder) {
			MyOrderResponse orderData = new MyOrderResponse();
			orderData.setOrderId(order.getOrderId());
			orderData.setProductDescription(order.getProduct().getDescription());
			orderData.setProductName(order.getProduct().getTitle());
			orderData.setProductImage(order.getProduct().getImageName());
			orderData.setQuantity(order.getQuantity());
			orderData.setOrderDate(order.getOrderDate());
			orderData.setProductId(order.getProduct().getId());
			orderData.setDeliveryDate(order.getDeliveryDate() + " " + order.getDeliveryTime());
			orderData.setDeliveryStatus(order.getDeliveryStatus());
			orderData.setTotalPrice(
					String.valueOf(order.getQuantity() * Double.parseDouble(order.getProduct().getPrice().toString())));
			orderData.setUserId(order.getUser().getId());
			orderData.setUserName(order.getUser().getFirstName() + " " + order.getUser().getLastName());
			orderData.setUserPhone(order.getUser().getPhoneNo());
			orderData.setAddress(order.getUser().getAddress());

			if (order.getDeliveryPersonId() == 0) {
				orderData.setDeliveryPersonContact(DeliveryStatus.PENDING.value());
				orderData.setDeliveryPersonName(DeliveryStatus.PENDING.value());
			}

			else {
				orderData.setDeliveryPersonContact(person.getPhoneNo());
				orderData.setDeliveryPersonName(person.getFirstName());
			}

			orderDatas.add(orderData);

		}

		response.setOrders(orderDatas);
		response.setResponseMessage("Order Fetched Successful!!");
		response.setSuccess(true);

		return new ResponseEntity<UserOrderResponse>(response, HttpStatus.OK);
	}

}
