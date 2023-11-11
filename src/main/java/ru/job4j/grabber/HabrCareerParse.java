package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HabrCareerParse {

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies/java_developer", SOURCE_LINK);

    public static void main(String[] args) throws IOException {
        for (int page = 1; page <= 5; page++) {
            String url = PAGE_LINK + "?page" + page;
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();
            Elements rows = document.select(".vacancy-card__inner");
            rows.forEach(row -> {
                Element titleElement = row.select(".vacancy-card__title").first();
                Element linkElement = titleElement.child(0);
                String vacancyName = titleElement.text();
                Element dateElement = row.select(".vacancy-card__date").select(".basic-date").first();
                String vacancyDate = dateElement.attr("datetime");
                HabrCareerDateTimeParser parser = new HabrCareerDateTimeParser();
                LocalDateTime localDateTime = parser.parse(vacancyDate);
                vacancyDate = getLocatDateTime(localDateTime);
                String link = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
                System.out.printf("%s %s %s%n", vacancyName, vacancyDate, link);
            });
        }

    }

    private static String getLocatDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "";
    }

}