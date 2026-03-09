package com.saswath.nile.integration;

import com.saswath.nile.entity.User;
import com.saswath.nile.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository — Integration Tests")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User user = new User("Alice", "alice@nile.com", "encoded_password");
        user.setRole("ROLE_USER");
        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("findByEmail returns user when email matches")
    void findByEmail_returnsUser_whenEmailExists() {
        Optional<User> result = userRepository.findByEmail("alice@nile.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findByEmail returns empty when email does not exist")
    void findByEmail_returnsEmpty_whenEmailNotFound() {
        Optional<User> result = userRepository.findByEmail("unknown@nile.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByEmail is case-sensitive — uppercase email does not match lowercase record")
    void findByEmail_isCaseSensitive() {
        Optional<User> result = userRepository.findByEmail("ALICE@nile.com");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("existsByEmail returns true when email exists")
    void existsByEmail_returnsTrue_whenEmailExists() {
        assertThat(userRepository.existsByEmail("alice@nile.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail returns false when email does not exist")
    void existsByEmail_returnsFalse_whenEmailNotFound() {
        assertThat(userRepository.existsByEmail("ghost@nile.com")).isFalse();
    }

    @Test
    @DisplayName("saving a user with a duplicate email throws a constraint violation")
    void save_throws_onDuplicateEmail() {
        User duplicate = new User("Alice2", "alice@nile.com", "different_password");
        duplicate.setRole("ROLE_USER");

        assertThatException().isThrownBy(() -> {
            userRepository.saveAndFlush(duplicate);
        });
    }

    @Test
    @DisplayName("saved user can be retrieved by id")
    void save_andFindById_roundTrip() {
        Optional<User> found = userRepository.findById(savedUser.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("alice@nile.com");
    }

    @Test
    @DisplayName("deleted user can no longer be found")
    void delete_removesUser() {
        userRepository.deleteById(savedUser.getId());

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }
}