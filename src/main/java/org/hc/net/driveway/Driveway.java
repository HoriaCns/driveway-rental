package org.hc.net.driveway;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hc.net.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "driveways")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class Driveway {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_per_hour", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerHour;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date", nullable = false)
    private Instant lastModifiedDate;
}
