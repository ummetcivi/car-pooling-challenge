package com.ummetcivi.carpoolingchallenge.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RegisterJourneyDto {

    private final long id;
    private final int people;
}
