package org.springframework.data.rest.tck.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.tck.jpa.domain.LineItem;

/**
 * @author Jon Brisbin
 */
public interface LineItemRepository extends CrudRepository<LineItem, Long> {
}
