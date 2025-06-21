package com.mournlied.nutrition_tracker_api.repository;

import com.mournlied.nutrition_tracker_api.domain.user.InformacionPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InfoPersonalRepository extends JpaRepository<InformacionPersonal, Long> {

    Optional<InformacionPersonal> findByUserUserId(Long userId);
}
