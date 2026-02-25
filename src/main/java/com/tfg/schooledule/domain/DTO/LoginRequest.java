package com.tfg.Schooledule.domain.DTO;

import lombok.Data;

// Clase pequeña para mapear el JSON
@Data
public class LoginRequest {
    public String email;
    public String password;
}