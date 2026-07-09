package org.hc.net.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RepositoryRestResource(collectionResourceRel = "bookings", path = "bookings")
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    List<Booking> findByRenterId(UUID renterId);

    List<Booking> findByDrivewayId(UUID drivewayId);

    List<Booking> findByStatus(BookingStatus status);

    @Query("""
            SELECT b FROM Booking b
            WHERE b.driveway.id = :drivewayId
              AND b.startTime < :endTime
              AND b.endTime   > :startTime
            """)
    List<Booking> findOverlappingBookings(
            @Param("drivewayId") UUID drivewayId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );
}
