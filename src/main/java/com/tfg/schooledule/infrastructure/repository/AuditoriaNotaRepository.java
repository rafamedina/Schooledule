package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.AuditoriaNota;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditoriaNotaRepository extends JpaRepository<AuditoriaNota, Integer> {

  List<AuditoriaNota> findByCalificacionId(Integer calificacionId);

  @Query(
      """
      SELECT a FROM AuditoriaNota a
      JOIN a.calificacion c
      JOIN c.matricula m
      JOIN m.alumno al
      JOIN m.imparticion i
      JOIN i.modulo mod
      WHERE (:alumnoEmail IS NULL OR LOWER(al.email) LIKE LOWER(CONCAT('%', :alumnoEmail, '%')))
        AND (:moduloNombre IS NULL OR LOWER(mod.nombre) LIKE LOWER(CONCAT('%', :moduloNombre, '%')))
        AND (:fechaDesde IS NULL OR a.fechaCambio >= :fechaDesde)
        AND (:fechaHasta IS NULL OR a.fechaCambio <= :fechaHasta)
      ORDER BY a.fechaCambio DESC
      """)
  List<AuditoriaNota> findWithFilters(
      @Param("alumnoEmail") String alumnoEmail,
      @Param("moduloNombre") String moduloNombre,
      @Param("fechaDesde") LocalDateTime fechaDesde,
      @Param("fechaHasta") LocalDateTime fechaHasta);
}
