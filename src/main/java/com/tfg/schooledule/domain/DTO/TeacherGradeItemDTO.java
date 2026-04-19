package com.tfg.schooledule.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TeacherGradeItemDTO(
    Integer itemEvaluableId,
    String itemNombre,
    String tipoActividad,
    LocalDate fecha,
    BigDecimal valor,
    String comentario,
    Integer calificacionId) {}
