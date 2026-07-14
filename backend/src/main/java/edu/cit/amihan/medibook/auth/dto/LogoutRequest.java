package edu.cit.amihan.medibook.auth.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
