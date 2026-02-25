package com.tfg.Schooledule.infrastructure.Controller;


import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {



    @GetMapping("/dashboard")
    public String panelAdministrador() {

        // 2. EL MÉTODO DEVUELVE EL NOMBRE DEL ARCHIVO HTML
        // Esto busca: src/main/resources/templates/admin/dashboard.html
        return "admin/dashboard";
    }
}
