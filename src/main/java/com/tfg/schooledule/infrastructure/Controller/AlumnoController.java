package com.tfg.schooledule.infrastructure.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

@Controller
@RequestMapping("/alumno")
public class AlumnoController {

    @Autowired
    private com.tfg.schooledule.infrastructure.Service.UsuarioService usuarioService;

    @GetMapping("/dashboard")
    public String panelAlumno() {
        return "alumno/menuAlumno"; // Busca mis_notas.html
    }

    @GetMapping("/perfil")
    public String perfilAlumno(Principal principal, Model model) {
        String username = principal.getName();
        com.tfg.schooledule.domain.entity.Usuario usuario = usuarioService.buscarPorNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        com.tfg.schooledule.domain.DTO.AlumnoProfileDTO profile = usuarioService.getAlumnoProfile(usuario.getId());
        model.addAttribute("profile", profile);
        return "alumno/perfil";
    }

    @GetMapping("/notas")
    public String dashboardNotas(@org.springframework.web.bind.annotation.RequestParam(required = false) Integer periodoId, 
                                Principal principal, Model model) {
        String username = principal.getName();
        com.tfg.schooledule.domain.entity.Usuario usuario = usuarioService.buscarPorNombreUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        java.util.List<com.tfg.schooledule.domain.entity.PeriodoEvaluacion> periodos = usuarioService.getStudentPeriods(usuario.getId());
        model.addAttribute("periodos", periodos);

        if (periodoId == null && !periodos.isEmpty()) {
            periodoId = periodos.get(0).getId();
        }

        if (periodoId != null) {
            com.tfg.schooledule.domain.DTO.GradeDashboardDTO dashboard = usuarioService.getStudentGrades(usuario.getId(), periodoId);
            model.addAttribute("dashboard", dashboard);
            model.addAttribute("selectedPeriodoId", periodoId);
        }

        return "alumno/dashboard_notas";
    }

}
