package com.example.baseblock.stadium.dto;

import com.example.baseblock.stadium.entity.SeatNum;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeatResponse {
    private String number;     // 좌석 번호 (예: a001)
    private boolean isActive;  // 예매 가능 여부

    public static SeatResponse fromEntity(SeatNum seatNum) {
        return SeatResponse.builder()
                .number(seatNum.getNumber())
                .isActive(seatNum.isActive())
                .build();
    }
}