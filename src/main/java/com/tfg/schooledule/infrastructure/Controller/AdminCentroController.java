package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminCentroFormDTO;
import com.tfg.schooledule.infrastructure.service.AdminCentroService;
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
@RequestMapping("/admin/centros")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCentroController {

  private final AdminCentroService adminCentroService;

  public AdminCentroController(AdminCentroService adminCentroService) {
    this.adminCentroService = adminCentroService;
  }

  @GetMapping
  public String lista(Model model) {
    model.addAttribute("centros", adminCentroService.listarTodos());
    return "admin/centros/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminCentroFormDTO());
    return "admin/centros/formulario";
  }

  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminCentroFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/centros/formulario";
    }
    try {
      adminCentroService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/centros/formulario";
    }
    return "redirect:/admin/centros";
  }

  @GetMapping("/{id}/editar")
  public String editar(@PathVariable @Positive Integer id, Model model) {
    model.addAttribute("form", adminCentroService.obtenerParaEditar(id));
    return "admin/centros/formulario";
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      @PathVariable @Positive Integer id,
      @Valid @ModelAttribute("form") AdminCentroFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/centros/formulario";
    }
    try {
      adminCentroService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/centros/formulario";
    }
    return "redirect:/admin/centros";
  }

  @PostMapping("/{id}/toggle-activo")
  public String toggleActivo(
      @PathVariable @Positive Integer id, RedirectAttributes redirectAttributes) {
    try {
      adminCentroService.toggleActivo(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/centros";
  }
}
