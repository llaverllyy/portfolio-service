package com.example.portfolio.repository;

import com.example.portfolio.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmailReturnsUserWhenEmailExists() {
        var user = userRepository.save(buildUser("olga@example.com"));

        var actual = userRepository.findByEmail(user.getEmail());

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isEqualTo(user.getId());
        assertThat(actual.get().getFirstName()).isEqualTo(user.getFirstName());
        assertThat(actual.get().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void findByEmailReturnsEmptyWhenEmailDoesNotExist() {
        var actual = userRepository.findByEmail("missing@example.com");

        assertThat(actual).isEmpty();
    }

    private User buildUser(String email) {
        return User.builder()
                .firstName("Test")
                .lastName("User")
                .email(email)
                .bio("Repository test user")
                .build();
    }
}