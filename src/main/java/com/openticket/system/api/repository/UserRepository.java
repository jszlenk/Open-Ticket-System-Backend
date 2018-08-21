package com.openticket.system.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.openticket.system.api.security.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
}
