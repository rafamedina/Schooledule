package com.tfg.schooledule.infrastructure.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.tfg.schooledule.domain.dto.ModuloImportErrorDTO;
import com.tfg.schooledule.domain.exception.ModuloImportException;
import com.tfg.schooledule.infrastructure.repository.CursoAcademicoRepository;
import com.tfg.schooledule.infrastructure.security.SecurityAuditLogger;
import com.tfg.schooledule.infrastructure.service.AdminCursoActivoService;
import com.tfg.schooledule.infrastructure.service.ModuloImportService;
import com.tfg.schooledule.infrastructure.service.ModuloPlantillaExcelService;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AdminModuloImportController.class)
@Import(AdminModuloImportControllerTest.MethodSecurityTestConfig.class)
class AdminModuloImportControllerTest {

  @EnableMethodSecurity
  static class MethodSecurityTestConfig {}

  @Autowired private MockMvc mockMvc;

  @MockBean private ModuloImportService importService;
  @MockBean private ModuloPlantillaExcelService plantillaService;
  @MockBean private CursoAcademicoRepository cursoAcademicoRepository;
  @MockBean private AdminCursoActivoService adminCursoActivoService;
  @MockBean private SecurityAuditLogger securityAuditLogger;

  private static final String XLSX_MIME =
      "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
  private static final byte[] BYTES_DUMMY = new byte[] {1, 2, 3, 4};

  private MockMultipartFile xlsxFile() {
    return new MockMultipartFile("archivo", "plantilla.xlsx", XLSX_MIME, BYTES_DUMMY);
  }

  // ── Plantilla ────────────────────────────────────────────────────────────

  @Test
  void descargarPlantilla_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/modulos/plantilla-excel").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void descargarPlantilla_conAlumno_403() throws Exception {
    mockMvc.perform(get("/admin/modulos/plantilla-excel")).andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void descargarPlantilla_conAdmin_200_contentTypeExcel() throws Exception {
    when(plantillaService.generarPlantilla()).thenReturn(BYTES_DUMMY);

    mockMvc
        .perform(get("/admin/modulos/plantilla-excel"))
        .andExpect(status().isOk())
        .andExpect(
            header()
                .string(
                    "Content-Type",
                    org.hamcrest.Matchers.containsString(
                        "openxmlformats-officedocument.spreadsheetml.sheet")));
  }

  // ── Formulario ───────────────────────────────────────────────────────────

  @Test
  void formulario_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(get("/admin/modulos/1/importar-ras").header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void formulario_conAdmin_200_contieneModuloId() throws Exception {
    when(cursoAcademicoRepository.findAllByOrderByFechaInicioDesc())
        .thenReturn(Collections.emptyList());

    mockMvc
        .perform(get("/admin/modulos/1/importar-ras"))
        .andExpect(status().isOk())
        .andExpect(model().attribute("moduloId", 1));
  }

  // ── Importar ─────────────────────────────────────────────────────────────

  @Test
  void importar_sinAuth_redirige302() throws Exception {
    mockMvc
        .perform(
            multipart("/admin/modulos/1/importar-ras")
                .file(xlsxFile())
                .param("cursoAcademicoId", "5")
                .with(csrf())
                .header("Accept", "text/html"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrlPattern("**/login"));
  }

  @Test
  @WithMockUser(roles = "ALUMNO")
  void importar_conAlumno_403() throws Exception {
    mockMvc
        .perform(
            multipart("/admin/modulos/1/importar-ras")
                .file(xlsxFile())
                .param("cursoAcademicoId", "5")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "PROFESOR")
  void importar_conProfesor_403() throws Exception {
    mockMvc
        .perform(
            multipart("/admin/modulos/1/importar-ras")
                .file(xlsxFile())
                .param("cursoAcademicoId", "5")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_archivoNoXlsx_extensionCsv_redirigeConFlashError() throws Exception {
    MockMultipartFile csv = new MockMultipartFile("archivo", "datos.csv", "text/csv", BYTES_DUMMY);

    mockMvc
        .perform(
            multipart("/admin/modulos/1/importar-ras")
                .file(csv)
                .param("cursoAcademicoId", "5")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos/1/importar-ras"))
        .andExpect(flash().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_archivoNoXlsx_contentTypePdf_redirigeConFlashError() throws Exception {
    MockMultipartFile pdf =
        new MockMultipartFile(
            "archivo", "documento.xlsx", MediaType.APPLICATION_PDF_VALUE, BYTES_DUMMY);

    mockMvc
        .perform(
            multipart("/admin/modulos/1/importar-ras")
                .file(pdf)
                .param("cursoAcademicoId", "5")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos/1/importar-ras"))
        .andExpect(flash().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_archivoVacio_redirigeConFlashError() throws Exception {
    MockMultipartFile vacio =
        new MockMultipartFile("archivo", "vacio.xlsx", XLSX_MIME, new byte[0]);

    mockMvc
        .perform(
            multipart("/admin/modulos/1/importar-ras")
                .file(vacio)
                .param("cursoAcademicoId", "5")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos/1/importar-ras"))
        .andExpect(flash().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_archivoValido_delegaAlServicioYRedirige() throws Exception {
    when(importService.importar(any(), eq(1), eq(5))).thenReturn(12);

    mockMvc
        .perform(
            multipart("/admin/modulos/1/importar-ras")
                .file(xlsxFile())
                .param("cursoAcademicoId", "5")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos"))
        .andExpect(flash().attributeExists("exito"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_servicioLanzaModuloImportException_redirigeConErrores() throws Exception {
    List<ModuloImportErrorDTO> errores =
        List.of(new ModuloImportErrorDTO(1, "ra_codigo", "El código de RA es obligatorio"));
    when(importService.importar(any(), eq(1), eq(5))).thenThrow(new ModuloImportException(errores));

    mockMvc
        .perform(
            multipart("/admin/modulos/1/importar-ras")
                .file(xlsxFile())
                .param("cursoAcademicoId", "5")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos/1/importar-ras"))
        .andExpect(flash().attributeExists("errores"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_servicioLanzaIllegalStateException_redirigeConFlashError() throws Exception {
    when(importService.importar(any(), eq(1), eq(5)))
        .thenThrow(new IllegalStateException("Ya existen RAs para este módulo y curso."));

    mockMvc
        .perform(
            multipart("/admin/modulos/1/importar-ras")
                .file(xlsxFile())
                .param("cursoAcademicoId", "5")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos/1/importar-ras"))
        .andExpect(flash().attributeExists("error"));
  }

  @Test
  @WithMockUser(roles = "ADMIN")
  void importar_servicioLanzaEntityNotFoundException_redirigeConFlashError() throws Exception {
    when(importService.importar(any(), eq(1), eq(5)))
        .thenThrow(new EntityNotFoundException("Módulo no encontrado"));

    mockMvc
        .perform(
            multipart("/admin/modulos/1/importar-ras")
                .file(xlsxFile())
                .param("cursoAcademicoId", "5")
                .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/admin/modulos/1/importar-ras"))
        .andExpect(flash().attributeExists("error"));
  }
}
