package com.example.baseblock.reservation.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReservationRequest {
    private Long gameId;
    private String zoneName;
    private Long stadiumId;
    private List<String> seatNumbers;
}
