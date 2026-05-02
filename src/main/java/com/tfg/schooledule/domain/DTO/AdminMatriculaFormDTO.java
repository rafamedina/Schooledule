package com.tfg.schooledule.domain.dto;

import com.tfg.schooledule.domain.enums.EstadoMatricula;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminMatriculaFormDTO {

  private Integer id;

  @NotNull @Positive private Integer imparticionId;

  @NotNull private EstadoMatricula estado;

  @NotNull private Boolean esRepetidor;
}
