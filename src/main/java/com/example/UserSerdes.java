package com.example;

import java.io.UncheckedIOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class UserSerdes {
    private ObjectMapper mapper; 
    
    public UserSerdes() {
        mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }
    
    public String serialize(final User user) {
        try {
            return mapper.writeValueAsString(user);
        } catch (final JsonProcessingException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    
    public User deserialize(final String json) {
        try {
            return mapper.readValue(json, User.class);
        } catch (final JsonProcessingException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
