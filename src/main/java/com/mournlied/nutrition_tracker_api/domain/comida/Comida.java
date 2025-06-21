package com.mournlied.nutrition_tracker_api.domain.comida;

import com.mournlied.nutrition_tracker_api.domain.comida.dto.RegistroComidaDTO;
import com.mournlied.nutrition_tracker_api.domain.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "comidas")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Comida {

    @Id
    @Column(name = "comida_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long comidaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @CreatedDate
    @Column(name = "comida_creacion")
    private LocalDate fechaCreacionComida;

    @Setter
    private String nombreComida;

    @Column(name = "cantidad_gramos")
    @Setter
    private Integer cantidadEnGramos;

    @Column(name = "comida_descripcion")
    @Setter
    private String descripcion;

    @Setter
    private String tipoComida;

    @Convert(converter = JsonbConverter.class)
    @Column(name = "info_nutricional", columnDefinition = "jsonb")
    @Setter
    private Map<String, Object> informacionNutricional;

    @Setter
    private Boolean esFavorita;

    public Comida(RegistroComidaDTO dto, User user) {
        this.nombreComida = dto.nombreComida();
        this.cantidadEnGramos = dto.cantidadEnGramos();
        this.descripcion = dto.descripcion();
        this.tipoComida = dto.tipoComida();
        this.informacionNutricional = dto.informacionNutricional();
        this.esFavorita = dto.esFavorita();
        this.user = user;
        this.fechaCreacionComida = LocalDate.now();
    }
}
