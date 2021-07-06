package com.ummetcivi.carpoolingchallenge.converter;

import com.ummetcivi.carpoolingchallenge.data.entity.JourneyQueueEntity;
import com.ummetcivi.carpoolingchallenge.domain.RegisterJourneyData;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class RegisterJourneyDataToJourneyQueueEntityConverter implements
        Converter<RegisterJourneyData, JourneyQueueEntity> {

    @Override
    public JourneyQueueEntity convert(RegisterJourneyData source) {
        return JourneyQueueEntity.builder()
                .people(source.getPeople())
                .id(source.getId())
                .build();
    }
}
