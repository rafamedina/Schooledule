package com.tfg.schooledule.infrastructure.repository;

import com.tfg.schooledule.domain.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Integer> {

  boolean existsByCentroId(Integer centroId);

  long countByCentroId(Integer centroId);
}
