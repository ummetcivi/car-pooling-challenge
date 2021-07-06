package com.ummetcivi.carpoolingchallenge.service;

import com.ummetcivi.carpoolingchallenge.data.entity.CarEntity;
import com.ummetcivi.carpoolingchallenge.data.entity.JourneyEntity;
import com.ummetcivi.carpoolingchallenge.data.entity.JourneyQueueEntity;
import com.ummetcivi.carpoolingchallenge.data.jpa.CarRepository;
import com.ummetcivi.carpoolingchallenge.data.jpa.JourneyQueueRepository;
import com.ummetcivi.carpoolingchallenge.data.jpa.JourneyRepository;
import com.ummetcivi.carpoolingchallenge.domain.Car;
import com.ummetcivi.carpoolingchallenge.domain.CreateCarData;
import com.ummetcivi.carpoolingchallenge.domain.RegisterJourneyData;
import com.ummetcivi.carpoolingchallenge.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class JourneyService {

    private final JourneyQueueRepository journeyQueueRepository;
    private final JourneyRepository journeyRepository;
    private final CarRepository carRepository;
    private final ConversionService conversionService;

    public void replaceCars(final List<CreateCarData> createCarDataList) {
        carRepository.deleteAll();
        if (CollectionUtils.isEmpty(createCarDataList)) {
            return;
        }

        final List<CarEntity> carEntityList = createCarDataList.stream()
                .map(createCarData -> conversionService.convert(createCarData, CarEntity.class))
                .collect(Collectors.toList());

        carRepository.saveAll(carEntityList);

        processJourneyQueue();
    }

    public void registerJourney(final RegisterJourneyData data) {
        final JourneyQueueEntity journeyQueueEntity = conversionService.convert(data, JourneyQueueEntity.class);
        journeyQueueRepository.save(journeyQueueEntity);
        startJourneyIfPossible(journeyQueueEntity);
    }

    public void dropOff(final long id) {
        final Optional<JourneyEntity> optionalJourney = journeyRepository.findById(id);

        if (optionalJourney.isEmpty()) {
            final JourneyQueueEntity journeyQueueEntity = journeyQueueRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found."));
            journeyQueueRepository.delete(journeyQueueEntity);
            return;
        }

        final JourneyEntity journeyToDropOff = optionalJourney.get();
        journeyRepository.delete(journeyToDropOff);

        freeCarAndStartNextJourney(journeyToDropOff);
    }

    public Optional<Car> locate(final long id) {
        if (journeyQueueRepository.existsById(id)) {
            return Optional.empty();
        }

        final Optional<JourneyEntity> optionalJourney = journeyRepository.findById(id);

        if (optionalJourney.isEmpty()) {
            throw new ResourceNotFoundException("Group not found.");
        }

        return optionalJourney.map(JourneyEntity::getCar)
                .map(carEntity -> conversionService.convert(carEntity, Car.class));
    }

    private void freeCarAndStartNextJourney(JourneyEntity journeyToDropOff) {
        final CarEntity carEntity = journeyToDropOff.getCar();
        carEntity.setAvailableSeats(carEntity.getAvailableSeats() + journeyToDropOff.getPeople());
        carRepository.save(carEntity);
        startJourneyForFreedCar(carEntity);
    }

    private void processJourneyQueue() {
        journeyQueueRepository.findAllByOrderByCreatedAtAsc().forEach(this::startJourneyIfPossible);
    }

    private void startJourneyIfPossible(JourneyQueueEntity journeyQueueEntity) {
        findCarForPeople(journeyQueueEntity.getPeople())
                .ifPresent(carEntity -> bookCarAndStartJourney(carEntity, journeyQueueEntity));
    }

    private void startJourneyForFreedCar(CarEntity carToBeFreed) {
        final Iterator<JourneyQueueEntity> queue = journeyQueueRepository
                .findAllByPeopleLessThanEqualOrderByCreatedAtAsc(carToBeFreed.getAvailableSeats()).iterator();

        while (queue.hasNext() && carToBeFreed.getAvailableSeats() > 0) {
            final JourneyQueueEntity currentJourneyQueue = queue.next();
            if (carToBeFreed.getAvailableSeats() >= currentJourneyQueue.getPeople()) {
                bookCarAndStartJourney(carToBeFreed, currentJourneyQueue);
            }
        }
    }

    private void bookCarAndStartJourney(CarEntity car, JourneyQueueEntity currentJourneyQueue) {
        car.setAvailableSeats(car.getAvailableSeats() - currentJourneyQueue.getPeople());
        carRepository.save(car);

        journeyQueueRepository.delete(currentJourneyQueue);
        journeyRepository.save(JourneyEntity.builder()
                .people(currentJourneyQueue.getPeople())
                .id(currentJourneyQueue.getId())
                .car(car)
                .build());
    }

    private Optional<CarEntity> findCarForPeople(int people) {
        return carRepository.findTopByAvailableSeatsGreaterThanEqualOrderByAvailableSeatsAsc(people);
    }
}
