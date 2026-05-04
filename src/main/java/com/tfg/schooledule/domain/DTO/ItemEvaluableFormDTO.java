package com.tfg.schooledule.domain.dto;

import com.tfg.schooledule.domain.enums.TipoActividad;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemEvaluableFormDTO {
  @NotBlank
  @Size(max = 200)
  private String nombre;

  @NotNull private TipoActividad tipo;

  @NotNull private LocalDate fecha;

  @NotNull @Positive private Integer periodoEvaluacionId;

  @NotNull @Positive private Integer resultadoAprendizajeId;
}
