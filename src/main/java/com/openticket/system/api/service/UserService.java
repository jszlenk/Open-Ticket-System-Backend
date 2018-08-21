package com.openticket.system.api.service;

import com.openticket.system.api.security.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public interface UserService {

    User findByEmail(String email);

    User createOrUpdate(User user);

    Optional<User> findById(String id);

    void delete(String id);

    Page<User> findAll(int page, int count);
}
