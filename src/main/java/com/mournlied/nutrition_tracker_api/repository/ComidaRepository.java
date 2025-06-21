package com.mournlied.nutrition_tracker_api.repository;

import com.mournlied.nutrition_tracker_api.domain.comida.Comida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ComidaRepository extends JpaRepository<Comida,Long> {

    Page<Comida> findByUserUserIdAndFechaCreacionComidaBetween(
            Long userId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Page<Comida> findByUserUserIdAndEsFavoritaTrue(Long userId, Pageable pageable);

    Optional<Comida> findByNombrecomida(String nombreComida);
}
