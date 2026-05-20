package com.nhan.to_do_api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import javax.management.relation.Role;
import java.util.Date;
import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    String id;
    @Size(min = 6 , message = "UserName need to have move than 6 digits ")
    String userName;
    String password;
    @Email
    String email;
    String lastName;
    String firstName;
    Date dob;
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    Set<Role> roles;

}