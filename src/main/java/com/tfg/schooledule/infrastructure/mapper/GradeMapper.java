package com.tfg.schooledule.infrastructure.mapper;

import com.tfg.schooledule.domain.dto.GradeDTO;
import com.tfg.schooledule.domain.entity.Calificacion;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface GradeMapper {

  @Mapping(target = "itemNombre", source = "itemEvaluable.nombre")
  @Mapping(target = "fecha", source = "itemEvaluable.fecha")
  @Mapping(
      target = "tipoActividad",
      expression = "java(calificacion.getItemEvaluable().getTipo().name())")
  GradeDTO toDto(Calificacion calificacion);

  List<GradeDTO> toDtoList(List<Calificacion> calificaciones);
}
