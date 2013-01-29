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
 * @author Jon Brisbin
 */
public class JpaTckTests extends AbstractTckTest {

  @Test
  public void repositoriesAreDiscoverable() throws Exception {
    Link customer = findCustomerLink();

    assertNotNull("Exposes a Link to manage Customers",
                  customer);
    assertThat("Customer Link looks correct",
               customer.getHref(),
               allOf(startsWith("http://"),
                     endsWith("/customer")));
  }

  @Test
  public void repositoriesExposeCreate() throws Exception {
    for(String href : loadCustomers()) {
      MockHttpServletResponse response = findEntity(href);
      String jsonBody = response.getContentAsString();

      assertThat("Customer is a Doe",
                 JsonPath.read(jsonBody, "lastname").toString(),
                 is("Doe"));
      assertThat("Entity contains self Link",
                 links.findLinkWithRel("self", jsonBody),
                 notNullValue());
      assertThat("Entity maintains addresses as Links",
                 links.findLinkWithRel("customer.Customer.addresses", jsonBody),
                 notNullValue());
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
    return links.findLinkWithRel("customer", response.getContentAsString());
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
