package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminMatriculaFormDTO;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.repository.ImparticionRepository;
import com.tfg.schooledule.infrastructure.service.AdminAlumnoService;
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
@RequestMapping("/admin/alumnos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAlumnoController {

  private final AdminAlumnoService adminAlumnoService;
  private final ImparticionRepository imparticionRepository;

  public AdminAlumnoController(
      AdminAlumnoService adminAlumnoService, ImparticionRepository imparticionRepository) {
    this.adminAlumnoService = adminAlumnoService;
    this.imparticionRepository = imparticionRepository;
  }

  @GetMapping
  public String lista(Model model) {
    model.addAttribute("alumnos", adminAlumnoService.listarAlumnos());
    return "admin/alumnos/lista";
  }

  @GetMapping("/{alumnoId}/matriculas")
  public String matriculas(@PathVariable @Positive Integer alumnoId, Model model) {
    model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
    model.addAttribute("matriculas", adminAlumnoService.listarMatriculas(alumnoId));
    return "admin/alumnos/matriculas";
  }

  @GetMapping("/{alumnoId}/matriculas/nueva")
  public String nuevaMatricula(@PathVariable @Positive Integer alumnoId, Model model) {
    model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
    model.addAttribute("form", new AdminMatriculaFormDTO());
    cargarImparticionesYEstados(model);
    return "admin/alumnos/matricula-formulario";
  }

  @PostMapping("/{alumnoId}/matriculas/nueva")
  public String crearMatricula(
      @PathVariable @Positive Integer alumnoId,
      @Valid @ModelAttribute("form") AdminMatriculaFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
      cargarImparticionesYEstados(model);
      return "admin/alumnos/matricula-formulario";
    }
    try {
      adminAlumnoService.crearMatricula(alumnoId, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
      model.addAttribute("error", ex.getMessage());
      cargarImparticionesYEstados(model);
      return "admin/alumnos/matricula-formulario";
    }
    return "redirect:/admin/alumnos/" + alumnoId + "/matriculas";
  }

  @GetMapping("/{alumnoId}/matriculas/{id}/editar")
  public String editarMatricula(
      @PathVariable @Positive Integer alumnoId, @PathVariable @Positive Integer id, Model model) {
    model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
    model.addAttribute("form", adminAlumnoService.obtenerMatriculaParaEditar(id));
    cargarImparticionesYEstados(model);
    return "admin/alumnos/matricula-formulario";
  }

  @PostMapping("/{alumnoId}/matriculas/{id}/editar")
  public String actualizarMatricula(
      @PathVariable @Positive Integer alumnoId,
      @PathVariable @Positive Integer id,
      @Valid @ModelAttribute("form") AdminMatriculaFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("alumno", adminAlumnoService.obtenerAlumno(alumnoId));
      cargarImparticionesYEstados(model);
      return "admin/alumnos/matricula-formulario";
    }
    adminAlumnoService.actualizarMatricula(id, form);
    return "redirect:/admin/alumnos/" + alumnoId + "/matriculas";
  }

  @PostMapping("/{alumnoId}/matriculas/{id}/eliminar")
  public String eliminarMatricula(
      @PathVariable @Positive Integer alumnoId,
      @PathVariable @Positive Integer id,
      RedirectAttributes redirectAttributes) {
    try {
      adminAlumnoService.eliminarMatricula(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/alumnos/" + alumnoId + "/matriculas";
  }

  private void cargarImparticionesYEstados(Model model) {
    model.addAttribute(
        "imparticiones", imparticionRepository.findAllByOrderByGrupoNombreAscModuloNombreAsc());
    model.addAttribute("estadosMatricula", EstadoMatricula.values());
  }
}
