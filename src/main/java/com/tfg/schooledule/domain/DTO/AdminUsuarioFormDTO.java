package com.tfg.schooledule.domain.dto;

import com.tfg.schooledule.domain.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUsuarioFormDTO {

  private Integer id;

  @NotBlank
  @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$")
  private String username;

  @NotBlank
  @Size(max = 100)
  private String nombre;

  @NotBlank
  @Size(max = 100)
  private String apellidos;

  @NotBlank
  @Email
  @Size(max = 254)
  private String email;

  @ValidPassword private String password;

  private Set<Integer> roleIds = new HashSet<>();
  private Set<Integer> centroIds = new HashSet<>();
}
