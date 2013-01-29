package org.springframework.data.rest.tck;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.data.rest.webmvc.RepositoryRestMvcConfiguration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author Jon Brisbin
 */
public class TckInitializer implements WebApplicationInitializer {

  @Override public void onStartup(ServletContext servletContext) throws ServletException {
    AnnotationConfigWebApplicationContext rootCtx = new AnnotationConfigWebApplicationContext();
    rootCtx.register(TckConfig.class);

    servletContext.addListener(new ContextLoaderListener(rootCtx));

    AnnotationConfigWebApplicationContext webCtx = new AnnotationConfigWebApplicationContext();
    webCtx.register(RepositoryRestMvcConfiguration.class);

    DispatcherServlet dispatcher = new DispatcherServlet(webCtx);

    ServletRegistration.Dynamic reg = servletContext.addServlet("dispatcher", dispatcher);
    reg.setLoadOnStartup(1);
    reg.addMapping("/");
  }

}
