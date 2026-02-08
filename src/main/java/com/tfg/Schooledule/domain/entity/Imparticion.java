package com.tfg.schooledule.domain.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "imparticiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Imparticion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "modulo_id", nullable = false)
    private Modulo modulo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "grupo_id", nullable = false)
    private Grupo grupo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profesor_id", nullable = false)
    private Usuario profesor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "centro_id", nullable = false)
    private Centro centro;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "configuracion_evaluacion", columnDefinition = "jsonb")
    private Map<String, Object> configuracionEvaluacion;
}
