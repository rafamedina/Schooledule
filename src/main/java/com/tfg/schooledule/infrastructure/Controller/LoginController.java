package com.tfg.schooledule.infrastructure.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String vistaLogin() {
        return "login";
    }

    @GetMapping("/seleccionar-rol")
    public String vistaSeleccionarRol() {
        return "seleccionar-rol";
    }
}
