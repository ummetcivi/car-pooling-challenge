package com.ummetcivi.carpoolingchallenge.controller.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CreateCarDto {

    private final long id;
    private final int seats;
}
