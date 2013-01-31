package org.springframework.data.rest.tck.jpa;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.jayway.jsonpath.JsonPath;
import org.junit.Test;
import org.springframework.data.rest.tck.AbstractTckTest;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Tests that check the REST API of JPA entities that are exported through Spring Data REST.
 *
 * @author Jon Brisbin
 */
public class JpaTckTests extends AbstractTckTest {

  static final String SELF_REL      = "self";
  static final String CUSTOMERS_REL = "customer";
  static final String CUSTOMER_REL  = "customer.Customer";
  static final String ADDRESSES_REL = "customer.Customer.addresses";
  static final String PRODUCTS_REL  = "product";
  static final String PRODUCT_REL   = "product.Product";
  static final String ORDERS_REL    = "order";
  static final String ORDER_REL     = "order.Order";

  /**
   * Test whether {@link org.springframework.data.repository.CrudRepository}s exported are discoverable.
   *
   * @throws Exception
   */
  @Test
  public void testDiscoverability() throws Exception {
    Link customer = findCustomersLink();

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
    Link customer = findCustomersLink();
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
    Link customer = findCustomersLink();
    MockHttpServletResponse response = requestCompact(customer.getHref());
    String jsonBody = response.getContentAsString();

    assertThat("Entity is referenced as a Link",
               JsonPath.read(jsonBody, "$links[0].rel").toString(),
               is("customer.Customer"));
  }

  /**
   * Test whether the {@link org.springframework.data.rest.tck.jpa.repository.CustomerRepository} exposes a CREATE
   * feature.
   *
   * @throws Exception
   */
  @Test
  public void testCreate() throws Exception {
    for(String href : loadCustomers()) {
      MockHttpServletResponse response = request(href);
      String jsonBody = response.getContentAsString();

      assertThat("Customer is a Doe",
                 JsonPath.read(jsonBody, "lastname").toString(),
                 is("Doe"));
      assertThat("Entity contains self Link",
                 links.findLinkWithRel(SELF_REL, jsonBody),
                 notNullValue());
      assertThat("Entity maintains addresses as Links",
                 links.findLinkWithRel(ADDRESSES_REL, jsonBody),
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
    for(Link l : findCustomersLinks()) {
      MockHttpServletResponse response = request(l.getHref());
      String jsonBody = response.getContentAsString();

      List<Link> addresses = links.findLinksWithRel(ADDRESSES_REL, jsonBody);

      assertThat("Has linked Addresses",
                 addresses,
                 notNullValue());
      assertThat("Addresses aren't empty",
                 addresses.size(),
                 greaterThan(0));

      Link addressLink = addresses.get(0);
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
    Link customer = findCustomersLinks().iterator().next();
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
    List<Link> customers = findCustomersLinks();
    assertThat("Customers exist to work with",
               customers.size(),
               greaterThan(0));

    Link customer = customers.get(customers.size() - 1);

    mockMvc
        .perform(delete(customer.getHref()))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get(customer.getHref()))
        .andExpect(status().isNotFound());
  }

  @Test
  public void testCreatesEntityWithLinkedProperties() throws Exception {
    List<Link> customers = findCustomersLinks();
    assertThat("Customers exist to work with",
               customers.size(),
               greaterThan(0));
    List<Link> products = findProductsLinks();
    assertThat("Products exist to work with",
               products.size(),
               greaterThan(0));

    Link customer = customers.get(0);
    Link addresses = follow(customer, "customer.Customer.addresses");
    String addressesBody = requestCompact(addresses.getHref()).getContentAsString();
    String addrHref = JsonPath.read(addressesBody, "$links[0].href");
    Link addrSelf = links.findLinkWithRel("self", request(addrHref).getContentAsString());

    Link product = products.get(0);

    String jsonBody = new String(Files.readAllBytes(Paths.get("src/test/resources/new-order-1.txt")));
    jsonBody = jsonBody.replaceAll("%CUSTOMER_HREF%", customer.getHref());
    jsonBody = jsonBody.replaceAll("%ADDR_HREF%", addrSelf.getHref());
    jsonBody = jsonBody.replaceAll("%PRODUCT_HREF%", product.getHref());

    Link orders = findOrdersLink();
    String loc = mockMvc
        .perform(post(orders.getHref())
                     .contentType(MediaType.APPLICATION_JSON)
                     .content(jsonBody))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getHeader("Location");
    assertNotNull("Location header is not null", loc);

    MockHttpServletResponse checkResp = request(loc);
    String checkJsonBody = checkResp.getContentAsString();

    Link lineItemsLink = follow(links.findLinkWithRel("order.Order.lineItems", checkJsonBody),
                                "order.Order.lineItems.LineItem");
    assertNotNull("LineItems links is not null", lineItemsLink);

    mockMvc
        .perform(get(lineItemsLink.getHref()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("price", is(100.0)));

    Link custLink = links.findLinkWithRel("order.Order.customer", checkJsonBody);
    assertNotNull("Customer link is not null", custLink);

    mockMvc
        .perform(get(custLink.getHref()).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("lastname", is("Doe")));
  }

  private Link findCustomersLink() throws Exception {
    MockHttpServletResponse response = request("/");
    return links.findLinkWithRel(CUSTOMERS_REL, response.getContentAsString());
  }

  private Link findProductsLink() throws Exception {
    MockHttpServletResponse response = request("/");
    return links.findLinkWithRel(PRODUCTS_REL, response.getContentAsString());
  }

  private Link findOrdersLink() throws Exception {
    MockHttpServletResponse response = request("/");
    return links.findLinkWithRel(ORDERS_REL, response.getContentAsString());
  }

  private List<Link> findCustomersLinks() throws Exception {
    Link customer = findCustomersLink();
    MockHttpServletResponse response = requestCompact(customer.getHref());
    return links.findLinksWithRel(CUSTOMER_REL, response.getContentAsString());
  }

  private List<Link> findProductsLinks() throws Exception {
    Link products = findProductsLink();
    MockHttpServletResponse response = requestCompact(products.getHref());
    return links.findLinksWithRel(PRODUCT_REL, response.getContentAsString());
  }

  private List<String> loadCustomers() throws Exception {
    Link customer = findCustomersLink();
    List<String> created = new ArrayList<>();
    for(String line : Files.readAllLines(Paths.get("src/test/resources/customer-json.txt"),
                                         Charset.defaultCharset())) {
      String loc = send(customer.getHref(), MediaType.APPLICATION_JSON, line.getBytes())
          .andExpect(status().isCreated())
          .andReturn().getResponse().getHeader("Location");

      created.add(loc);
    }

    return created;
  }

  private ResultActions send(String href, MediaType contentType, byte[] body) throws Exception {
    return mockMvc
        .perform(post(href)
                     .contentType(contentType)
                     .content(body));
  }

  private MockHttpServletResponse request(String href, MediaType contentType) throws Exception {
    return mockMvc
        .perform(get(href).accept(contentType))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();
  }

  private MockHttpServletResponse request(String href) throws Exception {
    return request(href, MediaType.APPLICATION_JSON);
  }

  private MockHttpServletResponse requestCompact(String href) throws Exception {
    return request(href, COMPACT_JSON);
  }

  private Link follow(Link link, String nextRel) throws Exception {
    MockHttpServletResponse response = requestCompact(link.getHref());
    String jsonBody = response.getContentAsString();
    return links.findLinkWithRel(nextRel, jsonBody);
  }

}
