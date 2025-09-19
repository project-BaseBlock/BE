package com.example.baseblock.payment.dto;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentReadyV2Request {

    @NotNull
    private Long reservationId;

    /*
    @NotNull
    private Long stadiumId;     // 예: 잠실 id
    @NotNull
    private String zoneName;    // 예: "그린", "오렌지" ...
    @NotEmpty
    private List<String> seatNumbers; // 예: ["g001","g002"]
    */

}
