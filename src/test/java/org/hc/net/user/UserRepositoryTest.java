package org.hc.net.user;

import org.hc.net.JpaConfig;
import org.hc.net.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaConfig.class})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void savesAndFindsUserByEmail() {
        User user = new User();
        user.setEmail("john@example.com");
        user.setPasswordHash("hashed_password");
        user.setFullName("John Doe");

        userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        var found = userRepository.findByEmail("john@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getFullName()).isEqualTo("John Doe");
    }

    @Test
    void emailIsUnique() {
        User user1 = new User();
        user1.setEmail("duplicate@example.com");
        user1.setPasswordHash("hash1");
        user1.setFullName("First User");

        User user2 = new User();
        user2.setEmail("duplicate@example.com");
        user2.setPasswordHash("hash2");
        user2.setFullName("Second User");

        userRepository.save(user1);

        assertThatThrownBy(() -> userRepository.saveAndFlush(user2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void auditFieldsArePopulated() {
        User user = new User();
        user.setEmail("audit@example.com");
        user.setPasswordHash("hash");
        user.setFullName("Audit User");

        User saved = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        User reloaded = userRepository.findById(saved.getId()).orElseThrow();

        assertThat(reloaded.getCreatedDate()).isNotNull();
        assertThat(reloaded.getLastModifiedDate()).isNotNull();
    }
}
