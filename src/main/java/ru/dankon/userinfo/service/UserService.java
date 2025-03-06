package ru.dankon.userinfo.service;

import org.springframework.web.multipart.MultipartFile;
import ru.dankon.userinfo.dto.UserContactInfo;
import ru.dankon.userinfo.model.User;

import java.util.List;

public interface UserService {
    List<User> getAllUsers();
    User getUserById(Long id);
    UserContactInfo getUserContactInfoById(Long id);
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    User uploadPhoto(Long id, MultipartFile file);
    String getPhotoPath(Long id);
    void deletePhoto(Long id);

}
