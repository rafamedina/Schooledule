package com.tfg.schooledule.domain.DTO;

import com.tfg.schooledule.domain.entity.Rol;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    private String username;
    private String nombre;
    private String apellidos;
    private String email;
    private boolean activo;
    //    private String password;
    private LocalDateTime fechaCreacion;
    private String roles;

}