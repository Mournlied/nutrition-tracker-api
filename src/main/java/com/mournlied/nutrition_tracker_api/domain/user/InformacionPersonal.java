package com.mournlied.nutrition_tracker_api.domain.user;

import com.mournlied.nutrition_tracker_api.domain.user.dto.RegistroInfoPersonalDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "informacion_personal")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class InformacionPersonal {

    @Id
    @Column(name = "user_id")
    private Long infoPersonalId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "personalInfo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Setter
    private List<HistorialPeso> historialPeso;

    @Setter
    private Integer pesoInicial;
    @Setter
    private String nombre;
    @Setter
    private LocalDate nacimiento;
    @Setter
    private Integer altura;
    @Setter
    private String objetivos;

    public InformacionPersonal(RegistroInfoPersonalDTO registroDTO, User user) {
        this.user = user;
        this.pesoInicial = registroDTO.pesoInicial();
        this.nombre = registroDTO.nombre();
        this.nacimiento = registroDTO.nacimiento();
        this.altura = registroDTO.altura();
        this.objetivos = registroDTO.objetivos();
    }
}
