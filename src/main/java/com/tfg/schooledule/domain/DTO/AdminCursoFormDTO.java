package com.tfg.schooledule.domain.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCursoFormDTO {

  private Integer id;

  @NotBlank
  @Size(max = 20)
  private String nombre;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaInicio;

  @NotNull
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate fechaFin;

  @AssertTrue(message = "La fecha de fin debe ser posterior a la fecha de inicio")
  public boolean isFechaFinValida() {
    if (fechaInicio == null || fechaFin == null) return true;
    return fechaFin.isAfter(fechaInicio);
  }
}
