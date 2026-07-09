package org.hc.net.review;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hc.net.booking.Booking;
import org.hc.net.driveway.Driveway;
import org.hc.net.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driveway_id", nullable = false)
    private Driveway driveway;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private Instant lastModifiedDate;
}
