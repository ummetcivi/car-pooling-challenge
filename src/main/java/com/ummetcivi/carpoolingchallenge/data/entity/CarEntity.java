package com.ummetcivi.carpoolingchallenge.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarEntity {

    @Id
    private long id;
    private int seats;
    private int availableSeats;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "car")
    private List<JourneyEntity> journeys;
}
