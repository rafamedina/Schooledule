package com.tfg.schooledule.domain.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradeDashboardDTO {
    private String periodoNombre;
    private Map<String, List<GradeDTO>> gradesByModulo;
}
