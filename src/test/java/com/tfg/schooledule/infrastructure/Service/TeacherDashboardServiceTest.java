package com.tfg.schooledule.infrastructure.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.tfg.schooledule.domain.dto.*;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.domain.enums.TipoActividad;
import com.tfg.schooledule.infrastructure.mapper.TeacherDashboardMapper;
import com.tfg.schooledule.infrastructure.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class TeacherDashboardServiceTest {

  @Mock private ImparticionRepository imparticionRepo;
  @Mock private MatriculaRepository matriculaRepo;
  @Mock private ItemEvaluableRepository itemEvaluableRepo;
  @Mock private CalificacionRepository calificacionRepo;
  @Mock private TeacherDashboardMapper mapper;
  @Mock private EntityManager entityManager;
  @Mock private jakarta.persistence.Query nativeQuery;

  @InjectMocks private TeacherDashboardService service;

  private Usuario profe;
  private Centro centro;
  private Imparticion imparticion;
  private Matricula matricula;

  @BeforeEach
  void setUp() {
    profe = Usuario.builder().id(2).email("juan@tfg.com").nombre("Juan").apellidos("G").build();

    centro = Centro.builder().id(1).nombre("IES Central").ubicacion("Madrid").build();
    profe.setCentros(Set.of(centro));

    Modulo mod = Modulo.builder().id(1).codigo("M1").nombre("Modulo1").build();
    CursoAcademico curso =
        CursoAcademico.builder()
            .id(1)
            .nombre("2025/2026")
            .fechaInicio(LocalDate.now())
            .fechaFin(LocalDate.now().plusYears(1))
            .build();
    Grupo grupo =
        Grupo.builder().id(1).nombre("DAW1-A").centro(centro).cursoAcademico(curso).build();

    imparticion =
        Imparticion.builder().id(1).modulo(mod).grupo(grupo).profesor(profe).centro(centro).build();

    Usuario alumno =
        Usuario.builder().id(3).nombre("Ana").apellidos("Lopez").email("ana@t.com").build();
    matricula =
        Matricula.builder()
            .id(1)
            .alumno(alumno)
            .imparticion(imparticion)
            .centro(centro)
            .estado(EstadoMatricula.ACTIVA)
            .build();
  }

  @Test
  void getCentersForTeacher_devuelveSoloCentrosVinculados() {
    when(mapper.toCenterDto(eq(centro), eq(1L)))
        .thenReturn(new TeacherCenterDTO(1, "IES Central", "Madrid", 1L));
    when(imparticionRepo.findByProfesorIdAndCentroId(2, 1)).thenReturn(List.of(imparticion));

    List<TeacherCenterDTO> result = service.getCentersForTeacher(profe);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).nombre()).isEqualTo("IES Central");
    assertThat(result.get(0).imparticionesCount()).isEqualTo(1L);
  }

  @Test
  void getSubjectsForTeacherAndCenter_filtraPorProfesorYCentro() {
    when(imparticionRepo.findByProfesorIdAndCentroId(2, 1)).thenReturn(List.of(imparticion));
    when(matriculaRepo.findByImparticionIdAndEstado(1, EstadoMatricula.ACTIVA))
        .thenReturn(List.of(matricula));
    when(mapper.toSubjectDto(eq(imparticion), eq(1L)))
        .thenReturn(new TeacherSubjectDTO(1, "M1", "Modulo1", "DAW1-A", "2025/2026", 1L));

    List<TeacherSubjectDTO> result = service.getSubjectsForTeacherAndCenter(2, 1);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).moduloCodigo()).isEqualTo("M1");
  }

  @Test
  void getRosterForImparticion_lanzaAccessDenied_siProfesorNoEsPropietario() {
    when(imparticionRepo.existsByIdAndProfesorId(1, 2)).thenReturn(false);

    assertThatThrownBy(() -> service.getRosterForImparticion(2, 1))
        .isInstanceOf(AccessDeniedException.class);
  }

  @Test
  void getStudentGrades_agrupaItemsPorPeriodoYCalculaMedias() {
    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));

    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .peso(new BigDecimal("100.00"))
            .cerrado(false)
            .build();
    ItemEvaluable item1 =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .nombre("Examen")
            .tipo(TipoActividad.EXAMEN)
            .fecha(LocalDate.now())
            .build();
    ItemEvaluable item2 =
        ItemEvaluable.builder()
            .id(2)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .nombre("Practica")
            .tipo(TipoActividad.PRACTICA)
            .fecha(LocalDate.now())
            .build();

    Calificacion cal1 =
        Calificacion.builder()
            .id(1)
            .matricula(matricula)
            .itemEvaluable(item1)
            .valor(new BigDecimal("8.00"))
            .build();

    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(item1, item2));
    when(calificacionRepo.findByMatriculaIdAndItemEvaluableId(1, 1)).thenReturn(Optional.of(cal1));
    when(calificacionRepo.findByMatriculaIdAndItemEvaluableId(1, 2)).thenReturn(Optional.empty());
    when(mapper.toGradeItem(eq(item1), eq(cal1)))
        .thenReturn(
            new TeacherGradeItemDTO(
                1, "Examen", "EXAMEN", LocalDate.now(), new BigDecimal("8.00"), null, 1));
    when(mapper.toGradeItem(eq(item2), isNull()))
        .thenReturn(
            new TeacherGradeItemDTO(2, "Practica", "PRACTICA", LocalDate.now(), null, null, null));

    TeacherStudentGradesDTO result = service.getStudentGrades(2, 1);

    assertThat(result.periodos()).hasSize(1);
    // media del periodo: solo item1 tiene nota → 8.00
    assertThat(result.periodos().get(0).media()).isEqualByComparingTo("8.00");
    // mediaGlobal = media del único periodo con nota
    assertThat(result.mediaGlobal()).isEqualByComparingTo("8.00");
  }

  @Test
  void upsertGrades_creaCalificacionNueva_cuandoNoExiste() {
    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(1, new BigDecimal("9.00"), "Muy bien")));

    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));

    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .peso(new BigDecimal("100.00"))
            .cerrado(false)
            .build();
    ItemEvaluable item =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodo)
            .nombre("Examen")
            .tipo(TipoActividad.EXAMEN)
            .fecha(LocalDate.now())
            .build();

    when(itemEvaluableRepo.findById(1)).thenReturn(Optional.of(item));
    when(calificacionRepo.findByMatriculaIdAndItemEvaluableId(1, 1)).thenReturn(Optional.empty());
    when(calificacionRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

    // Para recompute post-save
    when(itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(1))
        .thenReturn(List.of(item));
    when(calificacionRepo.findByMatriculaIdAndItemEvaluableId(1, 1))
        .thenReturn(
            Optional.of(
                Calificacion.builder()
                    .id(1)
                    .matricula(matricula)
                    .itemEvaluable(item)
                    .valor(new BigDecimal("9.00"))
                    .build()));
    when(mapper.toGradeItem(any(), any()))
        .thenReturn(
            new TeacherGradeItemDTO(
                1, "Examen", "EXAMEN", LocalDate.now(), new BigDecimal("9.00"), "Muy bien", 1));

    // set_config debe llamarse para inyectar el usuario de auditoría
    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
    when(nativeQuery.getSingleResult()).thenReturn("juan@tfg.com");

    TeacherStudentGradesDTO result = service.upsertGrades(2, "juan@tfg.com", req);

    assertThat(result).isNotNull();
    verify(entityManager).createNativeQuery("SELECT set_config('app.usuario_actual', :u, true)");
    verify(nativeQuery).setParameter("u", "juan@tfg.com");
    verify(calificacionRepo).save(any(Calificacion.class));
  }

  @Test
  void upsertGrades_rechazaCuandoPeriodoCerrado() {
    PeriodoEvaluacion periodoCerrado =
        PeriodoEvaluacion.builder()
            .id(1)
            .imparticion(imparticion)
            .nombre("P1")
            .cerrado(true)
            .build();
    ItemEvaluable item =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(imparticion)
            .periodoEvaluacion(periodoCerrado)
            .nombre("Examen")
            .tipo(TipoActividad.EXAMEN)
            .build();

    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(1, new BigDecimal("7.00"), null)));

    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));
    when(itemEvaluableRepo.findById(1)).thenReturn(Optional.of(item));
    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
    when(nativeQuery.getSingleResult()).thenReturn("juan@tfg.com");

    assertThatThrownBy(() -> service.upsertGrades(2, "juan@tfg.com", req))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("cerrado");
  }

  @Test
  void upsertGrades_rechazaCuandoItemNoEsDeEsaImparticion() {
    Imparticion otraImparticion = Imparticion.builder().id(99).build();
    PeriodoEvaluacion periodo =
        PeriodoEvaluacion.builder()
            .id(2)
            .imparticion(otraImparticion)
            .nombre("P2")
            .cerrado(false)
            .build();
    ItemEvaluable itemAjeno =
        ItemEvaluable.builder()
            .id(1)
            .imparticion(otraImparticion)
            .periodoEvaluacion(periodo)
            .nombre("Examen Ajeno")
            .tipo(TipoActividad.EXAMEN)
            .build();

    GradeUpsertRequest req =
        new GradeUpsertRequest(
            1, List.of(new GradeUpsertRequest.Entry(1, new BigDecimal("5.00"), null)));

    when(matriculaRepo.findByIdAndImparticionProfesorId(1, 2)).thenReturn(Optional.of(matricula));
    when(itemEvaluableRepo.findById(1)).thenReturn(Optional.of(itemAjeno));
    when(entityManager.createNativeQuery(anyString())).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(anyString(), anyString())).thenReturn(nativeQuery);
    when(nativeQuery.getSingleResult()).thenReturn("juan@tfg.com");

    assertThatThrownBy(() -> service.upsertGrades(2, "juan@tfg.com", req))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
