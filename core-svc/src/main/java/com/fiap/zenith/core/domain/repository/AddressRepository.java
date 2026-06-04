package com.fiap.zenith.core.domain.repository;

import com.fiap.zenith.core.domain.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Spring Data para {@link Address}.
 */
public interface AddressRepository extends JpaRepository<Address, UUID> {
}
