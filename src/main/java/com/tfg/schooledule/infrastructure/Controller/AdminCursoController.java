package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminCursoFormDTO;
import com.tfg.schooledule.infrastructure.service.AdminCursoService;
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
@RequestMapping("/admin/cursos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCursoController {

  private final AdminCursoService adminCursoService;

  public AdminCursoController(AdminCursoService adminCursoService) {
    this.adminCursoService = adminCursoService;
  }

  @GetMapping
  public String lista(Model model) {
    model.addAttribute("cursos", adminCursoService.listarTodos());
    return "admin/cursos/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminCursoFormDTO());
    return "admin/cursos/formulario";
  }

  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminCursoFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/cursos/formulario";
    }
    try {
      adminCursoService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/cursos/formulario";
    }
    return "redirect:/admin/cursos";
  }

  @GetMapping("/{id}/editar")
  public String editar(@PathVariable @Positive Integer id, Model model) {
    model.addAttribute("form", adminCursoService.obtenerParaEditar(id));
    return "admin/cursos/formulario";
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      @PathVariable @Positive Integer id,
      @Valid @ModelAttribute("form") AdminCursoFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "admin/cursos/formulario";
    }
    try {
      adminCursoService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      return "admin/cursos/formulario";
    }
    return "redirect:/admin/cursos";
  }

  @PostMapping("/{id}/activar")
  public String activar(@PathVariable @Positive Integer id, RedirectAttributes redirectAttributes) {
    try {
      adminCursoService.activar(id);
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/cursos";
  }

  @PostMapping("/{id}/cerrar")
  public String cerrar(@PathVariable @Positive Integer id, RedirectAttributes redirectAttributes) {
    try {
      adminCursoService.cerrar(id);
    } catch (Exception ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/cursos";
  }
}
