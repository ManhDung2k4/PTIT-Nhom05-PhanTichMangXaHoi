package vn.edu.socialgraph.algorithm;

import org.junit.jupiter.api.Test;
import vn.edu.socialgraph.model.SocialGraph;
import vn.edu.socialgraph.model.User;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DfsAndPathAlgorithmsTest {
    @Test void traversesGraphByDepth() {
        assertEquals(List.of("u1", "u2", "u3", "u4"), ids(DfsAndPathAlgorithms.dfs(sampleGraph(), "u1")));
    }

    @Test void findsShortestPath() {
        assertEquals(List.of("u1", "u2", "u3", "u4"),
                ids(DfsAndPathAlgorithms.shortestPath(sampleGraph(), "u1", "u4")));
    }

    @Test void returnsEmptyPathForDisconnectedUsers() {
        SocialGraph graph = sampleGraph();
        graph.addUser(new User("u5", "Em"));
        assertTrue(DfsAndPathAlgorithms.shortestPath(graph, "u1", "u5").isEmpty());
    }

    @Test void pathFromUserToItselfContainsOnlyThatUser() {
        assertEquals(List.of("u1"), ids(DfsAndPathAlgorithms.shortestPath(sampleGraph(), "u1", "u1")));
    }

    @Test void suggestsAndRanksFriendsUsingDfs() {
        SocialGraph graph = suggestionGraph();
        assertEquals(List.of("u4", "u5"), ids(DfsAndPathAlgorithms.suggestFriends(graph, "u1")));
    }

    @Test void finishesQuicklyOnMediumGraph() {
        SocialGraph graph = mediumGraph(600);
        assertTimeout(Duration.ofSeconds(2), () -> {
            assertEquals(600, DfsAndPathAlgorithms.dfs(graph, "u0").size());
            assertFalse(DfsAndPathAlgorithms.shortestPath(graph, "u0", "u599").isEmpty());
            DfsAndPathAlgorithms.suggestFriends(graph, "u0");
        });
    }

    @Test void shortestPathPicksMinimalEdgesInCycle() {
        SocialGraph graph = sampleGraph(); // u1-u2-u3-u4
        graph.connect("u1", "u4"); // Tạo đường tắt trực tiếp
        assertEquals(List.of("u1", "u4"), ids(DfsAndPathAlgorithms.shortestPath(graph, "u1", "u4")));
    }


    private static List<String> ids(List<User> users) { return users.stream().map(User::getId).toList(); }

    private static SocialGraph sampleGraph() {
        SocialGraph graph = new SocialGraph();
        addUsers(graph, 4);
        graph.connect("u1", "u2"); graph.connect("u2", "u3"); graph.connect("u3", "u4");
        return graph;
    }

    private static SocialGraph suggestionGraph() {
        SocialGraph graph = new SocialGraph();
        addUsers(graph, 5);
        graph.connect("u1", "u2"); graph.connect("u1", "u3");
        graph.connect("u2", "u4"); graph.connect("u3", "u4"); graph.connect("u2", "u5");
        return graph;
    }

    private static SocialGraph mediumGraph(int size) {
        SocialGraph graph = new SocialGraph();
        for (int i = 0; i < size; i++) graph.addUser(new User("u" + i, "User " + i));
        for (int i = 0; i < size - 1; i++) graph.connect("u" + i, "u" + (i + 1));
        return graph;
    }

    private static void addUsers(SocialGraph graph, int count) {
        for (int i = 1; i <= count; i++) graph.addUser(new User("u" + i, "User " + i));
    }
}
