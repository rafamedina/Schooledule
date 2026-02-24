package com.tfg.Schooledule.infrastructure.Controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/alumno")
public class AlumnoController {

    @GetMapping("/dashboard")
    public String panelAlumno() {
        return "alumno/menuAlumno"; // Busca mis_notas.html
    }

}
