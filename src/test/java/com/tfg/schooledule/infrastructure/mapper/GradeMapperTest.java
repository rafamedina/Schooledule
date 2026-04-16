package com.tfg.schooledule.infrastructure.mapper;

import static org.junit.jupiter.api.Assertions.*;

import com.tfg.schooledule.domain.dto.GradeDTO;
import com.tfg.schooledule.domain.entity.Calificacion;
import com.tfg.schooledule.domain.entity.ItemEvaluable;
import com.tfg.schooledule.domain.enums.TipoActividad;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class GradeMapperTest {

  private final GradeMapper mapper = Mappers.getMapper(GradeMapper.class);

  @Test
  void toDto_mapsItemEvaluableFields() {
    LocalDate today = LocalDate.now();
    ItemEvaluable item =
        ItemEvaluable.builder()
            .nombre("Examen Final")
            .tipo(TipoActividad.EXAMEN)
            .fecha(today)
            .build();
    Calificacion calif =
        Calificacion.builder()
            .itemEvaluable(item)
            .valor(new BigDecimal("9.0"))
            .comentario("Muy bien")
            .build();

    GradeDTO dto = mapper.toDto(calif);

    assertEquals("Examen Final", dto.itemNombre());
    assertEquals(new BigDecimal("9.0"), dto.valor());
    assertEquals("Muy bien", dto.comentario());
    assertEquals(today, dto.fecha());
    assertEquals("EXAMEN", dto.tipoActividad());
  }

  @Test
  void toDtoList_convertsAllElements() {
    ItemEvaluable item = ItemEvaluable.builder().nombre("T1").tipo(TipoActividad.PRACTICA).build();
    List<Calificacion> list =
        List.of(
            Calificacion.builder().itemEvaluable(item).valor(BigDecimal.valueOf(7)).build(),
            Calificacion.builder().itemEvaluable(item).valor(BigDecimal.valueOf(8)).build());

    List<GradeDTO> dtos = mapper.toDtoList(list);

    assertEquals(2, dtos.size());
  }
}
