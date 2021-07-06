package com.ummetcivi.carpoolingchallenge.domain;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CreateCarData {

    private final long id;
    private final int seats;
}
