
package vn.edu.socialgraph.algorithm;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import vn.edu.socialgraph.model.SocialGraph;
import vn.edu.socialgraph.model.User;

class BfsAlgorithmsTest {

    @Test
    void traversesGraphByBreadth() {
        assertEquals(
                List.of("u1", "u2", "u3", "u4"),
                ids(BfsAlgorithms.bfs(sampleGraph(), "u1"))
        );
    }

    @Test
    void suggestsFriendOfFriend() {
        assertEquals(
                List.of("u3"),
                ids(BfsAlgorithms.suggestFriends(sampleGraph(), "u1"))
        );
    }

    @Test
    void prioritizesCandidateWithMoreMutualFriends() {
        SocialGraph graph = suggestionGraph();

        assertEquals(
                List.of("u4", "u5"),
                ids(BfsAlgorithms.suggestFriends(graph, "u1"))
        );
    }

    @Test
    void finishesQuicklyOnMediumGraph() {
        SocialGraph graph = mediumGraph(600);

        assertTimeout(Duration.ofSeconds(2), () -> {
            assertEquals(600, BfsAlgorithms.bfs(graph, "u0").size());
            BfsAlgorithms.suggestFriends(graph, "u0");
        });
    }

    @Test
    void isolatedNodeBfsReturnsOnlySelfAndNoSuggestions() {
        SocialGraph graph = new SocialGraph();
        graph.addUser(new User("u1", "Độc thân"));

        assertEquals(
                List.of("u1"),
                ids(BfsAlgorithms.bfs(graph, "u1"))
        );
        assertTrue(BfsAlgorithms.suggestFriends(graph, "u1").isEmpty());
    }

    @Test
    void bfsHandlesCyclesWithoutVisitingSameUserTwice() {
        SocialGraph graph = new SocialGraph();
        addUsers(graph, 4);

        graph.connect("u1", "u2");
        graph.connect("u2", "u3");
        graph.connect("u3", "u1");
        graph.connect("u3", "u4");

        List<String> result = ids(BfsAlgorithms.bfs(graph, "u1"));

        assertEquals(4, result.size());
        assertEquals(4, result.stream().distinct().count());
        assertEquals(
                List.of("u1", "u2", "u3", "u4"),
                result
        );
    }

    @Test
    void suggestionsExcludeSelfAndDirectFriends() {
        SocialGraph graph = new SocialGraph();
        addUsers(graph, 4);

        graph.connect("u1", "u2");
        graph.connect("u1", "u3");
        graph.connect("u2", "u3");
        graph.connect("u2", "u4");

        List<String> suggestions =
                ids(BfsAlgorithms.suggestFriends(graph, "u1"));

        assertFalse(suggestions.contains("u1"));
        assertFalse(suggestions.contains("u2"));
        assertFalse(suggestions.contains("u3"));
        assertTrue(suggestions.contains("u4"));
    }

    @Test
    void bfsThrowsForUnknownUser() {
        SocialGraph graph = sampleGraph();

        assertThrows(
                IllegalArgumentException.class,
                () -> BfsAlgorithms.bfs(graph, "unknown")
        );
    }

    private static List<String> ids(List<User> users) {
        return users.stream()
                .map(User::getId)
                .toList();
    }

    private static SocialGraph sampleGraph() {
        SocialGraph graph = new SocialGraph();
        addUsers(graph, 4);

        graph.connect("u1", "u2");
        graph.connect("u2", "u3");
        graph.connect("u3", "u4");

        return graph;
    }

    private static SocialGraph suggestionGraph() {
        SocialGraph graph = new SocialGraph();
        addUsers(graph, 5);

        graph.connect("u1", "u2");
        graph.connect("u1", "u3");
        graph.connect("u2", "u4");
        graph.connect("u3", "u4");
        graph.connect("u2", "u5");

        return graph;
    }

    private static SocialGraph mediumGraph(int size) {
        SocialGraph graph = new SocialGraph();

        for (int i = 0; i < size; i++) {
            graph.addUser(new User("u" + i, "User " + i));
        }

        for (int i = 0; i < size - 1; i++) {
            graph.connect("u" + i, "u" + (i + 1));
        }

        return graph;
    }

    private static void addUsers(SocialGraph graph, int count) {
        for (int i = 1; i <= count; i++) {
            graph.addUser(new User("u" + i, "User " + i));
        }
    }
}
