package com.saswath.nile.auth;

import com.saswath.nile.entity.User;

public class AuthResponse {

    private String token;
    private UserResponse user;

    public AuthResponse(String token, User user) {
        this.token = token;
        this.user = new UserResponse(user);
    }

    public String getToken() { return token; }

    public UserResponse getUser() { return user; }

    public static class UserResponse {

        private Long id;
        private String name;
        private String email;
        private String role;

        public UserResponse(User user) {
            this.id = user.getId();
            this.name = user.getName();
            this.email = user.getEmail();
            this.role = user.getRole();
        }

        public Long getId() { return id; }

        public String getName() { return name; }

        public String getEmail() { return email; }

        public String getRole() { return role; }
    }
}
