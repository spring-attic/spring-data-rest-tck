package org.springframework.data.rest.tck;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.tck.jpa.domain.Address;
import org.springframework.data.rest.tck.jpa.domain.Customer;
import org.springframework.data.rest.tck.jpa.domain.EmailAddress;
import org.springframework.data.rest.tck.jpa.domain.LineItem;
import org.springframework.data.rest.tck.jpa.domain.Order;
import org.springframework.data.rest.tck.jpa.domain.Product;
import org.springframework.data.rest.tck.jpa.repository.CustomerRepository;
import org.springframework.data.rest.tck.jpa.repository.OrderRepository;
import org.springframework.data.rest.tck.jpa.repository.ProductRepository;
import org.springframework.stereotype.Component;

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
    LOG.info("Loading Customer domain objects...");
    Customer c1 = new Customer("John", "Doe");
    c1.add(new Address("123 W 1st Street", "Univille", "USA"));
    c1.setEmailAddress(new EmailAddress("john.doe@gmail.com"));
    c1 = customers.save(c1);

    Customer c2 = new Customer("Jane", "Doe");
    c2.add(new Address("123 W 1st Street", "Univille", "USA"));
    c2.setEmailAddress(new EmailAddress("jane.doe@gmail.com"));
    c2 = customers.save(c2);

    LOG.info("Loading Product domain objects...");
    Product p1 = new Product("Ginsu Knife",
                             BigDecimal.valueOf(100.0),
                             "The fabulous Ginsu knife that will cut through a car!");
    p1 = products.save(p1);

    Product p2 = new Product("Transformers Lunchbox",
                             BigDecimal.valueOf(25.0),
                             "Vintage Transformers lunchbox, complete with thermos.");
    p2 = products.save(p2);

    LOG.info("Loading Order domain objects...");
    Order o1 = new Order(c1, c1.getAddresses().iterator().next());
    o1.add(new LineItem(p1, 1));
    orders.save(o1);

    Order o2 = new Order(c2, c1.getAddresses().iterator().next());
    o2.add(new LineItem(p2, 1));
    orders.save(o2);

  }

}
