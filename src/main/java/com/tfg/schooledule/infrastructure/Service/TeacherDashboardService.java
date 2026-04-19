package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.*;
import com.tfg.schooledule.domain.entity.*;
import com.tfg.schooledule.domain.enums.EstadoMatricula;
import com.tfg.schooledule.infrastructure.mapper.TeacherDashboardMapper;
import com.tfg.schooledule.infrastructure.repository.*;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TeacherDashboardService {

  private final ImparticionRepository imparticionRepo;
  private final MatriculaRepository matriculaRepo;
  private final ItemEvaluableRepository itemEvaluableRepo;
  private final CalificacionRepository calificacionRepo;
  private final TeacherDashboardMapper mapper;
  private final EntityManager entityManager;

  public TeacherDashboardService(
      ImparticionRepository imparticionRepo,
      MatriculaRepository matriculaRepo,
      ItemEvaluableRepository itemEvaluableRepo,
      CalificacionRepository calificacionRepo,
      TeacherDashboardMapper mapper,
      EntityManager entityManager) {
    this.imparticionRepo = imparticionRepo;
    this.matriculaRepo = matriculaRepo;
    this.itemEvaluableRepo = itemEvaluableRepo;
    this.calificacionRepo = calificacionRepo;
    this.mapper = mapper;
    this.entityManager = entityManager;
  }

  /** Devuelve los centros del profesor con el conteo de sus imparticiones activas. */
  public List<TeacherCenterDTO> getCentersForTeacher(Usuario profesor) {
    return profesor.getCentros().stream()
        .map(
            centro -> {
              long count =
                  imparticionRepo
                      .findByProfesorIdAndCentroId(profesor.getId(), centro.getId())
                      .size();
              return mapper.toCenterDto(centro, count);
            })
        .sorted(Comparator.comparing(TeacherCenterDTO::nombre))
        .collect(Collectors.toList());
  }

  /** Lista las asignaturas (imparticiones) de un profesor en un centro concreto. */
  public List<TeacherSubjectDTO> getSubjectsForTeacherAndCenter(
      Integer profesorId, Integer centroId) {
    return imparticionRepo.findByProfesorIdAndCentroId(profesorId, centroId).stream()
        .map(
            imp -> {
              long alumnosCount =
                  matriculaRepo
                      .findByImparticionIdAndEstado(imp.getId(), EstadoMatricula.ACTIVA)
                      .size();
              return mapper.toSubjectDto(imp, alumnosCount);
            })
        .collect(Collectors.toList());
  }

  /** Devuelve el listado de alumnos activos de una imparticion. Verifica propiedad. */
  public List<TeacherStudentRowDTO> getRosterForImparticion(
      Integer profesorId, Integer imparticionId) {
    if (!imparticionRepo.existsByIdAndProfesorId(imparticionId, profesorId)) {
      throw new AccessDeniedException(
          "El profesor no tiene acceso a la impartición " + imparticionId);
    }
    return matriculaRepo
        .findByImparticionIdAndEstado(imparticionId, EstadoMatricula.ACTIVA)
        .stream()
        .map(mapper::toStudentRow)
        .collect(Collectors.toList());
  }

  /** Construye el DTO completo de notas de un alumno agrupadas por periodo. Verifica propiedad. */
  public TeacherStudentGradesDTO getStudentGrades(Integer profesorId, Integer matriculaId) {
    Matricula matricula = loadMatriculaWithOwnershipCheck(profesorId, matriculaId);
    return buildGradesDTO(matricula);
  }

  /**
   * Upsert de calificaciones. Establece app.current_user para que el trigger de auditoría registre
   * al profesor responsable.
   */
  @Transactional
  public TeacherStudentGradesDTO upsertGrades(
      Integer profesorId, String profesorEmail, GradeUpsertRequest req) {
    Matricula matricula = loadMatriculaWithOwnershipCheck(profesorId, req.matriculaId());

    // Inyecta el email del profesor en la sesión de Postgres para el trigger de auditoría.
    // SET LOCAL no acepta bind params; set_config(name, value, is_local=true) sí los admite.
    entityManager
        .createNativeQuery("SELECT set_config('app.usuario_actual', :u, true)")
        .setParameter("u", profesorEmail)
        .getSingleResult();

    for (GradeUpsertRequest.Entry entry : req.entries()) {
      ItemEvaluable item =
          itemEvaluableRepo
              .findById(entry.itemEvaluableId())
              .orElseThrow(
                  () ->
                      new IllegalArgumentException(
                          "Item evaluable no encontrado: " + entry.itemEvaluableId()));

      // El item debe pertenecer a la impartición de la matrícula
      if (!item.getImparticion().getId().equals(matricula.getImparticion().getId())) {
        throw new IllegalArgumentException(
            "El item " + entry.itemEvaluableId() + " no pertenece a esta impartición");
      }

      // No se puede modificar un periodo cerrado
      if (Boolean.TRUE.equals(item.getPeriodoEvaluacion().getCerrado())) {
        throw new IllegalStateException(
            "El periodo '"
                + item.getPeriodoEvaluacion().getNombre()
                + "' está cerrado y no admite cambios");
      }

      Optional<Calificacion> existing =
          calificacionRepo.findByMatriculaIdAndItemEvaluableId(matricula.getId(), item.getId());

      if (existing.isPresent()) {
        Calificacion cal = existing.get();
        cal.setValor(entry.valor());
        cal.setComentario(entry.comentario());
        calificacionRepo.save(cal);
      } else {
        calificacionRepo.save(
            Calificacion.builder()
                .matricula(matricula)
                .itemEvaluable(item)
                .valor(entry.valor())
                .comentario(entry.comentario())
                .build());
      }
    }

    return buildGradesDTO(matricula);
  }

  /** Devuelve el centro_id de una imparticion verificando que pertenece al profesor. */
  public Integer getCentroIdByImparticion(Integer profesorId, Integer imparticionId) {
    return imparticionRepo
        .findById(imparticionId)
        .filter(i -> i.getProfesor().getId().equals(profesorId))
        .map(i -> i.getCentro().getId())
        .orElseThrow(
            () ->
                new AccessDeniedException(
                    "El profesor no tiene acceso a la impartición " + imparticionId));
  }

  // ─── helpers privados ────────────────────────────────────────────────────────

  private Matricula loadMatriculaWithOwnershipCheck(Integer profesorId, Integer matriculaId) {
    return matriculaRepo
        .findByIdAndImparticionProfesorId(matriculaId, profesorId)
        .orElseThrow(
            () ->
                new AccessDeniedException(
                    "El profesor no tiene acceso a la matrícula " + matriculaId));
  }

  private TeacherStudentGradesDTO buildGradesDTO(Matricula matricula) {
    Integer imparticionId = matricula.getImparticion().getId();
    List<ItemEvaluable> items =
        itemEvaluableRepo.findByImparticionIdOrderByPeriodoEvaluacionIdAscFechaAsc(imparticionId);

    // Agrupa items por periodo
    Map<PeriodoEvaluacion, List<ItemEvaluable>> byPeriodo = new LinkedHashMap<>();
    for (ItemEvaluable item : items) {
      byPeriodo.computeIfAbsent(item.getPeriodoEvaluacion(), k -> new ArrayList<>()).add(item);
    }

    List<TeacherPeriodoGradesDTO> periodos = new ArrayList<>();
    for (Map.Entry<PeriodoEvaluacion, List<ItemEvaluable>> entry : byPeriodo.entrySet()) {
      PeriodoEvaluacion periodo = entry.getKey();
      List<TeacherGradeItemDTO> gradeItems = new ArrayList<>();

      for (ItemEvaluable item : entry.getValue()) {
        Calificacion existing =
            calificacionRepo
                .findByMatriculaIdAndItemEvaluableId(matricula.getId(), item.getId())
                .orElse(null);
        gradeItems.add(mapper.toGradeItem(item, existing));
      }

      BigDecimal media = computeMedia(gradeItems);
      periodos.add(
          new TeacherPeriodoGradesDTO(
              periodo.getId(),
              periodo.getNombre(),
              periodo.getPeso(),
              Boolean.TRUE.equals(periodo.getCerrado()),
              gradeItems,
              media));
    }

    BigDecimal mediaGlobal = computeMediaGlobal(periodos);
    String alumnoNombre =
        matricula.getAlumno().getNombre() + " " + matricula.getAlumno().getApellidos();
    String label =
        matricula.getImparticion().getGrupo().getNombre()
            + " · "
            + matricula.getImparticion().getModulo().getNombre();

    return new TeacherStudentGradesDTO(
        matricula.getId(), alumnoNombre, label, periodos, mediaGlobal);
  }

  /** Media aritmética de los valores no nulos de un periodo (2 decimales HALF_UP). */
  private BigDecimal computeMedia(List<TeacherGradeItemDTO> items) {
    List<BigDecimal> notas =
        items.stream()
            .map(TeacherGradeItemDTO::valor)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    if (notas.isEmpty()) return null;
    BigDecimal suma = notas.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    return suma.divide(new BigDecimal(notas.size()), 2, RoundingMode.HALF_UP);
  }

  /** Media global ponderada por el peso de cada periodo (Σ media_i * peso_i / Σ peso_i). */
  private BigDecimal computeMediaGlobal(List<TeacherPeriodoGradesDTO> periodos) {
    List<TeacherPeriodoGradesDTO> conNota =
        periodos.stream().filter(p -> p.media() != null).collect(Collectors.toList());
    if (conNota.isEmpty()) return null;

    BigDecimal sumaPonderada = BigDecimal.ZERO;
    BigDecimal sumaPesos = BigDecimal.ZERO;

    for (TeacherPeriodoGradesDTO p : conNota) {
      // Si no tiene peso definido, se trata como 1
      BigDecimal peso = p.peso() != null ? p.peso() : BigDecimal.ONE;
      sumaPonderada = sumaPonderada.add(p.media().multiply(peso));
      sumaPesos = sumaPesos.add(peso);
    }

    if (sumaPesos.compareTo(BigDecimal.ZERO) == 0) return null;
    return sumaPonderada.divide(sumaPesos, 2, RoundingMode.HALF_UP);
  }
}
