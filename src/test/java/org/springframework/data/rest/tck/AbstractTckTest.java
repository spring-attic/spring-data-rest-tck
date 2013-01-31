package org.springframework.data.rest.tck;

import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.rest.webmvc.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.LinkDiscoverer;
import org.springframework.hateoas.core.DefaultLinkDiscoverer;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

/**
 * @author Jon Brisbin
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
    TckConfig.class,
    RepositoryRestMvcConfiguration.class
})
public abstract class AbstractTckTest {

  protected static final MediaType COMPACT_JSON = MediaType.parseMediaType("application/x-spring-data-compact+json");
  protected static final MediaType VERBOSE_JSON = MediaType.parseMediaType("application/x-spring-data-verbose+json");
  protected static AnnotationConfigWebApplicationContext webAppCtx;
  protected static MockMvc                               mockMvc;
  protected static LinkDiscoverer                        links;
  static           OpenEntityManagerInViewFilter         osivFilter;

  @BeforeClass
  public static void setupContext() {
    MockServletContext servletContext = new MockServletContext();

    osivFilter = new OpenEntityManagerInViewFilter();
    osivFilter.setServletContext(servletContext);

    webAppCtx = new AnnotationConfigWebApplicationContext();
    webAppCtx.register(RepositoryRestMvcConfiguration.class);
    webAppCtx.setParent(new AnnotationConfigApplicationContext(TckConfig.class));
    webAppCtx.setServletContext(servletContext);
    servletContext.setAttribute(
        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, webAppCtx
    );

    mockMvc = webAppContextSetup(webAppCtx).build();

    links = new DefaultLinkDiscoverer();
  }

}
