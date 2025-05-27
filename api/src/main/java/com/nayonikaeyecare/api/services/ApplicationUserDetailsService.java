package com.nayonikaeyecare.api.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.nayonikaeyecare.api.repositories.user.UserNotFoundException;
import com.nayonikaeyecare.api.repositories.user.UserRepository;

@Service
public class ApplicationUserDetailsService implements UserDetailsService {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, String> users = new HashMap<>();
    private UserRepository userRepository;

    public ApplicationUserDetailsService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        // Add some test users

    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        // Implement your logic to load user details from the database or any other
        // source
        // For example, you can use a UserRepository to fetch user details from a
        // database

        // TODO: needs refactoring
        // this needs to be cached so as to avoid multiple database calls to mongo
        com.nayonikaeyecare.api.entities.user.User user = userRepository.findById(new ObjectId(username))
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return new User(user.getId().toString(), "", Collections.emptyList()); // Replace with actual user details
    }
}