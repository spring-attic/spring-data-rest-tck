package org.springframework.data.rest.tck.jpa.domain;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author Jon Brisbin
 */
public class CustomerValidator implements Validator {

	@Override public boolean supports(Class<?> clazz) {
		return Customer.class.isAssignableFrom(clazz);
	}

	@Override public void validate(Object target, Errors errors) {
		ValidationUtils.rejectIfEmpty(errors, "firstname", "not.blank");
		ValidationUtils.rejectIfEmpty(errors, "lastname", "not.blank");
	}

}
