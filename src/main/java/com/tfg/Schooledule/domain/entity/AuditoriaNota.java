package com.tfg.Schooledule.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_notas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditoriaNota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "calificacion_id", nullable = false)
    private Calificacion calificacion;

    @Column(name = "valor_anterior", precision = 5, scale = 2)
    private BigDecimal valorAnterior;

    @Column(name = "valor_nuevo", precision = 5, scale = 2)
    private BigDecimal valorNuevo;

    @Column(name = "usuario_responsable", length = 100)
    private String usuarioResponsable;

    @CreationTimestamp
    @Column(name = "fecha_cambio", updatable = false)
    private LocalDateTime fechaCambio;

    @Column(length = 255)
    private String motivo;
}
