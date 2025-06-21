package com.mournlied.nutrition_tracker_api.domain.user;

import com.mournlied.nutrition_tracker_api.domain.comida.Comida;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comida> comidas;

    @OneToOne(mappedBy = "user" , cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private InformacionPersonal infoPersonal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    @Setter
    private Rol rol;

    @CreatedDate
    @Column(name = "user_creacion")
    private LocalDate fechaCreacionUser;

    @Setter
    private String correo;
    @Setter
    private Integer estado;

    public User(String correo) {
        this.fechaCreacionUser = LocalDate.now();
        this.correo = correo;
        this.estado = 1;
    }
}
