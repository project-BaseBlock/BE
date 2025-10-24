package com.example.baseblock.payment.dto;

import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentReadyV2Request {

    /*
    @NotNull
    private Long reservationId;


    @NotNull
    private Long stadiumId;     // 예: 잠실 id
    @NotNull
    private String zoneName;    // 예: "그린", "오렌지" ...
    @NotEmpty
    private List<String> seatNumbers; // 예: ["g001","g002"]
    */

    @NotNull
    private Long reservationId;     // 필수: readyPaymentV2에서 사용

    // 서버에서 유효성 직접 체크하므로 Bean Validation은 생략
    private Long stadiumId;
    private String zoneName;
    private List<String> seatNumbers;

}
