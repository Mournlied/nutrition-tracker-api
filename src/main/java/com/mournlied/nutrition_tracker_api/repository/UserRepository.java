package com.mournlied.nutrition_tracker_api.repository;

import com.mournlied.nutrition_tracker_api.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findUserByCorreo(String correo);

    Optional<User> findUserByUserId(Long id);

    @Query("""
    SELECT u FROM User u
    JOIN FETCH u.rol r
    WHERE u.correo = :correo
""")
    Optional<User> findUserWithRolesAndPermisosByCorreo(@Param("correo") String correo);
}
