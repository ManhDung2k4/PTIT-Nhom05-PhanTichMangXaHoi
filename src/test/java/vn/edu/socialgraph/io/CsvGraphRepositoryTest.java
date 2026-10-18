package vn.edu.socialgraph.io;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.edu.socialgraph.model.SocialGraph;
import vn.edu.socialgraph.model.User;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvGraphRepositoryTest {
    @Test void roundTripPreservesUsersAndEdges(@TempDir Path dir) throws Exception {
        SocialGraph graph = new SocialGraph();
        graph.addUser(new User("u1", "An")); graph.addUser(new User("u2", "Bình"));
        graph.connect("u1", "u2");
        Path file = dir.resolve("graph.csv");
        CsvGraphRepository repository = new CsvGraphRepository();

        repository.save(graph, file);
        SocialGraph loaded = repository.load(file);

        assertEquals(2, loaded.getUsers().size());
        assertEquals(List.of("u2"), loaded.friendsOf("u1").stream().map(User::getId).toList());
    }

    @Test void preservesCommasQuotesAndUnicode(@TempDir Path dir) throws Exception {
        SocialGraph graph = new SocialGraph();
        graph.addUser(new User("u1", "Nguyễn, An \"A\""));
        graph.addUser(new User("u2", "Bình Trần"));
        graph.connect("u1", "u2");
        Path file = dir.resolve("special-characters.csv");
        CsvGraphRepository repository = new CsvGraphRepository();

        repository.save(graph, file);
        SocialGraph loaded = repository.load(file);

        assertEquals("Nguyễn, An \"A\"", loaded.requireUser("u1").getName());
        assertEquals(List.of("u2"), loaded.friendsOf("u1").stream().map(User::getId).toList());
    }
}
