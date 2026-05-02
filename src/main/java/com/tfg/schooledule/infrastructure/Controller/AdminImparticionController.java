package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminImparticionFormDTO;
import com.tfg.schooledule.infrastructure.repository.CentroRepository;
import com.tfg.schooledule.infrastructure.repository.GrupoRepository;
import com.tfg.schooledule.infrastructure.repository.ModuloRepository;
import com.tfg.schooledule.infrastructure.repository.UsuarioRepository;
import com.tfg.schooledule.infrastructure.service.AdminImparticionService;
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
@RequestMapping("/admin/imparticiones")
@PreAuthorize("hasRole('ADMIN')")
public class AdminImparticionController {

  private final AdminImparticionService adminImparticionService;
  private final ModuloRepository moduloRepository;
  private final GrupoRepository grupoRepository;
  private final UsuarioRepository usuarioRepository;
  private final CentroRepository centroRepository;

  public AdminImparticionController(
      AdminImparticionService adminImparticionService,
      ModuloRepository moduloRepository,
      GrupoRepository grupoRepository,
      UsuarioRepository usuarioRepository,
      CentroRepository centroRepository) {
    this.adminImparticionService = adminImparticionService;
    this.moduloRepository = moduloRepository;
    this.grupoRepository = grupoRepository;
    this.usuarioRepository = usuarioRepository;
    this.centroRepository = centroRepository;
  }

  @GetMapping
  public String lista(Model model) {
    model.addAttribute("imparticiones", adminImparticionService.listarTodas());
    return "admin/imparticiones/lista";
  }

  @GetMapping("/nuevo")
  public String nuevo(Model model) {
    model.addAttribute("form", new AdminImparticionFormDTO());
    cargarListas(model);
    return "admin/imparticiones/formulario";
  }

  @PostMapping("/nuevo")
  public String crear(
      @Valid @ModelAttribute("form") AdminImparticionFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      cargarListas(model);
      return "admin/imparticiones/formulario";
    }
    try {
      adminImparticionService.crear(form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      cargarListas(model);
      return "admin/imparticiones/formulario";
    }
    return "redirect:/admin/imparticiones";
  }

  @GetMapping("/{id}/editar")
  public String editar(@PathVariable @Positive Integer id, Model model) {
    model.addAttribute("form", adminImparticionService.obtenerParaEditar(id));
    cargarListas(model);
    return "admin/imparticiones/formulario";
  }

  @PostMapping("/{id}/editar")
  public String actualizar(
      @PathVariable @Positive Integer id,
      @Valid @ModelAttribute("form") AdminImparticionFormDTO form,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      cargarListas(model);
      return "admin/imparticiones/formulario";
    }
    try {
      adminImparticionService.actualizar(id, form);
    } catch (IllegalArgumentException ex) {
      model.addAttribute("error", ex.getMessage());
      cargarListas(model);
      return "admin/imparticiones/formulario";
    }
    return "redirect:/admin/imparticiones";
  }

  @PostMapping("/{id}/eliminar")
  public String eliminar(
      @PathVariable @Positive Integer id, RedirectAttributes redirectAttributes) {
    try {
      adminImparticionService.eliminar(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/imparticiones";
  }

  private void cargarListas(Model model) {
    model.addAttribute("modulos", moduloRepository.findByActivoTrueOrderByNombreAsc());
    model.addAttribute("grupos", grupoRepository.findAllByOrderByCentroNombreAscNombreAsc());
    model.addAttribute("profesores", usuarioRepository.findAllProfesoresOrdenados());
    model.addAttribute("centros", centroRepository.findAllByActivoTrueOrderByNombreAsc());
  }
}
