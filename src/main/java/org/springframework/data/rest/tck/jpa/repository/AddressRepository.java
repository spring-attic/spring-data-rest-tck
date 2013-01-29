package org.springframework.data.rest.tck.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.tck.jpa.domain.Address;

/**
 * @author Jon Brisbin
 */
public interface AddressRepository extends CrudRepository<Address, Long> {
}
