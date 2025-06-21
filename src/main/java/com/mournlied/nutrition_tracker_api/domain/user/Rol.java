package com.mournlied.nutrition_tracker_api.domain.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rolId;

    @OneToMany(mappedBy = "rol", fetch = FetchType.LAZY)
    private Set<User> users;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "rol_permisos",
            joinColumns = @JoinColumn(name = "rol_id"),
            inverseJoinColumns = @JoinColumn(name = "permiso_id")
    )
    @Setter
    private Set<Permiso> permisos;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "rol_hierarchy",
            joinColumns = @JoinColumn(name = "rol_padre_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_hijo_id")
    )
    @Setter
    private Set<Rol> rolesHeredados;

    @ManyToMany(mappedBy = "rolesHeredados")
    private Set<Rol> rolesPadres;

    @Setter
    private String nombreRol;
}
