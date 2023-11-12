package ru.job4j.grabber.utils;

import java.time.LocalDateTime;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        return LocalDateTime.parse(parse, java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}