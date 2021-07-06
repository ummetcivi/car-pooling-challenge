package com.ummetcivi.carpoolingchallenge.service;

import com.ummetcivi.carpoolingchallenge.TestConstants;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

public class JourneyServiceTest {

    @Mock
    private JourneyQueueRepository journeyQueueRepository;
    @Mock
    private JourneyRepository journeyRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private ConversionService conversionService;

    private JourneyService underTest;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new JourneyService(journeyQueueRepository, journeyRepository, carRepository, conversionService);
    }

    @Test
    public void shouldReplaceCarsAndProcessJourneyQueue() {
        // Given
        final CreateCarData createCarData = Mockito.mock(CreateCarData.class);
        final CarEntity carEntity = CarEntity.builder()
                .availableSeats(TestConstants.ANY_SEATS)
                .seats(TestConstants.ANY_SEATS)
                .build();

        final List<CreateCarData> data = List.of(createCarData);

        final JourneyQueueEntity journeyQueueEntity = Mockito.mock(JourneyQueueEntity.class);

        Mockito.when(journeyQueueEntity.getPeople()).thenReturn(TestConstants.ANY_PEOPLE);
        Mockito.when(journeyQueueEntity.getId()).thenReturn(TestConstants.ANY_JOURNEY_ID);

        Mockito.when(
                carRepository.findTopByAvailableSeatsGreaterThanEqualOrderByAvailableSeatsAsc(TestConstants.ANY_PEOPLE))
                .thenReturn(Optional.of(carEntity));
        Mockito.when(journeyQueueRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(journeyQueueEntity));
        Mockito.when(conversionService.convert(createCarData, CarEntity.class)).thenReturn(carEntity);
        Mockito.when(carRepository.saveAll(Mockito.anyList())).then(invocation -> invocation.getArgument(0));

        // When
        underTest.replaceCars(data);

        // Then
        Mockito.verify(conversionService).convert(createCarData, CarEntity.class);

        ArgumentCaptor<List<CarEntity>> savedCarsArgumentCaptor = ArgumentCaptor.forClass(List.class);
        Mockito.verify(carRepository).saveAll(savedCarsArgumentCaptor.capture());
        final List<CarEntity> savedCars = savedCarsArgumentCaptor.getValue();

        Assert.assertNotNull(savedCars);
        Assert.assertEquals(savedCars.size(), 1);
        final CarEntity savedCar = savedCars.get(0);

        Assert.assertEquals(savedCar, carEntity);

        Mockito.verify(carRepository).save(carEntity);

        Assert.assertEquals(carEntity.getAvailableSeats(), TestConstants.ANY_SEATS - TestConstants.ANY_PEOPLE);

        Mockito.verify(journeyQueueRepository).delete(journeyQueueEntity);

        ArgumentCaptor<JourneyEntity> savedJourneyEntityArgumentCaptor = ArgumentCaptor.forClass(JourneyEntity.class);
        Mockito.verify(journeyRepository).save(savedJourneyEntityArgumentCaptor.capture());
        final JourneyEntity savedJourney = savedJourneyEntityArgumentCaptor.getValue();

        Assert.assertEquals(savedJourney.getCar(), carEntity);
        Assert.assertEquals(savedJourney.getPeople(), TestConstants.ANY_PEOPLE);
        Assert.assertEquals(savedJourney.getId(), TestConstants.ANY_JOURNEY_ID);
    }

    @Test
    public void shouldDeleteAllCarsIfGivenCarListIsEmpty() {
        // When
        underTest.replaceCars(List.of());

        // Then
        Mockito.verify(carRepository).deleteAll();
        Mockito.verifyNoMoreInteractions(carRepository);
    }

    @Test
    public void shouldRegisterJourneyToQueueAndFindCarIfPossible() {
        // Given
        final CarEntity carEntity = CarEntity.builder()
                .availableSeats(TestConstants.ANY_SEATS)
                .seats(TestConstants.ANY_SEATS)
                .build();
        final RegisterJourneyData data = Mockito.mock(RegisterJourneyData.class);
        final JourneyQueueEntity journeyQueueEntity = Mockito.mock(JourneyQueueEntity.class);

        Mockito.when(journeyQueueEntity.getPeople()).thenReturn(TestConstants.ANY_PEOPLE);
        Mockito.when(journeyQueueEntity.getId()).thenReturn(TestConstants.ANY_JOURNEY_ID);

        Mockito.when(conversionService.convert(data, JourneyQueueEntity.class)).thenReturn(journeyQueueEntity);
        Mockito.when(
                carRepository.findTopByAvailableSeatsGreaterThanEqualOrderByAvailableSeatsAsc(TestConstants.ANY_PEOPLE))
                .thenReturn(Optional.of(carEntity));

        // When
        underTest.registerJourney(data);

        // Then
        Mockito.verify(conversionService).convert(data, JourneyQueueEntity.class);
        Mockito.verify(journeyQueueRepository).save(journeyQueueEntity);

        Mockito.verify(carRepository).save(carEntity);

        Assert.assertEquals(carEntity.getAvailableSeats(), TestConstants.ANY_SEATS - TestConstants.ANY_PEOPLE);

        Mockito.verify(journeyQueueRepository).delete(journeyQueueEntity);

        ArgumentCaptor<JourneyEntity> savedJourneyEntityArgumentCaptor = ArgumentCaptor.forClass(JourneyEntity.class);
        Mockito.verify(journeyRepository).save(savedJourneyEntityArgumentCaptor.capture());
        final JourneyEntity savedJourney = savedJourneyEntityArgumentCaptor.getValue();

        Assert.assertEquals(savedJourney.getCar(), carEntity);
        Assert.assertEquals(savedJourney.getPeople(), TestConstants.ANY_PEOPLE);
        Assert.assertEquals(savedJourney.getId(), TestConstants.ANY_JOURNEY_ID);
    }

    @Test
    public void shouldDropOffFromJourneyQueue() {
        // Given
        final JourneyQueueEntity journeyQueueEntity = Mockito.mock(JourneyQueueEntity.class);

        Mockito.when(journeyRepository.findById(TestConstants.ANY_JOURNEY_ID)).thenReturn(Optional.empty());
        Mockito.when(journeyQueueRepository.findById(TestConstants.ANY_JOURNEY_ID))
                .thenReturn(Optional.of(journeyQueueEntity));

        // When
        underTest.dropOff(TestConstants.ANY_JOURNEY_ID);

        // Then
        Mockito.verify(journeyRepository).findById(TestConstants.ANY_JOURNEY_ID);
        Mockito.verify(journeyQueueRepository).findById(TestConstants.ANY_JOURNEY_ID);
        Mockito.verify(journeyQueueRepository).delete(journeyQueueEntity);
    }

    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void shouldNotDropOffAndThrowResourceNotFoundExceptionWhenJourneyNotPresentInBothQueueAndJourneyTable() {
        // Given
        Mockito.when(journeyRepository.findById(TestConstants.ANY_JOURNEY_ID)).thenReturn(Optional.empty());
        Mockito.when(journeyQueueRepository.findById(TestConstants.ANY_JOURNEY_ID)).thenReturn(Optional.empty());

        try {
            // When
            underTest.dropOff(TestConstants.ANY_JOURNEY_ID);
        } finally {
            // Then
            Mockito.verify(journeyRepository).findById(TestConstants.ANY_JOURNEY_ID);
            Mockito.verify(journeyQueueRepository).findById(TestConstants.ANY_JOURNEY_ID);
            Mockito.verifyNoMoreInteractions(journeyQueueRepository);
        }
    }

    @Test
    public void shouldDropOffAndStartJourneyForFreedCarIfPossible() {
        // Given
        final JourneyEntity journeyEntity = Mockito.mock(JourneyEntity.class);
        final CarEntity carEntity = CarEntity.builder()
                .availableSeats(TestConstants.ANY_SEATS)
                .build();
        final JourneyQueueEntity journeyQueueEntity = Mockito.mock(JourneyQueueEntity.class);

        Mockito.when(journeyQueueEntity.getId()).thenReturn(TestConstants.ANY_JOURNEY_ID);
        Mockito.when(journeyQueueEntity.getPeople()).thenReturn(TestConstants.ANY_OTHER_PEOPLE);

        Mockito.when(journeyEntity.getCar()).thenReturn(carEntity);
        Mockito.when(journeyEntity.getPeople()).thenReturn(TestConstants.ANY_PEOPLE);

        Mockito.when(journeyRepository.findById(TestConstants.ANY_JOURNEY_ID)).thenReturn(Optional.of(journeyEntity));

        Mockito.when(journeyQueueRepository
                .findAllByPeopleLessThanEqualOrderByCreatedAtAsc(TestConstants.ANY_SEATS + TestConstants.ANY_PEOPLE))
                .thenReturn(List.of(journeyQueueEntity));

        // When
        underTest.dropOff(TestConstants.ANY_JOURNEY_ID);

        // Then
        Mockito.verify(journeyRepository).findById(TestConstants.ANY_JOURNEY_ID);
        Mockito.verify(journeyQueueRepository, Mockito.never()).findById(TestConstants.ANY_JOURNEY_ID);
        Mockito.verify(journeyRepository).delete(journeyEntity);

        Mockito.verify(carRepository, Mockito.times(2)).save(carEntity);

        Assert.assertEquals(carEntity.getAvailableSeats(),
                TestConstants.ANY_SEATS + TestConstants.ANY_PEOPLE - TestConstants.ANY_OTHER_PEOPLE);

        Mockito.verify(journeyQueueRepository).delete(journeyQueueEntity);

        ArgumentCaptor<JourneyEntity> startedJourneyArgumentCaptor = ArgumentCaptor.forClass(JourneyEntity.class);
        Mockito.verify(journeyRepository).save(startedJourneyArgumentCaptor.capture());
        final JourneyEntity startedJourney = startedJourneyArgumentCaptor.getValue();

        Assert.assertEquals(startedJourney.getCar(), carEntity);
        Assert.assertEquals(startedJourney.getPeople(), TestConstants.ANY_OTHER_PEOPLE);
        Assert.assertEquals(startedJourney.getId(), TestConstants.ANY_JOURNEY_ID);
    }

    @Test
    public void shouldDropOffButStartJourneyForFreedCarWhenCarDoesntHaveEnoughSeatsForQueuedJourney() {
        // Given
        final JourneyEntity journeyEntity = Mockito.mock(JourneyEntity.class);
        final CarEntity carEntity = CarEntity.builder()
                .availableSeats(TestConstants.ANY_SEATS)
                .build();
        final JourneyQueueEntity journeyQueueEntity = Mockito.mock(JourneyQueueEntity.class);

        Mockito.when(journeyQueueEntity.getId()).thenReturn(TestConstants.ANY_JOURNEY_ID);
        Mockito.when(journeyQueueEntity.getPeople()).thenReturn(TestConstants.ANY_EXCEEDING_PEOPLE);

        Mockito.when(journeyEntity.getCar()).thenReturn(carEntity);
        Mockito.when(journeyEntity.getPeople()).thenReturn(TestConstants.ANY_PEOPLE);

        Mockito.when(journeyRepository.findById(TestConstants.ANY_JOURNEY_ID)).thenReturn(Optional.of(journeyEntity));

        Mockito.when(journeyQueueRepository
                .findAllByPeopleLessThanEqualOrderByCreatedAtAsc(TestConstants.ANY_SEATS + TestConstants.ANY_PEOPLE))
                .thenReturn(List.of(journeyQueueEntity));

        // When
        underTest.dropOff(TestConstants.ANY_JOURNEY_ID);

        // Then
        Mockito.verify(journeyRepository).findById(TestConstants.ANY_JOURNEY_ID);
        Mockito.verify(journeyQueueRepository, Mockito.never()).findById(TestConstants.ANY_JOURNEY_ID);
        Mockito.verify(journeyRepository).delete(journeyEntity);
        Mockito.verify(carRepository).save(carEntity);

        Assert.assertEquals(carEntity.getAvailableSeats(), TestConstants.ANY_SEATS + TestConstants.ANY_PEOPLE);

        Mockito.verify(journeyRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(journeyQueueRepository, Mockito.never()).delete(Mockito.any());
    }

    @Test(expectedExceptions = ResourceNotFoundException.class)
    public void shouldThrowResourceNotFoundExceptionWhenJourneyNotPresentInBothJourneyAndQueue() {
        // Given
        Mockito.when(journeyRepository.findById(TestConstants.ANY_JOURNEY_ID)).thenReturn(Optional.empty());
        Mockito.when(journeyQueueRepository.existsById(TestConstants.ANY_JOURNEY_ID)).thenReturn(false);

        try {
            // When
            underTest.locate(TestConstants.ANY_JOURNEY_ID);
        } finally {
            // Then
            Mockito.verify(journeyRepository).findById(TestConstants.ANY_JOURNEY_ID);
            Mockito.verify(journeyQueueRepository).existsById(TestConstants.ANY_JOURNEY_ID);
        }
    }

    @Test
    public void shouldReturnOptionalEmptyWhenJourneyToLocateIsInTheQueue() {
        // Given
        Mockito.when(journeyQueueRepository.existsById(TestConstants.ANY_JOURNEY_ID)).thenReturn(true);

        // When
        final Optional<Car> result = underTest.locate(TestConstants.ANY_JOURNEY_ID);

        // Then
        Assert.assertTrue(result.isEmpty());

        Mockito.verify(journeyQueueRepository).existsById(TestConstants.ANY_JOURNEY_ID);
        Mockito.verifyNoInteractions(journeyRepository);
    }

    @Test
    public void shouldLocateJourneyAndReturnCar() {
        // Given
        final JourneyEntity journeyEntity = Mockito.mock(JourneyEntity.class);
        final CarEntity carEntity = Mockito.mock(CarEntity.class);
        final Car car = Mockito.mock(Car.class);

        Mockito.when(journeyRepository.findById(TestConstants.ANY_JOURNEY_ID)).thenReturn(Optional.of(journeyEntity));
        Mockito.when(journeyEntity.getCar()).thenReturn(carEntity);
        Mockito.when(conversionService.convert(carEntity, Car.class)).thenReturn(car);

        // When
        final Optional<Car> result = underTest.locate(TestConstants.ANY_JOURNEY_ID);

        // Then
        Assert.assertTrue(result.isPresent());
        Assert.assertEquals(result.get(), car);

        Mockito.verify(journeyRepository).findById(TestConstants.ANY_JOURNEY_ID);
        Mockito.verify(conversionService).convert(carEntity, Car.class);
    }
}