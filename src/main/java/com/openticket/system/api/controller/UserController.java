package com.openticket.system.api.controller;

import com.openticket.system.api.response.Response;
import com.openticket.system.api.security.entity.User;
import com.openticket.system.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserController(PasswordEncoder passwordEncoder, UserService userService) {
        this.passwordEncoder = passwordEncoder;
        this.userService = userService;
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Response<User>> create(HttpServletRequest request, @RequestBody User user, BindingResult result) {
        Response<User> response = new Response<>();
        try {
            validateCreateUser(user, result);
            if (result.hasErrors()) {
                result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(response);
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User userPersisted = userService.createOrUpdate(user);
            response.setData(userPersisted);
        } catch (DuplicateKeyException dE) {
            response.getErrors().add("E-mail already registered !");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    private void validateCreateUser(User user, BindingResult result) {
        if (user.getEmail() == null) {
            result.addError(new ObjectError("User", "Email no information"));
        }
    }

    @PutMapping()
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Response<User>> update(HttpServletRequest request, @RequestBody User user,
                                                 BindingResult result) {
        Response<User> response = new Response<>();
        try {
            validateUpdate(user, result);
            if (result.hasErrors()) {
                result.getAllErrors().forEach(error -> response.getErrors().add(error.getDefaultMessage()));
                return ResponseEntity.badRequest().body(response);
            }
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User userPersisted = userService.createOrUpdate(user);
            response.setData(userPersisted);
        } catch (Exception e) {
            response.getErrors().add(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
        return ResponseEntity.ok(response);
    }

    private void validateUpdate(User user, BindingResult result) {
        if (user.getId() == null) {
            result.addError(new ObjectError("User", "Id no information"));
            return;
        }
        if (user.getEmail() == null) {
            result.addError(new ObjectError("User", "Email no information"));
        }
    }

    @GetMapping(value = "{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Response<User>> findById(@PathVariable("id") String id) {
        Response<User> response = new Response<>();
        Optional<User> userOptional = userService.findById(id);
        User user = userOptional.get();
        if (user == null) {
            response.getErrors().add("Register not found id:" + id);
            return ResponseEntity.badRequest().body(response);
        }
        response.setData(user);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Response<String>> delete(@PathVariable("id") String id) {
        Response<String> response = new Response<>();
        Optional<User> userOptional = userService.findById(id);
        User user = userOptional.get();
        if (user == null) {
            response.getErrors().add("Register not found id:" + id);
            return ResponseEntity.badRequest().body(response);
        }
        userService.delete(id);
        return ResponseEntity.ok(new Response<>());
    }


    @GetMapping(value = "{page}/{count}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Response<Page<User>>> findAll(@PathVariable int page, @PathVariable int count) {
        Response<Page<User>> response = new Response<>();
        Page<User> users = userService.findAll(page, count);
        response.setData(users);
        return ResponseEntity.ok(response);
    }

}
