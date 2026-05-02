package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.infrastructure.service.AdminUsuarioService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final AdminUsuarioService adminUsuarioService;

  public AdminController(AdminUsuarioService adminUsuarioService) {
    this.adminUsuarioService = adminUsuarioService;
  }

  @GetMapping("/dashboard")
  public String panelAdministrador(Model model) {
    model.addAttribute("stats", adminUsuarioService.getStats());
    return "admin/dashboard";
  }
}
