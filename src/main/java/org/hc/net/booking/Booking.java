package org.hc.net.booking;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hc.net.driveway.Driveway;
import org.hc.net.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driveway_id", nullable = false)
    private Driveway driveway;

    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private Instant lastModifiedDate;
}
