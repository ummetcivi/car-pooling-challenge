package com.ummetcivi.carpoolingchallenge.data.jpa;

import com.ummetcivi.carpoolingchallenge.data.entity.JourneyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JourneyRepository extends JpaRepository<JourneyEntity, Long> {

}
