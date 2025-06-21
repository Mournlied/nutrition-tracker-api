package com.mournlied.nutrition_tracker_api.repository;

import com.mournlied.nutrition_tracker_api.domain.user.HistorialPeso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistorialPesoRepository extends JpaRepository<HistorialPeso, Integer> {

    Page<HistorialPeso> findByPersonalInfo_InfoPersonalId(Long userId, Pageable pageable);
}
