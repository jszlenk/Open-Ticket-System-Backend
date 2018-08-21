package com.openticket.system.api.repository;

import com.openticket.system.api.security.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TicketRepository extends MongoRepository<Ticket, String> {

    Page<Ticket> findByUserIdOrderByDateDesc(Pageable pages, String userId);

    Page<Ticket> findByTitleIgnoreCaseContainingOrderByDateDesc(String title, String status, String priority, Pageable pages);

    Page<Ticket> findByTitleIgnoreCaseContainingAndUserIdOrderByDateDesc(String title, String status, String priority, String user_id, Pageable pageable);

    Page<Ticket> findByNumber(Integer number, Pageable pages);

    Page<Ticket> findByTitleIgnoreCaseContainingAndAssignedUserIdOrderByDateDesc(String title, String status, String priority, String assignedUserId, Pageable pages);
}
