package ru.job4j.grabber;

import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public class PsqlStore implements Store {

    private Connection cnn;

    public PsqlStore(Properties cfg) throws SQLException {
        try {
            Class.forName(cfg.getProperty("jdbc.driver"));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        cnn = DriverManager.getConnection(cfg.getProperty("jdbc.url"), cfg.getProperty("jdbc.username"), cfg.getProperty("jdbc.password"));
    }

    @Override
    public void save(Post post) {
        String query = "INSERT INTO posts (name, text, link, created) \n"
             + "VALUES(?, ?, ?, ?)\n"
             + "ON CONFLICT (link) \n"
             + "do update SET name = EXCLUDED.name, text = EXCLUDED.text, created = EXCLUDED.created;";
        try (PreparedStatement statement =
                     cnn.prepareStatement(query,
                             Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            statement.setTimestamp(4, Timestamp.valueOf(post.getCreated()));
            statement.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> posts = new ArrayList<>();
        try (PreparedStatement statement = cnn.prepareStatement("SELECT * FROM posts")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    posts.add(getPost(resultSet));
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return posts;
    }

    @Override
    public Post findById(int id) {
        Post post =  null;
        try (PreparedStatement statement =
                     cnn.prepareStatement("SELECT * FROM posts WHERE id = ?")) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    post = getPost(resultSet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return post;
    }

    public void close() throws Exception {
        if (cnn != null) {
            cnn.close();
        }
    }

    private Post getPost(ResultSet resultSet) throws SQLException {
        return new Post(
            resultSet.getString("name"),
            resultSet.getString("link"),
            resultSet.getString("text"),
            resultSet.getTimestamp("created").toLocalDateTime()
        );
    }

    public static void main(String[] args) {
        Properties properties = new Properties();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        try (InputStream io = classloader.getResourceAsStream("rabbit.properties")) {
            properties.load(io);
            PsqlStore store = new PsqlStore(properties);
            Post post = new Post(
                "Java developer++",
                "https://career.habr.com/vacancies/1000118895",
                    null,
                    new HabrCareerDateTimeParser().parse("2023-11-05T14:46:16+03:00")
            );
            store.save(post);
            store.getAll().forEach(System.out::println);
            System.out.println(store.findById(11));
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}