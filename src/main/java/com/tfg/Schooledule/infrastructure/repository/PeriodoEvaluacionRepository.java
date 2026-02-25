package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.PeriodoEvaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PeriodoEvaluacionRepository extends JpaRepository<PeriodoEvaluacion, Integer> {
    List<PeriodoEvaluacion> findByImparticionId(Integer imparticionId);
}
