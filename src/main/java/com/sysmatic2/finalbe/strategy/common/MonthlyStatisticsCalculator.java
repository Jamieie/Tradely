package com.sysmatic2.finalbe.strategy.common;

import com.sysmatic2.finalbe.strategy.entity.DailyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.entity.MonthlyStatisticsEntity;
import com.sysmatic2.finalbe.strategy.repository.DailyStatisticsRepository;
import com.sysmatic2.finalbe.strategy.repository.MonthlyStatisticsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MonthlyStatisticsCalculator {

    /**
     * 현재 월 데이터를 가져오거나 새로 생성하는 메서드.
     *
     * @param strategyId      전략 ID
     * @param dailyStatistics 해당 일간 통계 데이터
     * @param currentMonth    현재 월 (String)
     * @param msp             월간 통계 레포지토리
     * @return 존재하는 월간 통계 데이터 또는 새로 생성된 데이터
     */
    public static MonthlyStatisticsEntity getOrCreateMonthlyStatistics(Long strategyId, DailyStatisticsEntity dailyStatistics,
                                                                       String currentMonth, MonthlyStatisticsRepository msp) {
        // 월간 통계 데이터가 이미 존재하면 반환, 없으면 새로운 데이터 생성
        return msp.findByStrategyIdAndAnalysisMonth(strategyId, currentMonth)
                .orElseGet(() -> MonthlyStatisticsEntity.builder()
                        .strategyEntity(dailyStatistics.getStrategyEntity()) // 전략 엔티티 설정
                        .analysisMonth(currentMonth) // 현재 월 설정
                        .monthlyAvgPrincipal(BigDecimal.ZERO) // 초기 월평균 원금
                        .monthlyDepWdAmount(BigDecimal.ZERO) // 초기 월 입출금 총액
                        .monthlyProfitLoss(BigDecimal.ZERO) // 초기 월 손익
                        .monthlyReturn(BigDecimal.ZERO) // 초기 월 손익률
                        .monthlyCumulativeProfitLoss(BigDecimal.ZERO) // 초기 월 누적 손익
                        .monthlyCumulativeReturn(BigDecimal.ZERO) // 초기 월 누적 손익률
                        .build());
    }

    /**
     * 월평균 원금을 계산하는 메서드.
     *
     * @param strategyId      전략 ID
     * @param dailyStatistics 해당 일간 통계 데이터
     * @param currentMonth    현재 월
     * @param dsp             일간 통계 레포지토리
     * @return 월평균 원금
     */
    public static BigDecimal calculateMonthlyAveragePrincipal(Long strategyId, DailyStatisticsEntity dailyStatistics,
                                                              String currentMonth, DailyStatisticsRepository dsp) {
        // 해당 월의 모든 일간 원금을 조회
        List<BigDecimal> dailyPrincipals = dsp.findDailyPrincipalsByStrategyIdAndMonth(
                strategyId, currentMonth);
        // 데이터가 null인 경우 빈 리스트로 초기화
        if (dailyPrincipals == null) {
            dailyPrincipals = new ArrayList<>();
        }
        // 현재 원금을 리스트에 추가
        dailyPrincipals.add(dailyStatistics.getPrincipal());

        // 원금 리스트가 비어 있으면 0 반환, 그렇지 않으면 평균 계산
        return dailyPrincipals.isEmpty()
                ? BigDecimal.ZERO
                : dailyPrincipals.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add) // 원금 합산
                .divide(BigDecimal.valueOf(dailyPrincipals.size()), 4, RoundingMode.HALF_UP); // 평균 계산 (소수점 4자리 반올림)
    }

    /**
     * 월 입출금 총액을 계산하는 메서드.
     *
     * @param strategyId      전략 ID
     * @param dailyStatistics 해당 일간 통계 데이터
     * @param currentMonth    현재 월
     * @param dsp             일간 통계 레포지토리
     * @return 월 입출금 총액
     */
    public static BigDecimal calculateTotalDepWdAmount(Long strategyId, DailyStatisticsEntity dailyStatistics,
                                                       String currentMonth, DailyStatisticsRepository dsp) {
        // 해당 월의 모든 입출금 데이터를 조회
        List<BigDecimal> depWdAmounts = dsp.findDailyDepWdAmountsByStrategyIdAndMonth(
                strategyId, currentMonth);
        // 데이터가 null인 경우 빈 리스트로 초기화
        if (depWdAmounts == null) {
            depWdAmounts = new ArrayList<>();
        }
        // 현재 입출금 금액을 리스트에 추가
        depWdAmounts.add(dailyStatistics.getDepWdPrice());

        // 입출금 데이터가 비어 있으면 0 반환, 그렇지 않으면 총합 계산
        return depWdAmounts.isEmpty()
                ? BigDecimal.ZERO
                : depWdAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add); // 총합 계산
    }

    /**
     * 월 손익을 계산하는 메서드.
     *
     * @param strategyId      전략 ID
     * @param dailyStatistics 해당 일간 통계 데이터
     * @param currentMonth    현재 월
     * @param dsp             일간 통계 레포지토리
     * @return 월 손익
     */
    public static BigDecimal calculateTotalProfitLoss(Long strategyId, DailyStatisticsEntity dailyStatistics,
                                                      String currentMonth, DailyStatisticsRepository dsp) {
        // 해당 월의 모든 일간 손익 데이터를 조회
        List<BigDecimal> profitLosses = dsp.findDailyProfitLossesByStrategyIdAndMonth(
                strategyId, currentMonth);
        // 데이터가 null인 경우 빈 리스트로 초기화
        if (profitLosses == null) {
            profitLosses = new ArrayList<>();
        }
        // 현재 일간 손익을 리스트에 추가
        profitLosses.add(dailyStatistics.getDailyProfitLoss());

        // 손익 데이터가 비어 있으면 0 반환, 그렇지 않으면 총합 계산
        return profitLosses.isEmpty()
                ? BigDecimal.ZERO
                : profitLosses.stream().reduce(BigDecimal.ZERO, BigDecimal::add); // 총합 계산
    }

    /**
     * 월 손익률을 계산하는 메서드.
     *
     * - 이전 월의 마지막 기준가와 현재 전달된 기준가를 비교하여 월 손익률을 계산합니다.
     *
     * @param strategyId           전략 ID
     * @param currentMonth         조회할 현재 월 (yyyy-MM 형식의 문자열)
     * @param currentReferencePrice 현재 월 마지막 기준가 (외부에서 전달받음)
     * @param dsp                  일간 통계 레포지토리
     * @return 월 손익률 (없으면 0 반환)
     */
    public static BigDecimal calculateMonthlyReturn(Long strategyId, DailyStatisticsEntity dailyStatistics,
                                                    String currentMonth, DailyStatisticsRepository dsp) {
        // 이전 월 계산
        String previousMonth = getPreviousMonth(currentMonth);

        // 이전 월 마지막 기준가 가져오기
        BigDecimal previousReferencePrice = getPreviousMonthLastReferencePrice(strategyId, previousMonth, dsp);

        // 이전 기준가 검증: 0 이하일 경우 월 손익률 0 반환
        if (previousReferencePrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 현재 기준가 검증: 0 이하일 경우 월 손익률 0 반환
        if (dailyStatistics.getReferencePrice() == null || dailyStatistics.getReferencePrice().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 월 손익률 = (현재 기준가 - 이전 기준가) / 이전 기준가
        return (dailyStatistics.getReferencePrice()).subtract(previousReferencePrice)
                .divide(previousReferencePrice, 4, RoundingMode.HALF_UP);
    }

    /**
     * 월 누적 손익을 계산하는 메서드.
     *
     * - 현재 입력된 일간 데이터의 누적 손익을 반환합니다.
     *
     * @param dailyStatistics 현재 입력된 일간 통계 엔티티
     * @return 일간 데이터의 누적 손익 (null일 경우 0 반환)
     */
    public static BigDecimal calculateCumulativeProfitLoss(DailyStatisticsEntity dailyStatistics) {
        // 현재 일간 누적 손익을 반환
        return dailyStatistics.getDailyProfitLoss() != null ? dailyStatistics.getDailyProfitLoss() : BigDecimal.ZERO;
    }

    /**
     * 월 누적 손익률을 계산하는 메서드.
     *
     * - 현재 전달받은 `DailyStatisticsEntity`의 기준가를 초기 기준가(1000)와 비교하여 월 누적 손익률을 계산합니다.
     *
     * @param dailyStatistics 현재 월 마지막 기준가가 포함된 `DailyStatisticsEntity`
     * @return 월 누적 손익률 (없으면 0 반환)
     */
    public static BigDecimal calculateCumulativeReturn(DailyStatisticsEntity dailyStatistics) {
        // 현재 기준가 검증: 기준가가 0 이하일 경우 기본값 0 반환
        if (dailyStatistics.getReferencePrice() == null || dailyStatistics.getReferencePrice().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 월 누적 손익률 = (현재 기준가 / 1000) - 1
        return dailyStatistics.getReferencePrice()
                .divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP)
                .subtract(BigDecimal.ONE);
    }

    /**
     * 이전 월의 마지막 기준가를 가져오는 메서드.
     *
     * @param strategyId    전략 ID
     * @param previousMonth 이전 월
     * @param dsp           일간 통계 레포지토리
     * @return 이전 월의 마지막 기준가
     */
    public static BigDecimal getPreviousMonthLastReferencePrice(Long strategyId, String previousMonth,
                                                                DailyStatisticsRepository dsp) {
        // 이전 월의 마지막 기준가를 조회
        List<BigDecimal> referencePrices = dsp.findLastReferencePriceByStrategyIdAndMonth(
                strategyId, previousMonth, Pageable.ofSize(1));

        // 리스트가 비어 있으면 0 반환, 그렇지 않으면 첫 번째 값 반환
        return referencePrices == null || referencePrices.isEmpty() ? BigDecimal.ZERO : referencePrices.get(0);
    }

    /**
     * 주어진 월(yyyy-MM)에서 이전 월(yyyy-MM)을 계산하는 메서드.
     *
     * @param currentMonth 현재 월 (yyyy-MM 형식의 문자열)
     * @return 이전 월 (yyyy-MM 형식의 문자열)
     */
    private static String getPreviousMonth(String currentMonth) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        return YearMonth.parse(currentMonth, formatter).minusMonths(1).format(formatter);
    }
}