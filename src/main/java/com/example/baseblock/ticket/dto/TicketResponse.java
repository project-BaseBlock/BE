package com.example.baseblock.ticket.dto;

import com.example.baseblock.ticket.entity.Ticket;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketResponse {

    @Schema(description = "티켓 PK")
    private Long ticketId;

    @Schema(description = "예약 PK")
    private Long reservationId;

    @Schema(description = "결제 PK")
    private Long paymentId;

    @Schema(description = "게임 ID (GameSchedule PK)")
    private Long gameId;

    @Schema(description = "좌석 번호 (예: a012, r034)")
    private String seatNo;

    @Schema(description = "NFT 토큰 ID")
    private Long tokenId; // (장기적으로 String/BigInteger 고려 가능)

    @Schema(description = "민팅 트랜잭션 해시(0x..)")
    private String txHash;

    @Schema(description = "발급(민팅) 시각")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime issuedAt;

    /* [추가] 경기 정보 필드 */
    @Schema(description = "홈팀 이름")
    private String homeTeam;

    @Schema(description = "원정팀 이름")
    private String awayTeam;

    @Schema(description = "경기 제목 (홈 vs 원정)")
    private String gameTitle;

    // fromEntity() 수정
    public static TicketResponse fromEntity(Ticket t) {
        Long gameId = (t.getGameSchedule() != null) ? t.getGameSchedule().getGameId() : null;
        String seatNo = (t.getSeatNum() != null) ? t.getSeatNum().getNumber() : null;

        String homeTeam = null;
        String awayTeam = null;
        String gameTitle = null;

        // GameSchedule에서 home / away 접근
        if (t.getGameSchedule() != null) {
            if (t.getGameSchedule().getHome() != null) {
                homeTeam = t.getGameSchedule().getHome().getTeamName();
            }
            if (t.getGameSchedule().getAway() != null) {
                awayTeam = t.getGameSchedule().getAway().getTeamName();
            }
            if (homeTeam != null && awayTeam != null) {
                gameTitle = homeTeam + " vs " + awayTeam;
            }
        }

        return TicketResponse.builder()
                .ticketId(t.getId())
                .reservationId(t.getReservation() != null ? t.getReservation().getId() : null)
                .paymentId(t.getPayment() != null ? t.getPayment().getId() : null)
                .gameId(gameId)
                .seatNo(seatNo)
                .tokenId(t.getTokenId())
                .txHash(t.getTxHash())
                .issuedAt(t.getIssuedAt())
                /* 추가된 필드 매핑 */
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .gameTitle(gameTitle)
                .build();
    }
}
