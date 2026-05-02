package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminGrupoFormDTO;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.service.AdminGrupoService;
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
@RequestMapping("/admin/grupos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGrupoController {

  private final AdminGrupoService adminGrupoService;
  private final CentroRepository centroRepository;
  private final CursoAcademicoRepository cursoAcademicoRepository;

  public AdminGrupoController(
      AdminGrupoService adminGrupoService,
      CentroRepository centroRepository,
      CursoAcademicoRepository cursoAcademicoRepository) {
    this.adminGrupoService = adminGrupoService;
    this.centroRepository = centroRepository;
    this.cursoAcademicoRepository = cursoAcademicoRepository;
  }

  @GetMapping
  public String lista(Model model) {
    model.addAttribute("grupos", adminGrupoService.listarTodos());
    return "admin/grupos/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminGrupoFormDTO());
    cargarListas(model);
    return "admin/grupos/formulario";
  }

  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminGrupoFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      cargarListas(model);
      return "admin/grupos/formulario";
    }
    try {
      adminGrupoService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      cargarListas(model);
      return "admin/grupos/formulario";
    }
    return "redirect:/admin/grupos";
  }

  @GetMapping("/{id}/editar")
  public String editar(@PathVariable @Positive Integer id, Model model) {
    model.addAttribute("form", adminGrupoService.obtenerParaEditar(id));
    cargarListas(model);
    return "admin/grupos/formulario";
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      @PathVariable @Positive Integer id,
      @Valid @ModelAttribute("form") AdminGrupoFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      cargarListas(model);
      return "admin/grupos/formulario";
    }
    try {
      adminGrupoService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      cargarListas(model);
      return "admin/grupos/formulario";
    }
    return "redirect:/admin/grupos";
  }

  @PostMapping("/{id}/eliminar")
  public String eliminar(
      @PathVariable @Positive Integer id, RedirectAttributes redirectAttributes) {
    try {
      adminGrupoService.eliminar(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/grupos";
  }

  private void cargarListas(Model model) {
    model.addAttribute("centros", centroRepository.findAllByOrderByNombreAsc());
    model.addAttribute("cursos", cursoAcademicoRepository.findAllByOrderByNombreAsc());
  }
}
