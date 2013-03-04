package org.springframework.data.rest.tck.jpa;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.tck.AbstractTckTest;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests that check the REST API of JPA entities that are exported through Spring Data REST.
 *
 * @author Jon Brisbin
 */
public class JpaTckTests extends AbstractTckTest {

  static final Logger LOG                    = LoggerFactory.getLogger(JpaTckTests.class);
  static final String SELF_REL               = "self";
  static final String CUSTOMERS_REL          = "customer";
  static final String CUSTOMER_REL           = "customer.customer";
  static final String CUSTOMER_ADDRESSES_REL = "customer.customer.addresses";
  static final String CUSTOMER_ADDRESS_REL   = "customer.customer.addresses.address";
  static final String PRODUCTS_REL           = "product";
  static final String PRODUCT_REL            = "product.product";
  static final String ORDERS_REL             = "order";
  static final String ORDER_REL              = "order.order";
  static final String ORDER_LINEITEMS_REL    = "order.order.lineItems";
  static final String ORDER_LINEITEM_REL     = "order.order.lineItems.lineItem";
  static final String LINEITEMS_REL          = "lineItem";
  static final String LINEITEM_REL           = "lineItem.lineItem";
  @Autowired
  protected TestDataLoader dataLoader;

  @Override protected void loadData() {
    dataLoader.loadData();
  }

  @Override protected void deleteData() {
  }

  /**
   * Test whether {@link org.springframework.data.repository.CrudRepository}s exported are discoverable.
   *
   * @throws Exception
   */
  @Test
  public void testDiscoverability() throws Exception {
    Link customer = customersLink();

    assertNotNull("Exposes a Link to manage Customers",
                  customer);
    assertThat("Customer Link looks correct",
               customer.getHref(),
               allOf(startsWith("http://"),
                     endsWith("/customer")));
  }

  /**
   * Test whether entities are exposed inline.
   *
   * @throws Exception
   */
  @Test
  public void testListsEntities() throws Exception {
    Link customer = customersLink();

    MockHttpServletResponse response = request(customer.getHref());
    String jsonBody = response.getContentAsString();

    assertThat("Entity data is exposed inline",
               JsonPath.read(jsonBody, "$content[0].lastname").toString(),
               is("Doe"));
  }

  /**
   * Tests whether entities are exposed as links using the {@literal application/x-spring-data-compact+json} JSON type.
   *
   * @throws Exception
   */
  @Test
  public void testLinksToEntities() throws Exception {
    List<Link> customers = linksToCustomers();

    assertThat("Entity is referenced as a Link",
               customers,
               Matchers.<Link>iterableWithSize(2));
  }

  /**
   * Test whether the {@link org.springframework.data.rest.tck.jpa.repository.CustomerRepository} exposes a CREATE
   * feature.
   *
   * @throws Exception
   */
  @Test
  public void testCreate() throws Exception {
    for(Link l : linksToCustomers()) {
      MockHttpServletResponse response = request(l.getHref());
      String jsonBody = response.getContentAsString();

      assertThat("Customer is a Doe",
                 JsonPath.read(jsonBody, "lastname").toString(),
                 is("Doe"));
      assertThat("Entity contains self Link",
                 links.findLinkWithRel(SELF_REL, jsonBody),
                 notNullValue());
      assertThat("Entity maintains addresses as Links",
                 links.findLinkWithRel(CUSTOMER_ADDRESSES_REL, jsonBody),
                 notNullValue());
    }

  }

  /**
   * Test whether linked properties expose the entity inline.
   *
   * @throws Exception
   */
  @Test
  public void testExposesAccessToLinkedEntities() throws Exception {
    for(Link l : linksToCustomers()) {
      List<Link> addresses = discover(l, CUSTOMER_ADDRESSES_REL);

      assertThat("Has linked Addresses",
                 addresses,
                 Matchers.<Link>iterableWithSize(1));

      Link addressLink = addresses.iterator().next();
      MockHttpServletResponse addrResponse = request(addressLink.getHref());
      String addrJsonBody = addrResponse.getContentAsString();

      assertThat("Has valid street",
                 String.format("%s", JsonPath.read(addrJsonBody, "$content[0].street")),
                 is("123 W 1st Street"));
    }
  }

  /**
   * Test that a PUT will do a partial update of the entity.
   *
   * @throws Exception
   */
  @Test
  public void testExposesUpdate() throws Exception {
    Link customer = linksToCustomers().iterator().next();
    byte[] bytes = Files.readAllBytes(Paths.get("src/test/resources/customer-update.txt"));

    mockMvc
        .perform(put(customer.getHref())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(bytes))
        .andExpect(status().isNoContent());

    MockHttpServletResponse response2 = request(customer.getHref());

    assertThat("Firstname field was updated correctly",
               JsonPath.read(response2.getContentAsString(), "firstname").toString(),
               is("Ralph"));
  }

