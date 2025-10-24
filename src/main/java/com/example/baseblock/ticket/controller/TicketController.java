package com.example.baseblock.ticket.controller;

import com.example.baseblock.ticket.dto.TicketResponse;
import com.example.baseblock.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
// === [NEW]
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// === [NEW]
import org.springframework.data.domain.Sort;
// === [NEW]
import org.springframework.data.web.PageableDefault;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Ticket", description = "티켓 조회/클레임 API")
// === [NEW] Swagger UI에서 상단 Authorize(JWT) 버튼 연동 (springdoc에서 bearerAuth 스키마 이름과 맞춰야 함)
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @Operation(summary = "내 티켓 목록(페이지네이션)", description = "로그인한 사용자의 발급 티켓 목록을 최신 발급순으로 반환합니다.")
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MASTER')")
    public Page<TicketResponse> myTickets(
            // === [NEW] 기본 정렬: 최신순(ID desc). 프로젝트에서 발급일 필드명이 다르면 "issuedAt" 등으로 바꿔도 됨.
            @ParameterObject
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ticketService.getMyTickets(pageable);
    }

    @Operation(summary = "내 티켓 단건 조회", description = "티켓 PK로 조회하며, 본인 소유 티켓만 접근 가능합니다.")
    @GetMapping("/{ticketId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MASTER')")
    public TicketResponse getMyTicket(@PathVariable Long ticketId) {
        return ticketService.getMyTicketById(ticketId);
    }

    @Operation(summary = "예약ID로 생성된 내 티켓 조회(결제 직후 화면용)", description = "결제 완료 직후 예약ID로 생성된 최신 티켓 1건을 반환합니다.")
    @GetMapping("/by-reservation/{reservationId}")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MASTER')")
    public TicketResponse getByReservation(@PathVariable Long reservationId) {
        return ticketService.getMyTicketByReservationId(reservationId);
    }

    // ✅ [NEW] 보관지갑 → 사용자 지갑 클레임
    @Operation(
            summary = "티켓 클레임(보관지갑 → 내 지갑 이관)",
            description = "보관지갑에 민팅된 티켓을 내 지갑으로 이관합니다. 성공 시 온체인 정보(txHash, tokenId 등)가 응답에 포함됩니다." // === [NEW]
    )
    @PostMapping("/{ticketId}/claim")
    @PreAuthorize("hasAnyRole('USER','ADMIN','MASTER')")
    public TicketResponse claim(@PathVariable Long ticketId) {
        return ticketService.claimMyTicket(ticketId);
    }
}
