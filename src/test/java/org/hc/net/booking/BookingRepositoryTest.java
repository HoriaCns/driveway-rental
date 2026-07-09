package org.hc.net.booking;

import org.hc.net.JpaConfig;
import org.hc.net.TestcontainersConfiguration;
import org.hc.net.driveway.Driveway;
import org.hc.net.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaConfig.class})
class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User renter;
    private Driveway driveway;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setPasswordHash("hash");
        owner.setFullName("Owner");
        entityManager.persist(owner);

        renter = new User();
        renter.setEmail("renter@example.com");
        renter.setPasswordHash("hash");
        renter.setFullName("Renter");
        entityManager.persist(renter);

        driveway = new Driveway();
        driveway.setOwner(owner);
        driveway.setAddress("10 Test Road, London");
        driveway.setPricePerHour(new BigDecimal("4.50"));
        driveway.setLatitude(51.5074);
        driveway.setLongitude(-0.1278);
        entityManager.persist(driveway);

        entityManager.flush();
    }

    private Booking buildBooking(Instant start, Instant end) {
        Booking booking = new Booking();
        booking.setRenter(renter);
        booking.setDriveway(driveway);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setStatus(BookingStatus.PENDING);
        booking.setTotalPrice(new BigDecimal("9.00"));
        return booking;
    }

    @Test
    void savesBookingWithPendingStatus() {
        Instant start = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Booking booking = buildBooking(start, start.plus(2, ChronoUnit.HOURS));

        bookingRepository.save(booking);
        entityManager.flush();
        entityManager.clear();

        Booking reloaded = bookingRepository.findById(booking.getId()).orElseThrow();

        assertThat(reloaded.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(reloaded.getRenter().getId()).isEqualTo(renter.getId());
    }

    @Test
    void findsByRenter() {
        Instant base = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        bookingRepository.save(buildBooking(base, base.plus(1, ChronoUnit.HOURS)));
        bookingRepository.save(buildBooking(base.plus(2, ChronoUnit.HOURS), base.plus(3, ChronoUnit.HOURS)));

        User other = new User();
        other.setEmail("other@example.com");
        other.setPasswordHash("hash");
        other.setFullName("Other");
        entityManager.persist(other);

        Booking otherBooking = buildBooking(base.plus(4, ChronoUnit.HOURS), base.plus(5, ChronoUnit.HOURS));
        otherBooking.setRenter(other);
        bookingRepository.save(otherBooking);
        entityManager.flush();
        entityManager.clear();

        var results = bookingRepository.findByRenterId(renter.getId());

        assertThat(results).hasSize(2);
    }

    @Test
    void findsByStatus() {
        Instant base = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        Booking pending = buildBooking(base, base.plus(1, ChronoUnit.HOURS));
        pending.setStatus(BookingStatus.PENDING);

        Booking confirmed = buildBooking(base.plus(2, ChronoUnit.HOURS), base.plus(3, ChronoUnit.HOURS));
        confirmed.setStatus(BookingStatus.CONFIRMED);

        bookingRepository.save(pending);
        bookingRepository.save(confirmed);
        entityManager.flush();
        entityManager.clear();

        assertThat(bookingRepository.findByStatus(BookingStatus.PENDING)).hasSize(1);
        assertThat(bookingRepository.findByStatus(BookingStatus.CONFIRMED)).hasSize(1);
    }

    @Test
    void detectsOverlappingBookings() {
        Instant base = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant start = base.plus(10, ChronoUnit.HOURS);
        Instant end = start.plus(2, ChronoUnit.HOURS);

        Booking existing = buildBooking(start, end);
        existing.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(existing);
        entityManager.flush();
        entityManager.clear();

        // overlaps: starts before existing ends, ends after existing starts
        var overlapping = bookingRepository.findOverlappingBookings(
                driveway.getId(),
                start.plus(30, ChronoUnit.MINUTES),
                end.plus(30, ChronoUnit.MINUTES)
        );
        assertThat(overlapping).hasSize(1);

        // no overlap: entirely after
        var noOverlap = bookingRepository.findOverlappingBookings(
                driveway.getId(),
                end.plus(1, ChronoUnit.HOURS),
                end.plus(2, ChronoUnit.HOURS)
        );
        assertThat(noOverlap).isEmpty();
    }
}
