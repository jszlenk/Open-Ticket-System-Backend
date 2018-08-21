package com.openticket.system.api.service.impl;

import com.openticket.system.api.repository.ChangeStatusRepository;
import com.openticket.system.api.repository.TicketRepository;
import com.openticket.system.api.security.entity.ChangeStatus;
import com.openticket.system.api.security.entity.Ticket;
import com.openticket.system.api.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ChangeStatusRepository changeStatusRepository;

    @Autowired
    public TicketServiceImpl(ChangeStatusRepository changeStatusRepository, TicketRepository ticketRepository) {
        this.changeStatusRepository = changeStatusRepository;
        this.ticketRepository = ticketRepository;
    }

    public Ticket createOrUpdate(Ticket ticket) {
        return this.ticketRepository.save(ticket);
    }

    public Optional<Ticket> findById(String id) {
        return this.ticketRepository.findById(id);
    }

    public void delete(String id) {
        this.ticketRepository.deleteById(id);
    }

    public Page<Ticket> listTicket(int page, int count) {
        Pageable pages = PageRequest.of(page, count);
        return this.ticketRepository.findAll(pages);
    }

    public Iterable<Ticket> findAll() {
        return this.ticketRepository.findAll();
    }

    public Page<Ticket> findByCurrentUser(int page, int count, String userId) {
        Pageable pages = PageRequest.of(page, count);
        return this.ticketRepository.findByUserIdOrderByDateDesc(pages, userId);
    }

    public void createChangeStatus(ChangeStatus changeStatus) {
        this.changeStatusRepository.save(changeStatus);
    }

    public Iterable<ChangeStatus> listChangeStatus(String ticketId) {
        return this.changeStatusRepository.findByTicketIdOrderByDateChangeStatusDesc(ticketId);
    }

    public Page<Ticket> findByParameters(int page, int count, String title, String status, String priority) {
        Pageable pages = PageRequest.of(page, count);
        return this.ticketRepository.findByTitleIgnoreCaseContainingOrderByDateDesc(title, status, priority, pages);
    }

    public Page<Ticket> findByParametersAndCurrentUser(int page, int count, String title, String status, String
            priority, String userId) {
        Pageable pages = PageRequest.of(page, count);
        return this.ticketRepository.findByTitleIgnoreCaseContainingAndUserIdOrderByDateDesc(title, status, priority,
                userId, pages);
    }

    public Page<Ticket> findByNumber(int page, int count, Integer number) {
        Pageable pages = PageRequest.of(page, count);
        return this.ticketRepository.findByNumber(number, pages);
    }

    public Page<Ticket> findByParametersAndAssignedUser(int page, int count, String title, String status, String
            priority, String assignedUserId) {
        Pageable pages = PageRequest.of(page, count);
        return this.ticketRepository.findByTitleIgnoreCaseContainingAndAssignedUserIdOrderByDateDesc(
                title, status, priority, assignedUserId, pages);
    }
}