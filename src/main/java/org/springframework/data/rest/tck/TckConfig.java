package org.springframework.data.rest.tck;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Jon Brisbin
 */
@Configuration
@ComponentScan(basePackageClasses = TckConfig.class)
public class TckConfig {
}
