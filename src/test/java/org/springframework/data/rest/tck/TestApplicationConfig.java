package org.springframework.data.rest.tck;

import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * @author Jon Brisbin
 */
@Configuration
public class TestApplicationConfig extends RepositoryRestMvcConfiguration {

	@Bean public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource msgsrc = new ReloadableResourceBundleMessageSource();
		msgsrc.setBasenames("/WEB-INF/classes/ValidationMessages");
		msgsrc.setFallbackToSystemLocale(false);
		return msgsrc;
	}

	@Bean public CookieLocaleResolver cookieLocaleResolver() {
		CookieLocaleResolver clr = new CookieLocaleResolver();
		clr.setDefaultLocale(Locale.ENGLISH);
		return clr;
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
