package ru.dankon.userinfo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.dankon.userinfo.domain.UserRepository;
import ru.dankon.userinfo.exceptions.UserAlreadyExistsException;
import ru.dankon.userinfo.exceptions.UserNotFoundException;
import ru.dankon.userinfo.model.User;
import ru.dankon.userinfo.service.impl.UserServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private final User testUser = User.builder()
            .id(1L)
            .email("test@test.com")
            .phoneNumber("+79998887766")
            .build();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "storageDirectory", "test-storage");
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<User> result = userService.getAllUsers();

        assertThat(result).containsExactly(testUser);
    }

    @Test
    void createUser_shouldSaveNewUser() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(testUser);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue()).isEqualTo(testUser);
        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void createUser_shouldThrowWhenEmailExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void createUser_shouldThrowWhenPhoneExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByPhoneNumber(anyString())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(testUser))
                .isInstanceOf(UserAlreadyExistsException.class);
    }

    @Test
    void getUserById_shouldReturnUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.getUserById(1L);

        assertThat(result).isEqualTo(testUser);
    }

    @Test
    void getUserById_shouldThrowWhenNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void updateUser_shouldUpdateExistingUser() {
        User updatedUser = testUser.toBuilder()
                .email("new@email.com")
                .build();

        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        User result = userService.updateUser(1L, updatedUser);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getId()).isEqualTo(1L);
        assertThat(userCaptor.getValue().getEmail()).isEqualTo("new@email.com");
        assertThat(result).isEqualTo(updatedUser);
    }

    @Test
    void updateUser_shouldThrowWhenUserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> userService.updateUser(999L, testUser))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    void deleteUser_shouldCallRepositoryDelete() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void uploadPhoto_shouldUpdatePhotoPath() throws IOException {
        User userWithPhoto = testUser.toBuilder().photoPath("old-path").build();
        InputStream inputStream = new ByteArrayInputStream(new byte[0]);

        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithPhoto));
        when(multipartFile.getOriginalFilename()).thenReturn("photo.jpg");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(userRepository.save(any(User.class))).thenReturn(userWithPhoto);

        userService.uploadPhoto(1L, multipartFile);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPhotoPath())
                .startsWith("test-storage")
                .endsWith(".jpg");
    }

    @Test
    void getPhotoPath_shouldReturnPathFromUser() {
        User userWithPhoto = testUser.toBuilder().photoPath("test-path").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithPhoto));

        String result = userService.getPhotoPath(1L);

        assertThat(result).isEqualTo("test-path");
    }

    @Test
    void deletePhoto_shouldRemovePhotoPath() {
        User userWithPhoto = testUser.toBuilder().photoPath("test-path").build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithPhoto));

        userService.deletePhoto(1L);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPhotoPath()).isNull();
    }

    @Test
    void deletePhoto_shouldHandleNullPhotoPath() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        userService.deletePhoto(1L);

        verify(userRepository).save(userCaptor.capture());
        assertThat(userCaptor.getValue().getPhotoPath()).isNull();
    }
}