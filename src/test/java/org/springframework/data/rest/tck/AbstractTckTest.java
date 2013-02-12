package org.springframework.data.rest.tck;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import java.util.List;
import javax.servlet.ServletContext;

import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.core.DefaultLinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

/**
 * @author Jon Brisbin
 */
@Transactional
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration("src/main/webapp")
@ContextConfiguration(classes = {TckConfig.class, RepositoryRestMvcConfiguration.class})
public abstract class AbstractTckTest {

  protected static final MediaType      COMPACT_JSON = MediaType.parseMediaType("application/x-spring-data-compact+json");
  protected static final MediaType      VERBOSE_JSON = MediaType.parseMediaType("application/x-spring-data-verbose+json");
  protected final        LinkDiscoverer links        = new DefaultLinkDiscoverer();
  @Autowired
  protected WebApplicationContext webAppCtx;
  @Autowired
  protected ServletContext        servletContext;
  protected MockMvc               mockMvc;

  public static ResultMatcher linkWithRel(final String rel) {
    return new ResultMatcher() {
      @Override public void match(MvcResult result) throws Exception {
        String s = result.getResponse().getContentAsString();
        Object o = JsonPath.read(s, String.format("$links[?(@.rel == '%s')].href", rel));
        assertThat(String.format("Link with rel '%s' exists", rel),
                   o,
                   notNullValue());
      }
    };
  }

  protected List<Link> follow(Link parent, String followRel, String childRel) throws Exception {
    List<Link> links = discover(parent, followRel);
    if(null == links || links.isEmpty()) {
      return null;
    }

    String json = mockMvc
        .perform(get(links.get(0).getHref()).accept(COMPACT_JSON))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    return this.links.findLinksWithRel(childRel, json);
  }

  protected List<Link> discover(String rel) throws Exception {
    return discover(new Link("/"), rel);
  }

  protected List<Link> discover(Link root, String rel) throws Exception {
    String s = mockMvc
        .perform(get(root.getHref()).accept(COMPACT_JSON))
        .andExpect(status().isOk())
        .andExpect(linkWithRel(rel))
        .andReturn().getResponse().getContentAsString();
    return links.findLinksWithRel(rel, s);
  }

  protected Link discoverRootLink(String rel) throws Exception {
    List<Link> l = discover(rel);
    assertThat(String.format("Link rel='%s' is exposed", rel),
               l,
               Matchers.<Link>iterableWithSize(1));
    return l.get(0);
  }

  protected MockHttpServletResponse request(String href, MediaType contentType) throws Exception {
    return mockMvc
        .perform(get(href).accept(contentType))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andReturn().getResponse();
  }

  protected MockHttpServletResponse request(String href) throws Exception {
    return request(href, MediaType.APPLICATION_JSON);
  }

  protected MockHttpServletResponse requestCompact(String href) throws Exception {
    return request(href, COMPACT_JSON);
  }

  @Before
  public void setup() {
    OpenEntityManagerInViewFilter oemivf = new OpenEntityManagerInViewFilter();
    oemivf.setServletContext(servletContext);

    mockMvc = webAppContextSetup(webAppCtx)
        .addFilter(oemivf)
        .build();

    loadData();
  }

  @After
  public void tearDown() {
    deleteData();
  }

  protected abstract void loadData();

  protected abstract void deleteData();

}
