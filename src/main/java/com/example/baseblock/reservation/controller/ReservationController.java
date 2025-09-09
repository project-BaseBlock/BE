package com.example.baseblock.reservation.controller; // ✅ 패키지명 수정

import com.example.baseblock.reservation.dto.ReservationRequest;
import com.example.baseblock.reservation.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody ReservationRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        // getUsername()이 이메일을 반환하는지 확인 필요
        Long reservationId = reservationService.createReservation(request, userDetails.getUsername());
        return ResponseEntity.ok(new ReservationResponse(reservationId));
    }

    // 응답 DTO (record)
    public record ReservationResponse(Long reservationId) {}
}
