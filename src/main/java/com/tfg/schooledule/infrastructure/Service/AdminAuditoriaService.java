package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.AdminAuditoriaListDTO;
import com.tfg.schooledule.domain.entity.AuditoriaNota;
import com.tfg.schooledule.infrastructure.repository.AuditoriaNotaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminAuditoriaService {

  private final AuditoriaNotaRepository auditoriaNotaRepository;

  public AdminAuditoriaService(AuditoriaNotaRepository auditoriaNotaRepository) {
    this.auditoriaNotaRepository = auditoriaNotaRepository;
  }

  @Transactional(readOnly = true)
  public List<AdminAuditoriaListDTO> buscar(
      String alumnoEmail, String moduloNombre, LocalDate fechaDesde, LocalDate fechaHasta) {

    LocalDateTime dtDesde = fechaDesde != null ? fechaDesde.atStartOfDay() : null;
    LocalDateTime dtHasta = fechaHasta != null ? fechaHasta.atTime(23, 59, 59) : null;

    return auditoriaNotaRepository
        .findWithFilters(alumnoEmail, moduloNombre, dtDesde, dtHasta)
        .stream()
        .map(this::toDTO)
        .toList();
  }

  private AdminAuditoriaListDTO toDTO(AuditoriaNota a) {
    return new AdminAuditoriaListDTO(
        a.getId(),
        a.getCalificacion().getMatricula().getAlumno().getEmail(),
        a.getCalificacion().getMatricula().getImparticion().getModulo().getNombre(),
        a.getValorAnterior(),
        a.getValorNuevo(),
        a.getUsuarioResponsable(),
        a.getFechaCambio(),
        a.getMotivo());
  }
}
