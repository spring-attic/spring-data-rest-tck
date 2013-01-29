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

  /**
   * Test whether {@link org.springframework.data.repository.CrudRepository}s exported are discoverable.
   *
   * @throws Exception
   */
  @Test
  public void testDiscoverability() throws Exception {
    Link customer = findCustomerLink();

    assertNotNull("Exposes a Link to manage Customers",
                  customer);
    assertThat("Customer Link looks correct",
               customer.getHref(),
               allOf(startsWith("http://"),
                     endsWith("/customer")));
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
      MockHttpServletResponse response = findEntity(href);
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

  @Test
  public void testExposesAccessToLinkedEntities() throws Exception {
    for(Link l : findCustomerLinks()) {
      MockHttpServletResponse response = findEntity(l.getHref());
      String jsonBody = response.getContentAsString();

      List<Link> addresses = links.findLinksWithRel(ADDRESSES_REL, jsonBody);

      assertThat("Has linked Addresses",
                 addresses,
                 notNullValue());
      assertThat("Addresses aren't empty",
                 addresses.size(),
                 greaterThan(0));

      Link addressLink = addresses.get(0);
      MockHttpServletResponse addrResponse = findEntity(addressLink.getHref());
      String addrJsonBody = addrResponse.getContentAsString();

      assertThat("Has valid street",
                 String.format("%s", JsonPath.read(addrJsonBody, "$content[0].street")),
                 is("123 W 1st Street"));
    }
  }

  private MockHttpServletResponse getRoot() throws Exception {
    return mockMvc
        .perform(get("/").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();
  }

  private Link findCustomerLink() throws Exception {
    MockHttpServletResponse response = getRoot();
    return links.findLinkWithRel(CUSTOMERS_REL, response.getContentAsString());
  }

  private List<Link> findCustomerLinks() throws Exception {
    Link customer = findCustomerLink();

    MockHttpServletResponse response = mockMvc
        .perform(get(customer.getHref())
                     .accept(COMPACT_TYPE))
        .andExpect(status().isOk())
        .andReturn().getResponse();

    return links.findLinksWithRel(CUSTOMER_REL, response.getContentAsString());
  }

  private List<String> loadCustomers() throws Exception {
    Link customer = findCustomerLink();
    List<String> created = new ArrayList<>();
    for(String line : Files.readAllLines(Paths.get("src/test/resources/customer-json.txt"),
                                         Charset.defaultCharset())) {
      String loc = mockMvc
          .perform(post(customer.getHref())
                       .contentType(MediaType.APPLICATION_JSON)
                       .content(line))
          .andExpect(status().isCreated())
          .andReturn().getResponse().getHeader("Location");
      created.add(loc);
    }

    return created;
  }

  private MockHttpServletResponse findEntity(String href) throws Exception {
    return mockMvc
        .perform(get(href).accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();
  }

}
