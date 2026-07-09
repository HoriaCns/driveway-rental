package org.hc.net.driveway;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "driveways", path = "driveways")
public interface DrivewayRepository extends JpaRepository<Driveway, UUID> {

    List<Driveway> findByOwnerId(UUID ownerId);
}
