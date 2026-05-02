package com.tfg.schooledule.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCentroFormDTO {

  private Integer id;

  @NotBlank
  @Size(max = 100)
  private String nombre;

  @Size(max = 200)
  private String ubicacion;
}
