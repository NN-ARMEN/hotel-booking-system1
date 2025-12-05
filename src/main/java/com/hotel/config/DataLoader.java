package com.hotel.config;

import com.hotel.model.ERole;
import com.hotel.model.Role;
import com.hotel.model.User;
import com.hotel.repository.RoleRepository;
import com.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Создаем роли, если их нет
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(ERole.ROLE_GUEST));
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
            roleRepository.save(new Role(ERole.ROLE_MANAGER));
            roleRepository.save(new Role(ERole.ROLE_RECEPTIONIST));
            System.out.println("✅ Roles created successfully!");
        }

        // Создаем администратора по умолчанию, если его нет
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@hotel.com");
            admin.setPassword(passwordEncoder.encode("Admin@123"));
            admin.setEnabled(true);

            Set<Role> roles = new HashSet<>();
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            roles.add(adminRole);

            admin.setRoles(roles);
            userRepository.save(admin);
            System.out.println("✅ Default admin user created:");
            System.out.println("   Username: admin");
            System.out.println("   Password: Admin@123");
            System.out.println("   Email: admin@hotel.com");
        }

        // Создаем тестового гостя, если его нет
        if (!userRepository.existsByUsername("guest")) {
            User guest = new User();
            guest.setUsername("guest");
            guest.setEmail("guest@hotel.com");
            guest.setPassword(passwordEncoder.encode("Guest@123"));
            guest.setEnabled(true);

            Set<Role> roles = new HashSet<>();
            Role guestRole = roleRepository.findByName(ERole.ROLE_GUEST)
                    .orElseThrow(() -> new RuntimeException("Guest role not found"));
            roles.add(guestRole);

            guest.setRoles(roles);
            userRepository.save(guest);
            System.out.println("✅ Default guest user created:");
            System.out.println("   Username: guest");
            System.out.println("   Password: Guest@123");
            System.out.println("   Email: guest@hotel.com");
        }
    }
}