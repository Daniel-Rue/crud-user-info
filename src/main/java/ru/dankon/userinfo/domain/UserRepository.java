package ru.dankon.userinfo.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.dankon.userinfo.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);

}
