package com.saswath.nile.auth;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LoginRequest {

    private String email;
    private String password;

    public String getEmail() { return email; }

    public String getPassword() { return password; }
}
