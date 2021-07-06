package com.ummetcivi.carpoolingchallenge.converter;

import com.ummetcivi.carpoolingchallenge.data.entity.CarEntity;
import com.ummetcivi.carpoolingchallenge.domain.Car;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CarEntityToCarConverter implements Converter<CarEntity, Car> {

    @Override
    public Car convert(CarEntity source) {
        return Car.builder()
                .id(source.getId())
                .seats(source.getSeats())
                .build();
    }
}
