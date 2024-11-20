package com.onlineshopping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.onlineshopping.dao.UserDao;
import com.onlineshopping.model.User;

@SpringBootApplication
public class OnlineShoppingApplication implements CommandLineRunner {

	private final Logger LOG = LoggerFactory.getLogger(OnlineShoppingApplication.class);

	@Autowired
	private UserDao userDao;

	public static void main(String[] args) {
		SpringApplication.run(OnlineShoppingApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		User admin = this.userDao.findByEmailIdAndRole("demo.admin@demo.com", "Admin");

		if (admin == null) {

			LOG.error("Admin not found!!!");
			User user = new User();
			user.setEmailId("demo.admin@demo.com");
			user.setPassword("123456");
			user.setRole("Admin");

			this.userDao.save(user);
			LOG.error("Created Default Demo Admin!!!");

		}

	}

}
