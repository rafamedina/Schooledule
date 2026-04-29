package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminUsuarioFormDTO;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.RolRepository;
import com.tfg.schooledule.infrastructure.service.AdminUsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuarioController {

  private final AdminUsuarioService adminUsuarioService;
  private final RolRepository rolRepository;
  private final CentroRepository centroRepository;

  public AdminUsuarioController(
      AdminUsuarioService adminUsuarioService,
      RolRepository rolRepository,
      CentroRepository centroRepository) {
    this.adminUsuarioService = adminUsuarioService;
    this.rolRepository = rolRepository;
    this.centroRepository = centroRepository;
  }

  @GetMapping
  public String lista(Model model) {
    model.addAttribute("usuarios", adminUsuarioService.listarTodos());
    return "admin/usuarios/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminUsuarioFormDTO());
    model.addAttribute("roles", rolRepository.findAll());
    model.addAttribute("centros", centroRepository.findAll());
    return "admin/usuarios/formulario";
  }

  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminUsuarioFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("roles", rolRepository.findAll());
      model.addAttribute("centros", centroRepository.findAll());
      return "admin/usuarios/formulario";
    }
    adminUsuarioService.crear(form);
    return "redirect:/admin/usuarios";
  }

  @PostMapping("/{id}/toggle-activo")
  public String toggleActivo(@PathVariable Integer id) {
    adminUsuarioService.toggleActivo(id);
    return "redirect:/admin/usuarios";
  }
}
