package org.springframework.data.rest.tck;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author Jon Brisbin
 */
public class TckInitializer implements WebApplicationInitializer {

	@Override public void onStartup(ServletContext servletContext) throws ServletException {
		AnnotationConfigWebApplicationContext rootCtx = new AnnotationConfigWebApplicationContext();
		rootCtx.register(TckConfig.class);

		servletContext.addListener(new ContextLoaderListener(rootCtx));

		AnnotationConfigWebApplicationContext webCtx = new AnnotationConfigWebApplicationContext();
		webCtx.register(WebConfig.class);

		DispatcherServlet dispatcher = new DispatcherServlet(webCtx);

		ServletRegistration.Dynamic reg = servletContext.addServlet("dispatcher", dispatcher);
		reg.setLoadOnStartup(1);
		reg.addMapping("/");
	}

	private static class WebConfig extends RepositoryRestMvcConfiguration {

		@Bean public MessageSource messageSource() {
			ReloadableResourceBundleMessageSource msgsrc = new ReloadableResourceBundleMessageSource();
			msgsrc.setBasenames("/ValidationMessages");
			msgsrc.setFallbackToSystemLocale(false);
			return msgsrc;
		}

		@Override public RequestMappingHandlerMapping repositoryExporterHandlerMapping() {
			RequestMappingHandlerMapping handlerMapping = super.repositoryExporterHandlerMapping();

			LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
			lci.setParamName("locale");
			handlerMapping.setInterceptors(new Object[]{
					lci
			});

			return handlerMapping;
		}

	}

}
