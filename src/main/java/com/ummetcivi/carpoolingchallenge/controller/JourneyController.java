package com.ummetcivi.carpoolingchallenge.controller;

import com.ummetcivi.carpoolingchallenge.controller.dto.CreateCarDto;
import com.ummetcivi.carpoolingchallenge.controller.dto.RegisterJourneyDto;
import com.ummetcivi.carpoolingchallenge.domain.Car;
import com.ummetcivi.carpoolingchallenge.domain.CreateCarData;
import com.ummetcivi.carpoolingchallenge.domain.RegisterJourneyData;
import com.ummetcivi.carpoolingchallenge.exception.ResourceNotFoundException;
import com.ummetcivi.carpoolingchallenge.service.JourneyService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyService journeyService;
    private final ConversionService conversionService;

    @PutMapping("/cars")
    public ResponseEntity<Void> replaceCars(@RequestBody final List<CreateCarDto> dto) {
        final List<CreateCarData> createCarDataList = dto.stream()
                .map(createCarDto -> conversionService.convert(createCarDto, CreateCarData.class))
                .collect(Collectors.toList());
        journeyService.replaceCars(createCarDataList);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/journey")
    public ResponseEntity<Void> registerJourney(@RequestBody final RegisterJourneyDto dto) {
        final RegisterJourneyData data = conversionService.convert(dto, RegisterJourneyData.class);
        journeyService.registerJourney(data);
        return ResponseEntity.accepted().build();
    }

    @PostMapping(path = "/dropoff", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> dropOff(@RequestParam("ID") final long id) {
        journeyService.dropOff(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(path = "/locate", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Car> locate(@RequestParam("ID") final long id) {
        try {
            final Optional<Car> optionalCar = journeyService.locate(id);

            if (optionalCar.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(optionalCar.get());
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
