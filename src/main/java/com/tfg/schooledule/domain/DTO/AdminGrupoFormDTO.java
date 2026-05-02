package com.tfg.schooledule.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminGrupoFormDTO {

  private Integer id;

  @NotBlank
  @Size(max = 50)
  private String nombre;

  @NotNull @Positive private Integer centroId;

  @NotNull @Positive private Integer cursoAcademicoId;
}
