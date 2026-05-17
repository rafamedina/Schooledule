package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.dto.AdminModuloImportarFormDTO;
import com.tfg.schooledule.domain.dto.AdminModuloPesosFormDTO;
import com.tfg.schooledule.domain.dto.AdminModuloResumenDTO;
import com.tfg.schooledule.domain.exception.ModuloImportException;
import com.tfg.schooledule.infrastructure.service.AdminModuloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Tag(name = "Admin - Módulos")
@Controller
@RequestMapping("/admin/modulos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModuloController {

  private static final Set<String> MIME_XLSX =
      Set.of(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "application/octet-stream");

  private final AdminModuloService adminModuloService;

  public AdminModuloController(AdminModuloService adminModuloService) {
    this.adminModuloService = adminModuloService;
  }

  @Operation(summary = "Listado de módulos")
  @ApiResponse(responseCode = "200", description = "Vista HTML: admin/modulos/lista")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping
  public String lista(@RequestParam(required = false) String nombre, Model model) {
    model.addAttribute("modulos", adminModuloService.listarFiltrado(nombre));
    model.addAttribute("nombre", nombre);
    return "admin/modulos/lista";
  }

  @Operation(summary = "Resumen de módulo (JSON)")
  @ApiResponse(responseCode = "200", description = "JSON: AdminModuloResumenDTO")
  @ApiResponse(responseCode = "404", description = "Módulo no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{id}/resumen")
  @ResponseBody
  public AdminModuloResumenDTO resumen(
      @Parameter(description = "ID del módulo formativo", required = true) @PathVariable @Positive
          Integer id) {
    return adminModuloService.getResumen(id);
  }

  @Operation(summary = "Formulario: importar módulo desde Excel")
  @ApiResponse(responseCode = "200", description = "Vista HTML: admin/modulos/importar")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/importar")
  public String formularioImportar(Model model) {
    model.addAttribute("form", new AdminModuloImportarFormDTO());
    return "admin/modulos/importar";
  }

  @Operation(summary = "Acción: importar módulo desde Excel")
  @ApiResponse(
      responseCode = "302",
      description = "Importación exitosa → redirect a /admin/modulos")
  @ApiResponse(responseCode = "200", description = "Formulario con errores")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/importar")
  public String importar(
      @Valid @ModelAttribute("form") AdminModuloImportarFormDTO form,
      BindingResult bindingResult,
      @RequestParam("archivo") MultipartFile archivo,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      return "admin/modulos/importar";
    }
    if (archivo.isEmpty()) {
      model.addAttribute("error", "Debes seleccionar un archivo Excel (.xlsx).");
      return "admin/modulos/importar";
    }
    if (!esXlsx(archivo)) {
      model.addAttribute(
          "error", "El archivo debe tener extensión .xlsx y tipo de contenido Excel.");
      return "admin/modulos/importar";
    }
    try {
      byte[] bytes = archivo.getBytes();
      int totalCes =
          adminModuloService.importarModulo(
              form.getCodigo(), form.getNombre(), form.getCursoAcademicoId(), bytes);
      redirectAttributes.addFlashAttribute(
          "exito", totalCes + " criterios de evaluación importados correctamente.");
      return "redirect:/admin/modulos";
    } catch (ModuloImportException e) {
      model.addAttribute("errores", e.getErrores());
      return "admin/modulos/importar";
    } catch (IllegalStateException | EntityNotFoundException e) {
      model.addAttribute("error", e.getMessage());
      return "admin/modulos/importar";
    } catch (IOException e) {
      model.addAttribute("error", "No se pudo leer el archivo subido.");
      return "admin/modulos/importar";
    }
  }

  @Operation(summary = "Formulario: editar pesos de RAs y CEs de un módulo")
  @ApiResponse(responseCode = "200", description = "Vista HTML: admin/modulos/editar-pesos")
  @ApiResponse(responseCode = "404", description = "Módulo no encontrado")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @GetMapping("/{id}/editar")
  public String editarPesos(
      @Parameter(description = "ID del módulo formativo", required = true) @PathVariable @Positive
          Integer id,
      Model model) {
    model.addAttribute("moduloId", id);
    model.addAttribute("form", adminModuloService.obtenerParaEditarPesos(id));
    return "admin/modulos/editar-pesos";
  }

  @Operation(summary = "Acción: actualizar pesos de RAs y CEs de un módulo")
  @ApiResponse(
      responseCode = "302",
      description = "Actualización exitosa → redirect a /admin/modulos")
  @ApiResponse(responseCode = "200", description = "Formulario con errores de validación")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/editar")
  public String actualizarPesos(
      @Parameter(description = "ID del módulo formativo", required = true) @PathVariable @Positive
          Integer id,
      @Valid @ModelAttribute("form") AdminModuloPesosFormDTO form,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes) {
    if (bindingResult.hasErrors()) {
      model.addAttribute("moduloId", id);
      return "admin/modulos/editar-pesos";
    }
    try {
      adminModuloService.actualizarPesos(id, form);
      redirectAttributes.addFlashAttribute("exito", "Módulo actualizado correctamente.");
    } catch (IllegalArgumentException | EntityNotFoundException ex) {
      model.addAttribute("error", ex.getMessage());
      model.addAttribute("moduloId", id);
      return "admin/modulos/editar-pesos";
    }
    return "redirect:/admin/modulos";
  }

  @Operation(summary = "Acción: activar/desactivar módulo")
  @ApiResponse(responseCode = "302", description = "Toggle exitoso → redirect a /admin/modulos")
  @ApiResponse(responseCode = "403", description = "Acceso denegado — requiere ROLE_ADMIN")
  @PostMapping("/{id}/toggle-activo")
  public String toggleActivo(
      @Parameter(description = "ID del módulo formativo", required = true) @PathVariable @Positive
          Integer id,
      RedirectAttributes redirectAttributes) {
    try {
      adminModuloService.toggleActivo(id);
    } catch (IllegalStateException ex) {
      redirectAttributes.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/admin/modulos";
  }

  private boolean esXlsx(MultipartFile archivo) {
    String nombre = archivo.getOriginalFilename();
    if (nombre == null || !nombre.toLowerCase(Locale.ROOT).endsWith(".xlsx")) return false;
    String contentType = archivo.getContentType();
    return contentType != null && MIME_XLSX.contains(contentType);
  }
}
