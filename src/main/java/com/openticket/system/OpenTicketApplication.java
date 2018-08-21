package com.openticket.system;

import com.openticket.system.api.repository.UserRepository;
import com.openticket.system.api.security.entity.User;
import com.openticket.system.api.security.enums.ProfileEnum;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class OpenTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenTicketApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> initUsers(userRepository, passwordEncoder);
    }

    private void initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        User admin = new User();
        admin.setEmail("admin@openticketsystem.com");
        admin.setPassword(passwordEncoder.encode("admin"));
        admin.setProfile(ProfileEnum.ROLE_ADMIN);

        User find = userRepository.findByEmail("admin@openticketsystem.com");
        if (find == null) {
            userRepository.save(admin);
        }
    }
}
