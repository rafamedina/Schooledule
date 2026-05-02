package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.AdminAuditoriaListDTO;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.infrastructure.repository.AuditoriaNotaRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminAuditoriaServiceTest {

  @Mock private AuditoriaNotaRepository auditoriaNotaRepository;

  @InjectMocks private AdminAuditoriaService adminAuditoriaService;

  private AuditoriaNota buildAuditoriaNota() {
    Modulo modulo = Modulo.builder().id(1).codigo("DAW01").nombre("Programación").build();
    Centro centro = Centro.builder().id(1).nombre("Centro Test").build();
    Grupo grupo = Grupo.builder().id(1).nombre("1DAW-A").build();
    Usuario profesor =
        Usuario.builder().id(2).nombre("Ana").apellidos("López").email("ana@test.com").build();
    Imparticion imparticion =
        Imparticion.builder()
            .id(1)
            .modulo(modulo)
            .grupo(grupo)
            .profesor(profesor)
            .centro(centro)
            .build();
    Usuario alumno =
        Usuario.builder().id(3).nombre("Juan").apellidos("García").email("juan@test.com").build();
    Matricula matricula =
        Matricula.builder().id(1).alumno(alumno).imparticion(imparticion).centro(centro).build();
    CriterioEvaluacion criterio =
        CriterioEvaluacion.builder().id(1).codigo("CE1").descripcion("Criterio test").build();
    Calificacion calificacion =
        Calificacion.builder().id(1).matricula(matricula).criterioEvaluacion(criterio).build();
    return AuditoriaNota.builder()
        .id(10)
        .calificacion(calificacion)
        .valorAnterior(new BigDecimal("5.00"))
        .valorNuevo(new BigDecimal("7.50"))
        .usuarioResponsable("ana@test.com")
        .fechaCambio(LocalDateTime.of(2025, 3, 15, 10, 30))
        .motivo("Corrección de examen")
        .build();
  }

  @Test
  void buscar_sinFiltros_llamaRepositorioConNulos() {
    when(auditoriaNotaRepository.findWithFilters(null, null, null, null)).thenReturn(List.of());

    adminAuditoriaService.buscar(null, null, null, null);

    verify(auditoriaNotaRepository).findWithFilters(null, null, null, null);
  }

  @Test
  void buscar_conFechas_convierteALocalDateTime() {
    LocalDate desde = LocalDate.of(2025, 1, 1);
    LocalDate hasta = LocalDate.of(2025, 12, 31);

    ArgumentCaptor<LocalDateTime> desdeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
    ArgumentCaptor<LocalDateTime> hastaCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

    when(auditoriaNotaRepository.findWithFilters(isNull(), isNull(), any(), any()))
        .thenReturn(List.of());

    adminAuditoriaService.buscar(null, null, desde, hasta);

    verify(auditoriaNotaRepository)
        .findWithFilters(isNull(), isNull(), desdeCaptor.capture(), hastaCaptor.capture());

    assertThat(desdeCaptor.getValue()).isEqualTo(desde.atStartOfDay());
    assertThat(hastaCaptor.getValue()).isEqualTo(hasta.atTime(23, 59, 59));
  }

  @Test
  void buscar_repositorioRetornaEntidad_mapeaCorrectamente() {
    AuditoriaNota entidad = buildAuditoriaNota();
    when(auditoriaNotaRepository.findWithFilters(any(), any(), any(), any()))
        .thenReturn(List.of(entidad));

    List<AdminAuditoriaListDTO> resultado = adminAuditoriaService.buscar(null, null, null, null);

    assertThat(resultado).hasSize(1);
    AdminAuditoriaListDTO dto = resultado.get(0);
    assertThat(dto.id()).isEqualTo(10);
    assertThat(dto.alumnoEmail()).isEqualTo("juan@test.com");
    assertThat(dto.moduloNombre()).isEqualTo("Programación");
    assertThat(dto.valorAnterior()).isEqualByComparingTo(new BigDecimal("5.00"));
    assertThat(dto.valorNuevo()).isEqualByComparingTo(new BigDecimal("7.50"));
    assertThat(dto.usuarioResponsable()).isEqualTo("ana@test.com");
    assertThat(dto.motivo()).isEqualTo("Corrección de examen");
  }
}
