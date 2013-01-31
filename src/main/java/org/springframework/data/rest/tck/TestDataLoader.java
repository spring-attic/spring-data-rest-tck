package org.springframework.data.rest.tck;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.tck.jpa.domain.Address;
import org.springframework.data.rest.tck.jpa.domain.Customer;
import org.springframework.data.rest.tck.jpa.domain.EmailAddress;
import org.springframework.data.rest.tck.jpa.domain.Product;
import org.springframework.data.rest.tck.jpa.repository.CustomerRepository;
import org.springframework.data.rest.tck.jpa.repository.OrderRepository;
import org.springframework.data.rest.tck.jpa.repository.ProductRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Jon Brisbin
 */
@Component
public class TestDataLoader implements InitializingBean {

  private static final Logger LOG = LoggerFactory.getLogger(TestDataLoader.class);
  @Autowired
  private CustomerRepository customers;
  @Autowired
  private ProductRepository  products;
  @Autowired
  private OrderRepository    orders;

  @Override public void afterPropertiesSet() throws Exception {
    saveCustomers();
    saveProducts();
  }

  @Transactional
  public void saveCustomers() {
    LOG.info("Loading Customer domain objects...");
    Customer c1 = new Customer("John", "Doe");
    c1.add(new Address("123 W 1st Street", "Univille", "USA"));
    c1.setEmailAddress(new EmailAddress("john.doe@gmail.com"));
    customers.save(c1);

    Customer c2 = new Customer("Jane", "Doe");
    c2.add(new Address("123 W 1st Street", "Univille", "USA"));
    c2.setEmailAddress(new EmailAddress("jane.doe@gmail.com"));
    customers.save(c2);
  }

  @Transactional
  public void saveProducts() {
    LOG.info("Loading Product domain objects...");
    Product p1 = new Product("Ginsu Knife",
                             BigDecimal.valueOf(100.0),
                             "The fabulous Ginsu knife that will cut through a car!");
    products.save(p1);

    Product p2 = new Product("Transformers Lunchbox",
                             BigDecimal.valueOf(25.0),
                             "Vintage Transformers lunchbox, complete with thermos.");
    products.save(p2);
  }

}
