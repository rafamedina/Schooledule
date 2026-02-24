package com.tfg.Schooledule.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        // Obtenemos todos los roles (autoridades) que tiene el usuario autenticado
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Extraemos los nombres de los roles quitando el prefijo "ROLE_"
        Set<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(role -> role.replace("ROLE_", ""))
                .collect(Collectors.toSet());

        // Guardamos los roles en la sesión para que puedan ser mostrados en la vista de
        // selección si es necesario
        request.getSession().setAttribute("userRoles", roles);

        // Si el usuario tiene más de un rol, lo redirigimos a la página para que
        // seleccione con qué rol quiere operar
        if (roles.size() > 1) {
            response.sendRedirect("/seleccionar-rol");
        }
        // Si el usuario solo tiene un rol, lo redirigimos directamente a su dashboard
        // específico
        else if (roles.size() == 1) {
            String role = roles.iterator().next().toLowerCase();
            response.sendRedirect("/" + role + "/dashboard");
        }
        // Si por alguna razón no tiene roles, lo mandamos al login con un error (o a
        // una página por defecto)
        else {
            response.sendRedirect("/login?error=noroles");
        }
    }
}
