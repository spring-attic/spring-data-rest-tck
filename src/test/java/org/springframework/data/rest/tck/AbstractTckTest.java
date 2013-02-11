package org.springframework.data.rest.tck;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import com.jayway.jsonpath.JsonPath;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.rest.webmvc.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.core.DefaultLinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * @author Jon Brisbin
 */
public abstract class AbstractTckTest {

  protected static final MediaType COMPACT_JSON = MediaType.parseMediaType("application/x-spring-data-compact+json");
  protected static final MediaType VERBOSE_JSON = MediaType.parseMediaType("application/x-spring-data-verbose+json");
  protected static AnnotationConfigApplicationContext    rootCtx;
  protected static AnnotationConfigWebApplicationContext webAppCtx;
  protected static MockMvc                               mockMvc;
  protected static LinkDiscoverer                        links;
  static           OpenEntityManagerInViewFilter         osivFilter;

  @BeforeClass
  public static void setupContext() {
    MockServletContext servletContext = new MockServletContext();

    osivFilter = new OpenEntityManagerInViewFilter();
    osivFilter.setServletContext(servletContext);

    rootCtx = new AnnotationConfigApplicationContext(TckConfig.class);

    webAppCtx = new AnnotationConfigWebApplicationContext();
    webAppCtx.register(RepositoryRestMvcConfiguration.class);
    webAppCtx.setParent(rootCtx);
    webAppCtx.setServletContext(servletContext);
    servletContext.setAttribute(
        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppCtx
    );

    mockMvc = webAppContextSetup(webAppCtx).build();

    links = new DefaultLinkDiscoverer();
  }

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

  @Before
  public void setup() {
    loadData();
  }

  @After
  public void tearDown() {
    deleteData();
  }

  protected abstract void loadData();

  protected abstract void deleteData();

}
