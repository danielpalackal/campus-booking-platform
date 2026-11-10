package com.xaviers.booking.service;

import com.xaviers.booking.model.Role;
import com.xaviers.booking.model.User;
import com.xaviers.booking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAdminServiceTest {
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;

    @Test
    void addRejectsNonXaviersEmail() {
        var service = new UserAdminService(userRepository, passwordEncoder);

        assertThatThrownBy(() -> service.add("person@example.com", "Person", "STUDENT", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("xaviers.edu.in");
    }

    @Test
    void addRejectsSuperadminCreation() {
        var service = new UserAdminService(userRepository, passwordEncoder);

        assertThatThrownBy(() -> service.add("person@xaviers.edu.in", "Person", "SUPERADMIN", "password123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SUPERADMIN");
    }

    @Test
    void addCreatesStudentWithEncodedPassword() {
        when(userRepository.findByEmail("person@xaviers.edu.in")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = new UserAdminService(userRepository, passwordEncoder)
                .add("person@xaviers.edu.in", "Person", "STUDENT", "password123");

        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(argThat(user -> user.getRole() == Role.STUDENT && user.getPassword().equals("encoded")));
        org.assertj.core.api.Assertions.assertThat(result.role()).isEqualTo("STUDENT");
    }
}
