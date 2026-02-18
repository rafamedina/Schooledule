package com.tfg.schooledule.infrastructure.Controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class LoginController {

    // 1. Muestra el formulario de Login
    @GetMapping("/login")
    public String vistaLogin() {
        return "login";
    }

    // 2. Muestra la pantalla de elegir rol (si tienes varios)
    @GetMapping("/seleccionar-perfil")
    public String vistaSeleccion() {
        return "seleccionar-perfil";
    }

    // 3. CAMBIO DE ROL REAL (Lógica Segura)
    @PostMapping("/cambiar-rol")
    public String cambiarRol(@RequestParam String rolElegido) {
        
        // A. Obtenemos quién está logueado ahora mismo
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // B. Creamos la nueva autoridad única (ej: ROLE_ADMIN)
        // OJO: Si en tu BD los roles no llevan "ROLE_", añade "ROLE_" + rolElegido
        GrantedAuthority nuevaAutoridad = new SimpleGrantedAuthority(rolElegido);

        // C. Creamos un nuevo "Carnet de Identidad" con ese único rol
        Authentication nuevaAuth = new UsernamePasswordAuthenticationToken(
                auth.getPrincipal(),
                auth.getCredentials(),
                List.of(nuevaAutoridad) // Solo el rol elegido
        );

        // D. Actualizamos la seguridad de Spring
        SecurityContextHolder.getContext().setAuthentication(nuevaAuth);

        // E. Redirigimos a donde corresponda
        if (rolElegido.contains("ROLE_ADMIN")) return "redirect:/admin/dashboard";
        if (rolElegido.contains("ROLE_PROFESOR")) return "redirect:/profesor/menuProfesor";
        if (rolElegido.contains("ROLE_ALUMNO")) return "redirect:/alumno/menuAlumno";
        
        return "redirect:/home";
    }
}