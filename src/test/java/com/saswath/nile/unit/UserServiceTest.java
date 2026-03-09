package com.saswath.nile.unit;

import com.saswath.nile.entity.User;
import com.saswath.nile.exception.ResourceNotFoundException;
import com.saswath.nile.repository.UserRepository;
import com.saswath.nile.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "alice@nile.com", "encoded_password");
        user.setId(1L);
        user.setRole("ROLE_USER");
    }

    @Test
    @DisplayName("createUser saves and returns the user")
    void createUser_savesAndReturnsUser() {
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertThat(result).isEqualTo(user);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("getUserById returns user when found")
    void getUserById_returnsUser_whenFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertThat(result.getEmail()).isEqualTo("alice@nile.com");
        assertThat(result.getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("getUserById throws ResourceNotFoundException when user does not exist")
    void getUserById_throws_whenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("getAllUsers returns all users from repository")
    void getAllUsers_returnsAll() {
        User u2 = new User("Bob", "bob@nile.com", "encoded");
        u2.setId(2L);
        when(userRepository.findAll()).thenReturn(List.of(user, u2));

        List<User> result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getEmail)
                .containsExactly("alice@nile.com", "bob@nile.com");
    }

    @Test
    @DisplayName("getAllUsers returns empty list when no users exist")
    void getAllUsers_returnsEmpty_whenNoneExist() {
        when(userRepository.findAll()).thenReturn(List.of());

        assertThat(userService.getAllUsers()).isEmpty();
    }

    @Test
    @DisplayName("deleteUser calls repository deleteById when user exists")
    void deleteUser_deletesSuccessfully_whenUserExists() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("deleteUser throws ResourceNotFoundException when user does not exist")
    void deleteUser_throws_whenUserNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.deleteUser(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository, never()).deleteById(any());
    }
}