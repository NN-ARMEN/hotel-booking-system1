package com.hotel.service;

import com.hotel.dto.AuthDTO;
import com.hotel.exception.PasswordValidationException;
import com.hotel.model.ERole;
import com.hotel.model.Role;
import com.hotel.model.User;
import com.hotel.repository.RoleRepository;
import com.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$");

    @Transactional
    public User registerUser(AuthDTO.RegisterRequest registerRequest) {
        // Проверяем уникальность username и email
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Проверяем надежность пароля
        validatePassword(registerRequest.getPassword());

        // Создаем нового пользователя
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword())
        );

        // Назначаем роли
        Set<Role> roles = new HashSet<>();

        if (registerRequest.getRole() == null) {
            Role guestRole = roleRepository.findByName(ERole.ROLE_GUEST)
                    .orElseThrow(() -> new RuntimeException("Role not found: ROLE_GUEST"));
            roles.add(guestRole);
        } else {
            switch (registerRequest.getRole().toUpperCase()) {
                case "ADMIN":
                    Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                            .orElseThrow(() -> new RuntimeException("Role not found: ROLE_ADMIN"));
                    roles.add(adminRole);
                    break;
                case "MANAGER":
                    Role managerRole = roleRepository.findByName(ERole.ROLE_MANAGER)
                            .orElseThrow(() -> new RuntimeException("Role not found: ROLE_MANAGER"));
                    roles.add(managerRole);
                    break;
                case "RECEPTIONIST":
                    Role receptionistRole = roleRepository.findByName(ERole.ROLE_RECEPTIONIST)
                            .orElseThrow(() -> new RuntimeException("Role not found: ROLE_RECEPTIONIST"));
                    roles.add(receptionistRole);
                    break;
                default:
                    Role guestRole = roleRepository.findByName(ERole.ROLE_GUEST)
                            .orElseThrow(() -> new RuntimeException("Role not found: ROLE_GUEST"));
                    roles.add(guestRole);
            }
        }

        user.setRoles(roles);

        return userRepository.save(user);
    }

    private void validatePassword(String password) {
        if (password.length() < 8) {
            throw new PasswordValidationException("Password must be at least 8 characters long");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new PasswordValidationException(
                    "Password must contain at least one digit, one lowercase letter, " +
                            "one uppercase letter, one special character (@#$%^&+=!) and no whitespace"
            );
        }
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}