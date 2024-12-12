package com.sysmatic2.finalbe.strategy.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sysmatic2.finalbe.exception.InvalidDateException;
import com.sysmatic2.finalbe.strategy.common.HolidayUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class LocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final List<DateTimeFormatter> FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyyMMdd"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        return deserialize(parser.getText());
    }

    // 문자열로 날짜를 파싱 및 검증
    public LocalDate deserialize(String date) {
        for (DateTimeFormatter formatter : FORMATTERS) {
            Optional<LocalDate> parseDate = tryParseDate(date, formatter);
            if (parseDate.isPresent()) {
                LocalDate validDate = parseDate.get();

                // 공휴일 및 주말 체크
//                if (HolidayUtil.isHolidayOrWeekend(validDate)) {
//                    throw new InvalidDateException("공휴일 또는 주말은 허용되지 않는 날짜입니다: " + validDate);
//                }

                // 미래 날짜 제한
                if (validDate.isAfter(LocalDate.now())) {
                    throw new InvalidDateException("미래 날짜는 허용되지 않습니다: " + validDate);
                }

                return validDate; // 유효한 날짜 반환
            }
        }
        throw new InvalidDateException("지원하지 않는 날짜 형식입니다: " + date +
                " (허용 형식: yyyyMMdd, yyyy-MM-dd, yyyy/MM/dd)");
    }

    private Optional<LocalDate> tryParseDate(String date, DateTimeFormatter formatter) {
        try {
            return Optional.of(LocalDate.parse(date, formatter));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}