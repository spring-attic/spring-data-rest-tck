package org.springframework.data.rest.tck;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.tck.jpa.domain.CustomerValidator;

/**
 * @author Jon Brisbin
 */
@Configuration
@ComponentScan(basePackageClasses = TckConfig.class)
public class TckConfig {

	@Bean public CustomerValidator beforeCreateCustomerValidator() {
		return new CustomerValidator();
	}

}
