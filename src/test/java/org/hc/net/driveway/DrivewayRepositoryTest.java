package org.hc.net.driveway;

import org.hc.net.JpaConfig;
import org.hc.net.TestcontainersConfiguration;
import org.hc.net.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaConfig.class})
class DrivewayRepositoryTest {

    @Autowired
    private DrivewayRepository drivewayRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User persistOwner(String email) {
        User owner = new User();
        owner.setEmail(email);
        owner.setPasswordHash("hash");
        owner.setFullName("Test Owner");
        return entityManager.persist(owner);
    }

    private Driveway buildDriveway(User owner) {
        Driveway driveway = new Driveway();
        driveway.setOwner(owner);
        driveway.setAddress("10 Test Street, London");
        driveway.setPricePerHour(new BigDecimal("5.00"));
        driveway.setLatitude(51.5074);
        driveway.setLongitude(-0.1278);
        return driveway;
    }

    @Test
    void savesAndFindsDriveway() {
        User owner = persistOwner("owner@example.com");
        Driveway driveway = buildDriveway(owner);
        driveway.setAddress("5 High Street, London");

        drivewayRepository.save(driveway);
        entityManager.flush();
        entityManager.clear();

        var found = drivewayRepository.findById(driveway.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getAddress()).isEqualTo("5 High Street, London");
        assertThat(found.get().getOwner().getId()).isEqualTo(owner.getId());
    }

    @Test
    void findsByOwner() {
        User owner = persistOwner("multi@example.com");
        User other = persistOwner("other@example.com");

        drivewayRepository.save(buildDriveway(owner));
        drivewayRepository.save(buildDriveway(owner));
        drivewayRepository.save(buildDriveway(other));
        entityManager.flush();
        entityManager.clear();

        var results = drivewayRepository.findByOwnerId(owner.getId());

        assertThat(results).hasSize(2);
    }

    @Test
    void auditFieldsArePopulated() {
        User owner = persistOwner("audit-driveway@example.com");
        Driveway driveway = buildDriveway(owner);

        drivewayRepository.save(driveway);
        entityManager.flush();
        entityManager.clear();

        Driveway reloaded = drivewayRepository.findById(driveway.getId()).orElseThrow();

        assertThat(reloaded.getCreatedDate()).isNotNull();
        assertThat(reloaded.getLastModifiedDate()).isNotNull();
    }
}
