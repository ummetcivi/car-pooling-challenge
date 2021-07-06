package com.ummetcivi.carpoolingchallenge.data.jpa;

import com.ummetcivi.carpoolingchallenge.data.entity.JourneyQueueEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JourneyQueueRepository extends JpaRepository<JourneyQueueEntity, Long> {

    List<JourneyQueueEntity> findAllByOrderByCreatedAtAsc();

    List<JourneyQueueEntity> findAllByPeopleLessThanEqualOrderByCreatedAtAsc(int seats);
}
