package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminModuloFormDTO;
import com.tfg.schooledule.infrastructure.service.AdminModuloService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/modulos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModuloController {

  private final AdminModuloService adminModuloService;

  public AdminModuloController(AdminModuloService adminModuloService) {
    this.adminModuloService = adminModuloService;
  }

  @GetMapping
  public String lista(Model model) {
    model.addAttribute("modulos", adminModuloService.listarTodos());
    return "admin/modulos/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminModuloFormDTO());
    return "admin/modulos/formulario";
  }

  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminModuloFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/modulos/formulario";
    }
    try {
      adminModuloService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/modulos/formulario";
    }
    return "redirect:/admin/modulos";
  }

  @GetMapping("/{id}/editar")
  public String editar(@PathVariable @Positive Integer id, Model model) {
    model.addAttribute("form", adminModuloService.obtenerParaEditar(id));
    return "admin/modulos/formulario";
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      @PathVariable @Positive Integer id,
      @Valid @ModelAttribute("form") AdminModuloFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/modulos/formulario";
    }
    try {
      adminModuloService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/modulos/formulario";
    }
    return "redirect:/admin/modulos";
  }

  @PostMapping("/{id}/toggle-activo")
  public String toggleActivo(
      @PathVariable @Positive Integer id, RedirectAttributes redirectAttributes) {
    try {
      adminModuloService.toggleActivo(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/modulos";
  }
}
