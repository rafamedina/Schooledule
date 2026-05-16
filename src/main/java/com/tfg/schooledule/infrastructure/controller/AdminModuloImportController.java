package com.tfg.schooledule.infrastructure.controller;

import com.tfg.schooledule.domain.exception.ModuloImportException;
import com.tfg.schooledule.infrastructure.service.ModuloImportService;
import com.tfg.schooledule.infrastructure.service.ModuloPlantillaExcelService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/modulos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminModuloImportController {

  private static final Set<String> MIME_XLSX =
      Set.of(
          "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
          "application/octet-stream");

  private final ModuloImportService importService;
  private final ModuloPlantillaExcelService plantillaService;

  public AdminModuloImportController(
      ModuloImportService importService, ModuloPlantillaExcelService plantillaService) {
    this.importService = importService;
    this.plantillaService = plantillaService;
  }

  @GetMapping("/plantilla-excel")
  public void descargarPlantilla(HttpServletResponse response) throws IOException {
    byte[] bytes = plantillaService.generarPlantilla();
    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    response.setHeader("Content-Disposition", "attachment; filename=plantilla_ras.xlsx");
    response.setContentLength(bytes.length);
    response.getOutputStream().write(bytes);
  }

  @GetMapping("/{moduloId}/importar-ras")
  public String formulario(@PathVariable Integer moduloId, Model model) {
    model.addAttribute("moduloId", moduloId);
    return "admin/modulos/importar-ras";
  }

  @PostMapping("/{moduloId}/importar-ras")
  public String importar(
      @PathVariable Integer moduloId,
      @RequestParam Integer cursoAcademicoId,
      @RequestParam("archivo") MultipartFile archivo,
      RedirectAttributes redirectAttributes) {

    if (archivo.isEmpty()) {
      redirectAttributes.addFlashAttribute("error", "Debes seleccionar un archivo Excel (.xlsx).");
      return "redirect:/admin/modulos/" + moduloId + "/importar-ras";
    }

    if (!esXlsx(archivo)) {
      redirectAttributes.addFlashAttribute(
          "error", "El archivo debe tener extensión .xlsx y tipo de contenido Excel.");
      return "redirect:/admin/modulos/" + moduloId + "/importar-ras";
    }

    try {
      byte[] bytes = archivo.getBytes();
      int totalCes = importService.importar(bytes, moduloId, cursoAcademicoId);
      redirectAttributes.addFlashAttribute(
          "exito", totalCes + " criterios de evaluación importados correctamente.");
      return "redirect:/admin/modulos";
    } catch (ModuloImportException e) {
      redirectAttributes.addFlashAttribute("errores", e.getErrores());
      return "redirect:/admin/modulos/" + moduloId + "/importar-ras";
    } catch (IllegalStateException | EntityNotFoundException e) {
      redirectAttributes.addFlashAttribute("error", e.getMessage());
      return "redirect:/admin/modulos/" + moduloId + "/importar-ras";
    } catch (IOException e) {
      redirectAttributes.addFlashAttribute("error", "No se pudo leer el archivo subido.");
      return "redirect:/admin/modulos/" + moduloId + "/importar-ras";
    }
  }

  private boolean esXlsx(MultipartFile archivo) {
    String nombre = archivo.getOriginalFilename();
    if (nombre == null || !nombre.toLowerCase(Locale.ROOT).endsWith(".xlsx")) return false;
    String contentType = archivo.getContentType();
    return contentType != null && MIME_XLSX.contains(contentType);
  }
}
