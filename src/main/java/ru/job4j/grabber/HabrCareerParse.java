package ru.job4j.grabber;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private final DateTimeParser dateTimeParser;

    private static final String SOURCE_LINK = "https://career.habr.com";

    private static final String PAGE_LINK = String.format("%s/vacancies?q=Java%%20developer&type=all", SOURCE_LINK);

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    public static void main(String[] args) throws IOException {
        HabrCareerParse parse = new HabrCareerParse(new HabrCareerDateTimeParser());
        List<Post> vacancies = parse.list(PAGE_LINK);
        vacancies.forEach(System.out::println);
    }

    private static String retrieveDescription(String link) {
        String description = null;
        Connection vacancyConnection = Jsoup.connect(link);
        try {
            Document vacancyDocument = vacancyConnection.get();
            Element vacancyElement = vacancyDocument.select(".faded-content").first();
            description = vacancyElement.text();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return description;
    }

    private static String getLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")) : "";
    }

    @Override
    public List<Post> list(String link) throws IOException {
        List<Post> vacancies = new ArrayList<>();
        Connection connection = Jsoup.connect(link);
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
            String vacancyLink = String.format("%s%s", SOURCE_LINK, linkElement.attr("href"));
            String description = retrieveDescription(vacancyLink);
            Post post = new Post(vacancyName, vacancyLink, description, localDateTime);
            vacancies.add(post);
        });

        return vacancies;
    }
}