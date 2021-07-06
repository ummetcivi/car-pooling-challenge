package com.ummetcivi.carpoolingchallenge.converter;

import com.ummetcivi.carpoolingchallenge.data.entity.CarEntity;
import com.ummetcivi.carpoolingchallenge.domain.CreateCarData;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CreateCarDataToCarEntityConverter implements Converter<CreateCarData, CarEntity> {

    @Override
    public CarEntity convert(CreateCarData source) {
        return CarEntity.builder()
                .id(source.getId())
                .seats(source.getSeats())
                .availableSeats(source.getSeats())
                .build();
    }
}
