package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HabrCareerDateTimeParserTest {
    @Test
    public void whenDateTimeFormatIsCorrect() {
        String dateTime = "2023-11-05T14:46:16+03:00";
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        LocalDateTime parsedDate = parser.parse(dateTime);
        assertThat(parsedDate.getYear()).isEqualTo(2023);
        assertThat(parsedDate.getMonthValue()).isEqualTo(11);
        assertThat(parsedDate.getDayOfMonth()).isEqualTo(5);
        assertThat(parsedDate.getHour()).isEqualTo(14);
    }

    @Test
    public void whenDateTimeFormatIsInCorrect() {
        String dateTime = "2023-11-05";
        HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
        assertThatThrownBy(() -> parser.parse(dateTime))
                .isInstanceOf(DateTimeParseException.class);
    }
}