package org.springframework.data.rest.tck.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.repository.annotation.RestResource;
import org.springframework.data.rest.tck.jpa.domain.NotAccessible;

/**
 * @author Jon Brisbin
 */
@RestResource(exported = false)
public interface NotAccessibleRepository extends CrudRepository<NotAccessible, Long> {
}
