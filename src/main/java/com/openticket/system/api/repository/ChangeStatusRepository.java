package com.openticket.system.api.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.openticket.system.api.security.entity.ChangeStatus;

public interface ChangeStatusRepository extends MongoRepository<ChangeStatus, String> {
    Iterable<ChangeStatus> findByTicketIdOrderByDateChangeStatusDesc(String ticketId);
}
