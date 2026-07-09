package org.hc.net.review;

import org.hc.net.JpaConfig;
import org.hc.net.TestcontainersConfiguration;
import org.hc.net.booking.Booking;
import org.hc.net.booking.BookingStatus;
import org.hc.net.driveway.Driveway;
import org.hc.net.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaConfig.class})
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User reviewer;
    private Driveway driveway;

    @BeforeEach
    void setUp() {
        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setPasswordHash("hash");
        owner.setFullName("Owner");
        entityManager.persist(owner);

        reviewer = new User();
        reviewer.setEmail("reviewer@example.com");
        reviewer.setPasswordHash("hash");
        reviewer.setFullName("Reviewer");
        entityManager.persist(reviewer);

        driveway = new Driveway();
        driveway.setOwner(owner);
        driveway.setAddress("1 Review Lane, London");
        driveway.setPricePerHour(new BigDecimal("3.50"));
        driveway.setLatitude(51.5074);
        driveway.setLongitude(-0.1278);
        entityManager.persist(driveway);

        entityManager.flush();
    }

    private Booking persistBooking() {
        Instant start = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Booking booking = new Booking();
        booking.setRenter(reviewer);
        booking.setDriveway(driveway);
        booking.setStartTime(start);
        booking.setEndTime(start.plus(1, ChronoUnit.HOURS));
        booking.setStatus(BookingStatus.COMPLETED);
        booking.setTotalPrice(new BigDecimal("3.50"));
        return entityManager.persist(booking);
    }

    @Test
    void savesAndFindsReview() {
        Booking booking = persistBooking();
        entityManager.flush();

        Review review = new Review();
        review.setReviewer(reviewer);
        review.setDriveway(driveway);
        review.setBooking(booking);
        review.setRating(5);
        review.setComment("Great spot, very convenient!");

        reviewRepository.save(review);
        entityManager.flush();
        entityManager.clear();

        Review reloaded = reviewRepository.findById(review.getId()).orElseThrow();

        assertThat(reloaded.getRating()).isEqualTo(5);
        assertThat(reloaded.getComment()).isEqualTo("Great spot, very convenient!");
    }

    @Test
    void findsByDriveway() {
        Booking booking = persistBooking();
        entityManager.flush();

        Review review = new Review();
        review.setReviewer(reviewer);
        review.setDriveway(driveway);
        review.setBooking(booking);
        review.setRating(4);
        reviewRepository.save(review);
        entityManager.flush();
        entityManager.clear();

        assertThat(reviewRepository.findByDrivewayId(driveway.getId())).hasSize(1);
    }

    @Test
    void oneReviewPerBooking() {
        Booking booking = persistBooking();
        entityManager.flush();

        Review first = new Review();
        first.setReviewer(reviewer);
        first.setDriveway(driveway);
        first.setBooking(booking);
        first.setRating(3);
        reviewRepository.saveAndFlush(first);

        Review second = new Review();
        second.setReviewer(reviewer);
        second.setDriveway(driveway);
        second.setBooking(booking);
        second.setRating(5);

        assertThatThrownBy(() -> reviewRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
