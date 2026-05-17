package com.tfg.schooledule.infrastructure.service;

import com.tfg.schooledule.domain.dto.UsuarioImportErrorDTO;
import com.tfg.schooledule.domain.dto.UsuarioImportPreviewDTO;
import com.tfg.schooledule.domain.dto.UsuarioImportRowDTO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class UsuarioImportValidatorService {

  private static final int MAX_USUARIOS = 200;
  private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

  public UsuarioImportPreviewDTO validar(List<UsuarioImportRowDTO> filas) {
    List<UsuarioImportErrorDTO> errores = new ArrayList<>();

    if (filas.isEmpty()) {
      errores.add(new UsuarioImportErrorDTO(0, "archivo", "El archivo no contiene filas de datos"));
      return new UsuarioImportPreviewDTO(false, errores, 0);
    }

    if (filas.size() > MAX_USUARIOS) {
      errores.add(
          new UsuarioImportErrorDTO(
              0,
              "archivo",
              "El archivo supera el límite de " + MAX_USUARIOS + " usuarios por importación"));
      return new UsuarioImportPreviewDTO(false, errores, filas.size());
    }

    validarDuplicadosEnLote(filas, errores);
    filas.forEach(fila -> validarCampos(fila, errores));

    return new UsuarioImportPreviewDTO(errores.isEmpty(), errores, filas.size());
  }

  private void validarCampos(UsuarioImportRowDTO fila, List<UsuarioImportErrorDTO> errores) {
    int f = fila.numeroFila();

    // username
    if (fila.username() == null || fila.username().isBlank()) {
      errores.add(new UsuarioImportErrorDTO(f, "username", "El nombre de usuario es obligatorio"));
    } else if (fila.username().contains(" ")) {
      errores.add(
          new UsuarioImportErrorDTO(
              f, "username", "El nombre de usuario no puede contener espacios"));
    } else if (fila.username().length() > 50) {
      errores.add(
          new UsuarioImportErrorDTO(
              f, "username", "El nombre de usuario no puede superar 50 caracteres"));
    }

    // nombre
    if (fila.nombre() == null || fila.nombre().isBlank()) {
      errores.add(new UsuarioImportErrorDTO(f, "nombre", "El nombre es obligatorio"));
    } else if (fila.nombre().length() > 100) {
      errores.add(
          new UsuarioImportErrorDTO(f, "nombre", "El nombre no puede superar 100 caracteres"));
    }

    // apellidos
    if (fila.apellidos() == null || fila.apellidos().isBlank()) {
      errores.add(new UsuarioImportErrorDTO(f, "apellidos", "Los apellidos son obligatorios"));
    } else if (fila.apellidos().length() > 100) {
      errores.add(
          new UsuarioImportErrorDTO(
              f, "apellidos", "Los apellidos no pueden superar 100 caracteres"));
    }

    // email (opcional)
    if (fila.email() != null && !fila.email().isBlank()) {
      if (fila.email().length() > 150) {
        errores.add(
            new UsuarioImportErrorDTO(f, "email", "El email no puede superar 150 caracteres"));
      } else if (!EMAIL_PATTERN.matcher(fila.email()).matches()) {
        errores.add(new UsuarioImportErrorDTO(f, "email", "El email no tiene un formato válido"));
      }
    }

    // password
    if (fila.password() == null || fila.password().isBlank()) {
      errores.add(new UsuarioImportErrorDTO(f, "password", "La contraseña es obligatoria"));
    } else {
      validarPassword(f, fila.password(), errores);
    }

    // centro_nombre
    if (fila.centroNombre() == null || fila.centroNombre().isBlank()) {
      errores.add(
          new UsuarioImportErrorDTO(f, "centro_nombre", "El nombre del centro es obligatorio"));
    }

    // curso_academico_nombre
    if (fila.cursoAcademicoNombre() == null || fila.cursoAcademicoNombre().isBlank()) {
      errores.add(
          new UsuarioImportErrorDTO(
              f, "curso_academico_nombre", "El nombre del curso académico es obligatorio"));
    }

    // grupo_nombre
    if (fila.grupoNombre() == null || fila.grupoNombre().isBlank()) {
      errores.add(
          new UsuarioImportErrorDTO(f, "grupo_nombre", "El nombre del grupo es obligatorio"));
    }
  }

  private void validarPassword(int fila, String password, List<UsuarioImportErrorDTO> errores) {
    if (password.length() < 8) {
      errores.add(
          new UsuarioImportErrorDTO(
              fila, "password", "La contraseña debe tener al menos 8 caracteres"));
      return;
    }
    if (!password.matches(".*[A-Z].*")) {
      errores.add(
          new UsuarioImportErrorDTO(
              fila, "password", "La contraseña debe contener al menos una letra mayúscula"));
    }
    if (!password.matches(".*[a-z].*")) {
      errores.add(
          new UsuarioImportErrorDTO(
              fila, "password", "La contraseña debe contener al menos una letra minúscula"));
    }
    if (!password.matches(".*[0-9].*")) {
      errores.add(
          new UsuarioImportErrorDTO(
              fila, "password", "La contraseña debe contener al menos un dígito"));
    }
  }

  private void validarDuplicadosEnLote(
      List<UsuarioImportRowDTO> filas, List<UsuarioImportErrorDTO> errores) {
    Map<String, Integer> usernamesVistos = new HashMap<>();
    Map<String, Integer> emailsVistos = new HashMap<>();

    for (UsuarioImportRowDTO fila : filas) {
      if (fila.username() != null && !fila.username().isBlank()) {
        String key = fila.username().toLowerCase();
        if (usernamesVistos.containsKey(key)) {
          errores.add(
              new UsuarioImportErrorDTO(
                  fila.numeroFila(),
                  "username",
                  "Username '"
                      + fila.username()
                      + "' duplicado en el lote (filas "
                      + usernamesVistos.get(key)
                      + " y "
                      + fila.numeroFila()
                      + ")"));
        } else {
          usernamesVistos.put(key, fila.numeroFila());
        }
      }

      if (fila.email() != null && !fila.email().isBlank()) {
        String key = fila.email().toLowerCase();
        if (emailsVistos.containsKey(key)) {
          errores.add(
              new UsuarioImportErrorDTO(
                  fila.numeroFila(),
                  "email",
                  "Email '"
                      + fila.email()
                      + "' duplicado en el lote (filas "
                      + emailsVistos.get(key)
                      + " y "
                      + fila.numeroFila()
                      + ")"));
        } else {
          emailsVistos.put(key, fila.numeroFila());
        }
      }
    }
  }
}
