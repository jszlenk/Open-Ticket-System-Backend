package com.openticket.system.api.controller;

import com.openticket.system.api.dto.Summary;
import com.openticket.system.api.response.Response;
import com.openticket.system.api.security.entity.ChangeStatus;
import com.openticket.system.api.security.entity.Ticket;
import com.openticket.system.api.security.entity.User;
import com.openticket.system.api.security.enums.ProfileEnum;
import com.openticket.system.api.security.enums.StatusEnum;
import com.openticket.system.api.security.jwt.JwtTokenUtil;
import com.openticket.system.api.service.TicketService;
import com.openticket.system.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@RestController
@RequestMapping("/api/ticket")
@CrossOrigin(origins = "*")
public class TicketController {

    private final TicketService ticketService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    @Autowired
    public TicketController(TicketService ticketService, JwtTokenUtil jwtTokenUtil, UserService userService) {
        this.ticketService = ticketService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Response<Ticket>> create(HttpServletRequest request, @RequestBody Ticket ticket,
                                                   BindingResult result) {
        Response<Ticket> response = new Response<>();
        try {
            validateCreateTicket(ticket, result);
            if (result.hasErrors()) {
                result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(response);
            }
            ticket.setStatus(StatusEnum.getStatus("New"));
            ticket.setUser(userFromRequest(request));
            ticket.setDate(new Date());
            ticket.setNumber(generateNumber());
            Ticket ticketPersisted = ticketService.createOrUpdate(ticket);
            response.setData(ticketPersisted);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    private void validateCreateTicket(Ticket ticket, BindingResult result) {
        if (ticket.getTitle() == null) {
            result.addError(new ObjectError("Ticket", "Title no information"));
        }
    }

    private User userFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String email = jwtTokenUtil.getUsernameFromToken(token);
        return userService.findByEmail(email);
    }

    private Integer generateNumber() {
        Random random = new Random();
        return random.nextInt(9999);
    }

    @PutMapping()
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Response<Ticket>> update(HttpServletRequest request, @RequestBody Ticket ticket,
                                                   BindingResult result) {
        Response<Ticket> response = new Response<>();
        try {
            validateUpdateTicket(ticket, result);
            if (result.hasErrors()) {
                result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(response);
            }
            Optional<Ticket> ticketCurrentOptional = ticketService.findById(ticket.getId());
            Ticket ticketCurrent = ticketCurrentOptional.get();
            ticket.setStatus(ticketCurrent.getStatus());
            ticket.setUser(ticketCurrent.getUser());
            ticket.setDate(ticketCurrent.getDate());
            ticket.setNumber(ticketCurrent.getNumber());
            if (ticketCurrent.getAssignedUser() != null) {
                ticket.setAssignedUser(ticketCurrent.getAssignedUser());
            }
            Ticket ticketPersisted = ticketService.createOrUpdate(ticket);
            response.setData(ticketPersisted);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    private void validateUpdateTicket(Ticket ticket, BindingResult result) {
        if (ticket.getId() == null) {
            result.addError(new ObjectError("Ticket", "Id no information"));
            return;
        }
        if (ticket.getTitle() == null) {
            result.addError(new ObjectError("Ticket", "Title no information"));
        }
    }


    @GetMapping(value = "{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
    public ResponseEntity<Response<Ticket>> findById(@PathVariable("id") String id) {
        Response<Ticket> response = new Response<>();
        Optional<Ticket> ticketOptional = ticketService.findById(id);
        Ticket ticket = ticketOptional.get();
        if (ticket == null) {
            response.getErrors().add("Register not found id:" + id);
            return ResponseEntity.badRequest().body(response);
        }
        List<ChangeStatus> changes = new ArrayList<>();
        Iterable<ChangeStatus> changesCurrent = ticketService.listChangeStatus(ticket.getId());
        for (ChangeStatus changeStatus : changesCurrent) {
            changeStatus.setTicket(null);
            changes.add(changeStatus);
        }
        ticket.setChanges(changes);
        response.setData(ticket);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public ResponseEntity<Response<String>> delete(@PathVariable("id") String id) {
        Response<String> response = new Response<>();
        Optional<Ticket> ticketOptional = ticketService.findById(id);
        Ticket ticket = ticketOptional.get();
        if (ticket == null) {
            response.getErrors().add("Register not found id:" + id);
            return ResponseEntity.badRequest().body(response);
        }
        ticketService.delete(id);
        return ResponseEntity.ok(new Response<>());
    }


    @GetMapping(value = "{page}/{count}")
    @PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
    public ResponseEntity<Response<Page<Ticket>>> findAll(HttpServletRequest request, @PathVariable int page,
														  @PathVariable int count) {

        Response<Page<Ticket>> response = new Response<>();
        Page<Ticket> tickets = null;
        User userRequest = userFromRequest(request);
        if (userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
            tickets = ticketService.listTicket(page, count);
        } else if (userRequest.getProfile().equals(ProfileEnum.ROLE_CUSTOMER)) {
            tickets = ticketService.findByCurrentUser(page, count, userRequest.getId());
        }
        response.setData(tickets);
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "{page}/{count}/{number}/{title}/{status}/{priority}/{assigned}")
    @PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
    public ResponseEntity<Response<Page<Ticket>>> findByParams(HttpServletRequest request,
                                                               @PathVariable int page,
                                                               @PathVariable int count,
                                                               @PathVariable Integer number,
                                                               @PathVariable String title,
                                                               @PathVariable String status,
                                                               @PathVariable String priority,
                                                               @PathVariable boolean assigned) {

        title = title.equals("uninformed") ? "" : title;
        status = status.equals("uninformed") ? "" : status;
        priority = priority.equals("uninformed") ? "" : priority;

        Response<Page<Ticket>> response = new Response<>();
        Page<Ticket> tickets = null;
        if (number > 0) {
            tickets = ticketService.findByNumber(page, count, number);
        } else {
            User userRequest = userFromRequest(request);
            if (userRequest.getProfile().equals(ProfileEnum.ROLE_TECHNICIAN)) {
                if (assigned) {
                    tickets = ticketService.findByParametersAndAssignedUser(page, count, title, status, priority,
							userRequest.getId());
                } else {
                    tickets = ticketService.findByParameters(page, count, title, status, priority);
                }
            } else if (userRequest.getProfile().equals(ProfileEnum.ROLE_CUSTOMER)) {
                tickets = ticketService.findByParametersAndCurrentUser(page, count, title, status, priority,
						userRequest.getId());
            }
        }
        response.setData(tickets);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}/{status}")
    @PreAuthorize("hasAnyRole('CUSTOMER','TECHNICIAN')")
    public ResponseEntity<Response<Ticket>> changeStatus(
            @PathVariable("id") String id,
            @PathVariable("status") String status,
            HttpServletRequest request,
            @RequestBody Ticket ticket,
            BindingResult result) {

        Response<Ticket> response = new Response<>();
        try {
            validateChangeStatus(id, status, result);
            if (result.hasErrors()) {
                result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(response);
            }
            Optional<Ticket> ticketCurrentOptional = ticketService.findById(id);
            Ticket ticketCurrent = ticketCurrentOptional.get();
            ticketCurrent.setStatus(StatusEnum.getStatus(status));
            if (status.equals("Assigned")) {
                ticketCurrent.setAssignedUser(userFromRequest(request));
            }
            Ticket ticketPersisted = ticketService.createOrUpdate(ticketCurrent);
            ChangeStatus changeStatus = new ChangeStatus();
            changeStatus.setUserChange(userFromRequest(request));
            changeStatus.setDateChangeStatus(new Date());
            changeStatus.setStatus(StatusEnum.getStatus(status));
            changeStatus.setTicket(ticketPersisted);
            ticketService.createChangeStatus(changeStatus);
            response.setData(ticketPersisted);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    private void validateChangeStatus(String id, String status, BindingResult result) {
        if (id == null || id.equals("")) {
            result.addError(new ObjectError("Ticket", "Id no information"));
            return;
        }
        if (status == null || status.equals("")) {
            result.addError(new ObjectError("Ticket", "Status no information"));
        }
    }

    @GetMapping(value = "/summary")
    public ResponseEntity<Response<Summary>> findChart() {
        Response<Summary> response = new Response<>();
        Summary chart = new Summary();
        int amountNew = 0;
        int amountResolved = 0;
        int amountApproved = 0;
        int amountDisapproved = 0;
        int amountAssigned = 0;
        int amountClosed = 0;
        Iterable<Ticket> tickets = ticketService.findAll();
        if (tickets != null) {
            for (Ticket ticket : tickets) {
                if (ticket.getStatus().equals(StatusEnum.New)) {
                    amountNew++;
                }
                if (ticket.getStatus().equals(StatusEnum.Resolved)) {
                    amountResolved++;
                }
                if (ticket.getStatus().equals(StatusEnum.Approved)) {
                    amountApproved++;
                }
                if (ticket.getStatus().equals(StatusEnum.Disapproved)) {
                    amountDisapproved++;
                }
                if (ticket.getStatus().equals(StatusEnum.Assigned)) {
                    amountAssigned++;
                }
                if (ticket.getStatus().equals(StatusEnum.Closed)) {
                    amountClosed++;
                }
            }
        }
        chart.setAmountNew(amountNew);
        chart.setAmountResolved(amountResolved);
        chart.setAmountApproved(amountApproved);
        chart.setAmountDisapproved(amountDisapproved);
        chart.setAmountAssigned(amountAssigned);
        chart.setAmountClosed(amountClosed);
        response.setData(chart);
        return ResponseEntity.ok(response);
    }

}
