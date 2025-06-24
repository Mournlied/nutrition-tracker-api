package com.mournlied.nutrition_tracker_api.repository;

import com.mournlied.nutrition_tracker_api.domain.user.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol,Integer> {
    @Query("""
    SELECT DISTINCT r FROM Rol r
    LEFT JOIN FETCH r.permisos
    LEFT JOIN FETCH r.rolesHeredados
    WHERE r.rolId = :id
""")
    Optional<Rol> findByIdWithPermisosAndHeredados(@Param("id") Integer id);

    Optional<Rol> findRolByRolId(Integer id);
}
