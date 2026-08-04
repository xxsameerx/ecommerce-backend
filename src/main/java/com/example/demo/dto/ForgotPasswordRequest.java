package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;

public class ForgotPasswordRequest {
    @NotBlank
    private String emailOrMobile;

    public String getEmailOrMobile() { return emailOrMobile; }
    public void setEmailOrMobile(String emailOrMobile) { this.emailOrMobile = emailOrMobile; }
}