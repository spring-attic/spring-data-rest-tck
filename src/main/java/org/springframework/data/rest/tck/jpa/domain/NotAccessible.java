package org.springframework.data.rest.tck.jpa.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Jon Brisbin
 */
@Entity
public class NotAccessible {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public NotAccessible() {
	}

	public Long getId() {
		return id;
	}

}
