package com.sysmatic2.finalbe.strategy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FollowingRankingResponseDto {
    private String memberId; //멤버ID
    private String nickname; //닉네임
    private String profilePath; //프로필사진
    private String introduction; //자기소개글
    private Integer strategyCnt; //전략 수
    private Integer followerCnt; //전체 전략에 대한 팔로워 수
    private BigDecimal totalCumulativeProfitLoss; // 전체 전략에 대한 누적손익액 합산
}
