package com.tfg.schooledule.infrastructure.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        java.util.Set<String> roles = org.springframework.security.core.authority.AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        
        if (roles.contains("ROLE_ADMIN")) {
            response.sendRedirect("/admin/dashboard");
        } else if (roles.contains("ROLE_PROFESOR")) {
            response.sendRedirect("/profe/menuProfesor");
        } else if (roles.contains("ROLE_ALUMNO")) {
            response.sendRedirect("/alumno/menuAlumno");
        } else {
            response.sendRedirect("/");
        }
    }
}
