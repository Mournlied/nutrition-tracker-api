package com.mournlied.nutrition_tracker_api.domain.user;

import com.mournlied.nutrition_tracker_api.domain.user.dto.RegistroHistorialPesoDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Table(name = "historial_peso")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class HistorialPeso {

    @Id
    @Column(name = "historial_peso_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer historialPesoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private InformacionPersonal personalInfo;

    @Column(name = "peso_registro")
    private Integer pesoActual;

    @CreatedDate
    @Column(name = "fecha_registro")
    private LocalDate fechaActual;

    public HistorialPeso(RegistroHistorialPesoDTO dto) {
        this.pesoActual = dto.pesoActual();
        this.fechaActual = LocalDate.now();
    }
}
