package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.infrastructure.service.AdminAuditoriaService;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/auditoria")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditoriaController {

  private final AdminAuditoriaService adminAuditoriaService;

  public AdminAuditoriaController(AdminAuditoriaService adminAuditoriaService) {
    this.adminAuditoriaService = adminAuditoriaService;
  }

  @GetMapping
  public String lista(
      @RequestParam(required = false) String alumnoEmail,
      @RequestParam(required = false) String moduloNombre,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fechaDesde,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate fechaHasta,
      Model model) {
    model.addAttribute(
        "registros",
        adminAuditoriaService.buscar(alumnoEmail, moduloNombre, fechaDesde, fechaHasta));
    model.addAttribute("alumnoEmail", alumnoEmail);
    model.addAttribute("moduloNombre", moduloNombre);
    model.addAttribute("fechaDesde", fechaDesde);
    model.addAttribute("fechaHasta", fechaHasta);
    return "admin/auditoria/lista";
  }
}
