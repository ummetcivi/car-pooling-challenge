package com.ummetcivi.carpoolingchallenge.converter;

import com.ummetcivi.carpoolingchallenge.controller.dto.CreateCarDto;
import com.ummetcivi.carpoolingchallenge.domain.CreateCarData;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CreateCarDtoToCreateCarDataConverter implements Converter<CreateCarDto, CreateCarData> {

    @Override
    public CreateCarData convert(CreateCarDto source) {
        return CreateCarData.builder()
                .id(source.getId())
                .seats(source.getSeats())
                .build();
    }
}
