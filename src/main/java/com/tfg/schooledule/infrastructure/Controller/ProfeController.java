package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.GradeUpsertRequest;
import com.tfg.schooledule.domain.dto.ItemEvaluableFormDTO;
import com.tfg.schooledule.domain.dto.TeacherStudentGradesDTO;
import com.tfg.schooledule.domain.entity.Usuario;
import com.tfg.schooledule.infrastructure.repository.ItemEvaluableRepository;
import com.tfg.schooledule.infrastructure.repository.PeriodoEvaluacionRepository;
import com.tfg.schooledule.infrastructure.repository.ResultadoAprendizajeRepository;
import com.tfg.schooledule.infrastructure.service.TeacherDashboardService;
import com.tfg.schooledule.infrastructure.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profe")
@PreAuthorize("hasRole('PROFESOR')")
public class ProfeController {

  private final UsuarioService usuarioService;
  private final TeacherDashboardService teacherService;
  private final ItemEvaluableRepository itemEvaluableRepository;
  private final PeriodoEvaluacionRepository periodoRepository;
  private final ResultadoAprendizajeRepository raRepository;

  public ProfeController(
      UsuarioService usuarioService,
      TeacherDashboardService teacherService,
      ItemEvaluableRepository itemEvaluableRepository,
      PeriodoEvaluacionRepository periodoRepository,
      ResultadoAprendizajeRepository raRepository) {
    this.usuarioService = usuarioService;
    this.teacherService = teacherService;
    this.itemEvaluableRepository = itemEvaluableRepository;
    this.periodoRepository = periodoRepository;
    this.raRepository = raRepository;
  }

  /** Paso 1: muestra los centros del profesor autenticado. */
  @GetMapping("/dashboard")
  public String dashboard(Principal principal, Model model) {
    Usuario profesor = resolveProfesor(principal);
    model.addAttribute("centros", teacherService.getCentersForTeacher(profesor));
    model.addAttribute("teacherName", profesor.getNombre() + " " + profesor.getApellidos());
    return "profe/dashboard";
  }

  /** Paso 2: asignaturas del profesor en un centro concreto. */
  @GetMapping("/centro/{centroId}/asignaturas")
  public String asignaturas(@PathVariable Integer centroId, Principal principal, Model model) {
    Usuario profesor = resolveProfesor(principal);
    teacherService.validateCentroOwnership(profesor.getId(), centroId);
    model.addAttribute(
        "asignaturas", teacherService.getSubjectsForTeacherAndCenter(profesor.getId(), centroId));
    model.addAttribute("centroId", centroId);
    // Nombre del centro desde los centros del profesor
    profesor.getCentros().stream()
        .filter(c -> c.getId().equals(centroId))
        .findFirst()
        .ifPresent(c -> model.addAttribute("centroNombre", c.getNombre()));
    return "profe/asignaturas";
  }

  /** Paso 3: listado de alumnos de una impartición. */
  @GetMapping("/imparticion/{imparticionId}/alumnos")
  public String alumnos(
      @PathVariable @Positive Integer imparticionId, Principal principal, Model model) {
    Usuario profesor = resolveProfesor(principal);
    model.addAttribute(
        "alumnos", teacherService.getRosterForImparticion(profesor.getId(), imparticionId));
    model.addAttribute("imparticionId", imparticionId);
    model.addAttribute(
        "imparticionLabel",
        teacherService
            .getSubjectsForTeacherAndCenter(
                profesor.getId(),
                teacherService.getCentroIdByImparticion(profesor.getId(), imparticionId))
            .stream()
            .filter(s -> s.imparticionId().equals(imparticionId))
            .findFirst()
            .map(s -> s.grupoNombre() + " · " + s.moduloNombre())
            .orElse(""));

    // Datos para gestión de ítems evaluables
    model.addAttribute("itemForm", new ItemEvaluableFormDTO());
    model.addAttribute(
        "items",
        itemEvaluableRepository.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(
            imparticionId));
    model.addAttribute("periodos", periodoRepository.findByImparticionId(imparticionId));
    Integer moduloId = teacherService.getModuloIdByImparticion(profesor.getId(), imparticionId);
    model.addAttribute("ras", raRepository.findByModuloIdOrderByCodigoAsc(moduloId));

    return "profe/alumnos";
  }

  /** API: obtiene las notas de un alumno (JSON). */
  @GetMapping("/api/matricula/{matriculaId}/notas")
  @ResponseBody
  public TeacherStudentGradesDTO getNotas(@PathVariable Integer matriculaId, Principal principal) {
    Usuario profesor = resolveProfesor(principal);
    return teacherService.getStudentGrades(profesor.getId(), matriculaId);
  }

  /** API: guarda/actualiza notas y devuelve el payload actualizado (JSON). */
  @PostMapping("/api/matricula/{matriculaId}/notas")
  @ResponseBody
  public TeacherStudentGradesDTO postNotas(
      @PathVariable Integer matriculaId,
      @Valid @RequestBody GradeUpsertRequest req,
      Principal principal) {
    // El matriculaId del path debe coincidir con el del body
    if (!matriculaId.equals(req.matriculaId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "matriculaId en path y body no coinciden");
    }
    Usuario profesor = resolveProfesor(principal);
    return teacherService.upsertGrades(profesor.getId(), profesor.getEmail(), req);
  }

  @PostMapping("/imparticion/{imparticionId}/items")
  public String crearItem(
      @PathVariable @Positive Integer imparticionId,
      @Valid @ModelAttribute("itemForm") ItemEvaluableFormDTO dto,
      BindingResult result,
      Principal principal,
      RedirectAttributes ra) {
    if (result.hasErrors()) {
      ra.addFlashAttribute("errorItem", "Datos del ítem no válidos.");
      return "redirect:/profe/imparticion/" + imparticionId + "/alumnos";
    }
    Usuario profesor = resolveProfesor(principal);
    try {
      teacherService.crearItem(imparticionId, profesor.getId(), dto);
      ra.addFlashAttribute("okItem", "Ítem añadido correctamente.");
    } catch (Exception ex) {
      ra.addFlashAttribute("errorItem", ex.getMessage());
    }
    return "redirect:/profe/imparticion/" + imparticionId + "/alumnos";
  }

  @PostMapping("/items/{itemId}/eliminar")
  public String eliminarItem(
      @PathVariable @Positive Integer itemId, Principal principal, RedirectAttributes ra) {
    Usuario profesor = resolveProfesor(principal);
    try {
      Integer imparticionId = teacherService.eliminarItem(itemId, profesor.getId());
      ra.addFlashAttribute("okItem", "Ítem eliminado.");
      return "redirect:/profe/imparticion/" + imparticionId + "/alumnos";
    } catch (Exception ex) {
      ra.addFlashAttribute("errorItem", ex.getMessage());
      return "redirect:/profe/dashboard";
    }
  }

  private Usuario resolveProfesor(Principal principal) {
    return usuarioService
        .buscarPorCorreo(principal.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  }
}
