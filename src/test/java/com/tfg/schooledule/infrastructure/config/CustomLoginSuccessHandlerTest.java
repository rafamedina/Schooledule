package com.tfg.schooledule.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;

import static org.mockito.Mockito.*;

class CustomLoginSuccessHandlerTest {

    private CustomLoginSuccessHandler handler;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        handler = new CustomLoginSuccessHandler();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        authentication = mock(Authentication.class);
    }

    @Test
    void testRedirectAdmin() throws Exception {
        doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .when(authentication).getAuthorities();

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("/admin/dashboard");
    }

    @Test
    void testRedirectProfesor() throws Exception {
        doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_PROFESOR")))
                .when(authentication).getAuthorities();

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("/profe/menuProfesor");
    }

    @Test
    void testRedirectAlumno() throws Exception {
        doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_ALUMNO")))
                .when(authentication).getAuthorities();

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("/alumno/menuAlumno");
    }

    @Test
    void testRedirectDefault() throws Exception {
        doReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")))
                .when(authentication).getAuthorities();

        handler.onAuthenticationSuccess(request, response, authentication);

        verify(response).sendRedirect("/");
    }
}
