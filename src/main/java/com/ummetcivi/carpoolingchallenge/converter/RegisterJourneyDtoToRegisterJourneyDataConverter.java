package com.ummetcivi.carpoolingchallenge.converter;

import com.ummetcivi.carpoolingchallenge.controller.dto.RegisterJourneyDto;
import com.ummetcivi.carpoolingchallenge.domain.RegisterJourneyData;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RegisterJourneyDtoToRegisterJourneyDataConverter implements
        Converter<RegisterJourneyDto, RegisterJourneyData> {

    @Override
    public RegisterJourneyData convert(RegisterJourneyDto source) {
        return RegisterJourneyData.builder()
                .people(source.getPeople())
                .id(source.getId())
                .build();
    }
}
