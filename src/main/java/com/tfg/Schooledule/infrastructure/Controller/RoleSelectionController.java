package com.tfg.Schooledule.infrastructure.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.Set;

@Controller
public class RoleSelectionController {

    @GetMapping("/seleccionar-rol")
    public String selectRolePage(HttpSession session, Model model) {
        // Recuperamos los roles de la sesión que guardamos en el
        // CustomLoginSuccessHandler
        Set<String> roles = (Set<String>) session.getAttribute("userRoles");

        if (roles == null || roles.isEmpty()) {
            return "redirect:/login?error=noroles";
        }

        // Pasamos los roles a la vista para que el usuario pueda elegir
        model.addAttribute("roles", roles);
        return "seleccionar-rol";
    }

    @PostMapping("/seleccionar-rol")
    public String processRoleSelection(@RequestParam("rol") String selectedRole,
            HttpSession session, HttpServletRequest request) {
        Set<String> roles = (Set<String>) session.getAttribute("userRoles");

        // Si la sesión expiró o intentan algo raro, volvemos a enviar a login
        if (roles == null || !roles.contains(selectedRole)) {
            return "redirect:/login?error=invalidrole";
        }

        // Actualizamos el contexto de seguridad de Spring (SecurityContext)
        // para que de ahora en adelante considere SÓLO el rol que el usuario acaba de
        // seleccionar
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + selectedRole.toUpperCase());

        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                auth.getPrincipal(),
                auth.getCredentials(),
                Collections.singletonList(authority));
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // Limpiamos la variable de sesión ya que no la necesitamos más
        session.removeAttribute("userRoles");

        // Redirigimos al dashboard correspondiente al rol que acaba de seleccionar
        return "redirect:/" + selectedRole.toLowerCase() + "/dashboard";
    }
}
