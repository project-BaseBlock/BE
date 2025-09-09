package com.example.baseblock.reservation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReservationCreationResponse {
    @JsonProperty("reservationId")
    private final Long reservationId;
}
