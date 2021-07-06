package com.ummetcivi.carpoolingchallenge.data.jpa;

import com.ummetcivi.carpoolingchallenge.data.entity.CarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CarRepository extends JpaRepository<CarEntity, Long> {

    Optional<CarEntity> findTopByAvailableSeatsGreaterThanEqualOrderByAvailableSeatsAsc(int people);
}
