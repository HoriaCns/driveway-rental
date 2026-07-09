package org.hc.net.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "reviews", path = "reviews")
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByDrivewayId(UUID drivewayId);

    List<Review> findByReviewerId(UUID reviewerId);

    Optional<Review> findByBookingId(UUID bookingId);
}
