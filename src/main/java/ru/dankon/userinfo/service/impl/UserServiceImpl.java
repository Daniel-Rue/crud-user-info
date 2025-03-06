package ru.dankon.userinfo.service.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.dankon.userinfo.domain.UserRepository;
import ru.dankon.userinfo.dto.UserContactInfo;
import ru.dankon.userinfo.exceptions.UserAlreadyExistsException;
import ru.dankon.userinfo.exceptions.UserNotFoundException;
import ru.dankon.userinfo.model.User;
import ru.dankon.userinfo.service.UserService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Value("${file.storage.directory}")
    private String storageDirectory;

    @PostConstruct
    public void init() throws IOException {
        Path uploadPath = Paths.get(storageDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException();
        } else if (userRepository.existsByPhoneNumber(user.getPhoneNumber())) {
            throw new UserAlreadyExistsException();
        }
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public UserContactInfo getUserContactInfoById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        return UserContactInfo.builder()
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .patronymic(user.getPatronymic())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    @Override
    public User updateUser(Long id, User user) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        user.setId(id);
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public User uploadPhoto(Long id, MultipartFile file) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (user.getPhotoPath() != null) {
            deletePhotoFile(user.getPhotoPath());
        }

        String relativePath = saveFile(file);
        user.setPhotoPath(relativePath);
        return userRepository.save(user);
    }

    @Override
    public String getPhotoPath(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return user.getPhotoPath();
    }

    @Override
    public void deletePhoto(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        deletePhotoFile(user.getPhotoPath());
        user.setPhotoPath(null);
        userRepository.save(user);
    }

    private String saveFile(MultipartFile file) {
        try {
            String extension = getFileExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + extension;
            Path filePath = Paths.get(storageDirectory).resolve(filename);

            Files.copy(file.getInputStream(), filePath);

            return Paths.get(storageDirectory).getFileName() + "/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

    private void deletePhotoFile(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) return;
        try {
            Path filePath = Paths.get(storageDirectory).resolve(relativePath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }
}