package org.springframework.data.rest.tck.jpa;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.tck.jpa.domain.Account;
import org.springframework.data.rest.tck.jpa.domain.Address;
import org.springframework.data.rest.tck.jpa.domain.Customer;
import org.springframework.data.rest.tck.jpa.domain.EmailAddress;
import org.springframework.data.rest.tck.jpa.domain.LineItem;
import org.springframework.data.rest.tck.jpa.domain.Order;
import org.springframework.data.rest.tck.jpa.domain.Product;
import org.springframework.data.rest.tck.jpa.domain.User;
import org.springframework.data.rest.tck.jpa.repository.AccountRepository;
import org.springframework.data.rest.tck.jpa.repository.AddressRepository;
import org.springframework.data.rest.tck.jpa.repository.CustomerRepository;
import org.springframework.data.rest.tck.jpa.repository.LineItemRepository;
import org.springframework.data.rest.tck.jpa.repository.OrderRepository;
import org.springframework.data.rest.tck.jpa.repository.ProductRepository;
import org.springframework.data.rest.tck.jpa.repository.UserRepository;
import org.springframework.stereotype.Component;

/**
 * @author Jon Brisbin
 */
@Component
public class TestDataLoader {

  private static final Logger LOG = LoggerFactory.getLogger(TestDataLoader.class);
  private final CustomerRepository customers;
  private final AddressRepository  addresses;
  private final ProductRepository  products;
  private final OrderRepository    orders;
  private final LineItemRepository lineItems;
  private final UserRepository users;
  private final AccountRepository accounts;

  @Autowired
  public TestDataLoader(CustomerRepository customers,
                        AddressRepository addresses,
                        ProductRepository products,
                        OrderRepository orders,
                        LineItemRepository lineItems, 
                        UserRepository users, 
                        AccountRepository accounts) {
    this.customers = customers;
    this.addresses = addresses;
    this.products = products;
    this.orders = orders;
    this.lineItems = lineItems;
    this.users = users;
    this.accounts = accounts;
  }

  public void loadData() {
    if(customers.findAll().iterator().hasNext()) {
      return;
    }
    List<Customer> customers = saveCustomers();
    List<Product> products = saveProducts();

    for(int i = 0; i < 2; i++) {
      saveOrder(customers.get(i), new Address("123 W 1st Street", "Univille", "USA"), products.get(i));
    }
  }

  private List<Customer> saveCustomers() {
    LOG.debug("Loading Customer domain objects...");

    List<Customer> customers = new ArrayList<>();
    Customer c1 = new Customer("John", "Doe");
    c1.add(new Address("123 W 1st Street", "Univille", "USA"));
    c1.setEmailAddress(new EmailAddress("john.doe@gmail.com"));
    customers.add(this.customers.save(c1));

    Customer c2 = new Customer("Jane", "Doe");
    c2.add(new Address("123 W 1st Street", "Univille", "USA"));
    c2.setEmailAddress(new EmailAddress("jane.doe@gmail.com"));
    customers.add(this.customers.save(c2));

    return customers;
  }

  private List<Product> saveProducts() {
    LOG.debug("Loading Product domain objects...");

    List<Product> products = new ArrayList<>();
    Product p1 = new Product("Ginsu Knife",
                             BigDecimal.valueOf(100.0),
                             "The fabulous Ginsu knife that will cut through a car!");
    products.add(this.products.save(p1));

    Product p2 = new Product("Transformers Lunchbox",
                             BigDecimal.valueOf(25.0),
                             "Vintage Transformers lunchbox, complete with thermos.");
    products.add(this.products.save(p2));

    return products;
  }

  private void saveOrder(Customer c, Address a, Product p) {
    LOG.debug("Loading Order domain objects...");

    Order o = new Order(c, a);
    o.add(new LineItem(p, 1));

    orders.save(o);
  }
  
  public void createUsersAndAccounts() {
  	
  	User user = new User("Oliver", "Gierke");
  	Account account = new Account("Twitter", users.save(user));
  	
  	accounts.save(account);
  }
}
