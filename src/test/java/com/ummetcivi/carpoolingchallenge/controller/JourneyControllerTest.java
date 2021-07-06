package com.ummetcivi.carpoolingchallenge.controller;

import com.ummetcivi.carpoolingchallenge.TestConstants;
import com.ummetcivi.carpoolingchallenge.controller.dto.CreateCarDto;
import com.ummetcivi.carpoolingchallenge.controller.dto.RegisterJourneyDto;
import com.ummetcivi.carpoolingchallenge.domain.Car;
import com.ummetcivi.carpoolingchallenge.domain.CreateCarData;
import com.ummetcivi.carpoolingchallenge.domain.RegisterJourneyData;
import com.ummetcivi.carpoolingchallenge.service.JourneyService;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

public class JourneyControllerTest {

    @Mock
    private JourneyService journeyService;
    @Mock
    private ConversionService conversionService;

    private JourneyController underTest;

    @BeforeMethod
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        underTest = new JourneyController(journeyService, conversionService);
    }

    @Test
    public void shouldReplaceCars() {
        final CreateCarDto createCarDto = Mockito.mock(CreateCarDto.class);
        final CreateCarData createCarData = Mockito.mock(CreateCarData.class);
        final List<CreateCarDto> dto = List.of(createCarDto);

        Mockito.when(conversionService.convert(createCarDto, CreateCarData.class)).thenReturn(createCarData);

        // When
        final ResponseEntity<Void> response = underTest.replaceCars(dto);

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertNull(response.getBody());

        Mockito.verify(conversionService).convert(createCarDto, CreateCarData.class);
        Mockito.verify(journeyService).replaceCars(List.of(createCarData));

    }

    @Test
    public void shouldRegisterJourney() {
        // Given
        final RegisterJourneyDto dto = Mockito.mock(RegisterJourneyDto.class);
        final RegisterJourneyData data = Mockito.mock(RegisterJourneyData.class);

        Mockito.when(conversionService.convert(dto, RegisterJourneyData.class)).thenReturn(data);

        // When
        final ResponseEntity<Void> response = underTest.registerJourney(dto);

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.ACCEPTED);
        Assert.assertNull(response.getBody());

        Mockito.verify(conversionService).convert(dto, RegisterJourneyData.class);
        Mockito.verify(journeyService).registerJourney(data);
    }

    @Test
    public void shouldDropOff() {
        // When
        final ResponseEntity<Void> response = underTest.dropOff(TestConstants.ANY_JOURNEY_ID);

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        Assert.assertNull(response.getBody());

        Mockito.verify(journeyService).dropOff(TestConstants.ANY_JOURNEY_ID);
    }

    @Test
    public void shouldReturnCarIfJourneyPresent() {
        // Given
        final Car car = Mockito.mock(Car.class);

        Mockito.when(journeyService.locate(TestConstants.ANY_JOURNEY_ID)).thenReturn(Optional.of(car));

        // When
        final ResponseEntity<Car> response = underTest.locate(TestConstants.ANY_JOURNEY_ID);

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assert.assertEquals(response.getBody(), car);

        Mockito.verify(journeyService).locate(TestConstants.ANY_JOURNEY_ID);
    }

    @Test
    public void shouldReturnNoContentIfJourneyNotPresent() {
        // Given
        Mockito.when(journeyService.locate(TestConstants.ANY_JOURNEY_ID)).thenReturn(Optional.empty());

        // When
        final ResponseEntity<Car> response = underTest.locate(TestConstants.ANY_JOURNEY_ID);

        // Then
        Assert.assertNotNull(response);
        Assert.assertEquals(response.getStatusCode(), HttpStatus.NO_CONTENT);
        Assert.assertNull(response.getBody());

        Mockito.verify(journeyService).locate(TestConstants.ANY_JOURNEY_ID);
    }
}