  /**
   * Test that a DELETE really removes an entity from the datastore.
   *
   * @throws Exception
   */
  @Test
  public void testExposesDelete() throws Exception {
    List<Link> customers = linksToCustomers();
    Link customer = customers.get(customers.size() - 1);

    mockMvc
        .perform(delete(customer.getHref()))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get(customer.getHref()))
        .andExpect(status().isNotFound());
  }

  /**
   * Test that entities can be created with linked entities.
   *
   * @throws Exception
   */
  @Test
  public void testCreatesEntityWithLinkedProperties() throws Exception {
    List<Link> customers = linksToCustomers();
    List<Link> products = linksToProducts();

    Link customer = customers.get(0);
    Link addrSelf = follow(discover(customer, CUSTOMER_ADDRESSES_REL).get(0),
                           CUSTOMER_ADDRESS_REL,
                           "self").get(0);
    Link product = products.get(0);

    String jsonBody = new String(Files.readAllBytes(Paths.get("src/test/resources/new-order-1.txt")));
    jsonBody = jsonBody.replaceAll("%CUSTOMER_HREF%", customer.getHref());
    jsonBody = jsonBody.replaceAll("%ADDR_HREF%", addrSelf.getHref());
    jsonBody = jsonBody.replaceAll("%PRODUCT_HREF%", product.getHref());

    Link orders = ordersLink();
    String loc = mockMvc
        .perform(post(orders.getHref())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(jsonBody))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getHeader("Location");
    assertNotNull("Location header is not null", loc);

    MockHttpServletResponse checkResp = request(loc);
    String checkJsonBody = checkResp.getContentAsString();

    Link lineItemsLink = follow(new Link(loc), ORDER_LINEITEMS_REL, ORDER_LINEITEM_REL).get(0);
    assertNotNull("LineItems links is not null", lineItemsLink);

    mockMvc
        .perform(get(lineItemsLink.getHref()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("price", is(100.0)));

    Link custLink = links.findLinkWithRel("order.order.customer", checkJsonBody);
    assertNotNull("Customer link is not null", custLink);

    mockMvc
        .perform(get(custLink.getHref()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("content.lastname", is("Doe")));
  }

  @Test
  public void testAddNewLinkedEntity() throws Exception {
    testCreatesEntityWithLinkedProperties();

    List<Link> customers = linksToCustomers();
    List<Link> products = linksToProducts();
    Link lineItemsLink = lineItemsLink();

    String jsonBody = new String(Files.readAllBytes(Paths.get("src/test/resources/lineitem-json.txt")));
    jsonBody = jsonBody.replaceAll("%PRODUCT_HREF%", products.get(products.size() - 1).getHref());

    mockMvc
        .perform(post(lineItemsLink.getHref())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(jsonBody))
        .andExpect(status().isCreated());


  }

  @SuppressWarnings({"unchecked"})
  @Test
  public void testUpdateExistingLinkedEntity() throws Exception {
    List<Link> products = linksToProducts();
    Link lineItemsLink = lineItemsLink();
    Link orderLink = linksToOrders().get(0);
    Link orderLineItemsLink = discover(orderLink, ORDER_LINEITEMS_REL).get(0);

    String jsonBody = new String(Files.readAllBytes(Paths.get("src/test/resources/lineitem-json.txt")));
    jsonBody = jsonBody.replaceAll("%PRODUCT_HREF%", products.get(products.size() - 1).getHref());

    String lineItemLoc = mockMvc
        .perform(post(lineItemsLink.getHref())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(jsonBody.getBytes()))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getHeader("Location");

    jsonBody = new String(Files.readAllBytes(Paths.get("src/test/resources/new-lineitem-json.txt")));
    jsonBody = jsonBody.replaceAll("%LINEITEM_HREF%", lineItemLoc);

    mockMvc
        .perform(post(orderLineItemsLink.getHref())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(jsonBody.getBytes()))
        .andExpect(status().isCreated());

    assertThat("Order now contains additional LineItem",
               discover(orderLineItemsLink, ORDER_LINEITEM_REL),
               Matchers.<Link>iterableWithSize(2));
  }

  private Link customersLink() throws Exception {
    return discoverRootLink(CUSTOMERS_REL);
  }

  private Link productsLink() throws Exception {
    return discoverRootLink(PRODUCTS_REL);
  }

  private Link ordersLink() throws Exception {
    return discoverRootLink(ORDERS_REL);
  }

  private Link lineItemsLink() throws Exception {
    return discoverRootLink(LINEITEMS_REL);
  }

  private List<Link> linksToCustomers() throws Exception {
    return discover(customersLink(), CUSTOMER_REL);
  }

  private List<Link> linksToProducts() throws Exception {
    return discover(productsLink(), PRODUCT_REL);
  }

  private List<Link> linksToOrders() throws Exception {
    return discover(ordersLink(), ORDER_REL);
  }

}
