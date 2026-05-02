package com.tfg.schooledule.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminImparticionFormDTO {

  private Integer id;

  @NotNull @Positive private Integer moduloId;

  @NotNull @Positive private Integer grupoId;

  @NotNull @Positive private Integer profesorId;

  @NotNull @Positive private Integer centroId;
}
