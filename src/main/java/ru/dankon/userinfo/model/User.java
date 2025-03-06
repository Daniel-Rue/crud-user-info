package ru.dankon.userinfo.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private Long id;
    private String lastName;
    private String firstName;
    private String patronymic;
    private String birthDate;
    @Column(unique = true)
    private String email;
    @Column(unique = true)
    private String phoneNumber;
    private String photoPath;
}
