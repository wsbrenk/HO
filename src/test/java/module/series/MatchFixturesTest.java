package module.series;

import core.file.xml.TeamStats;
import core.model.series.Paarung;
import core.util.HODateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class MatchFixturesTest {

    @Test
    void matchFixturesReplaceTwoTeamAtRound1Test() {
        var teams = create8Teams();
        var team9 = new TeamStats();
        var team10 = new TeamStats();
        team9.setTeamId(9);
        team10.setTeamId(10);
        var fixtures = MatchFixtures.createFixtures(HODateTime.fromHTWeek(new HODateTime.HTWeek(89, 1)), teams);
        var pair1 = fixtures.get(0);
        pair1.setHeimId(team9.getTeamId());     // Team9 is replaced by Team1 after round 1
        pair1.setGastId(team10.getTeamId());    // Team10 is replaced by Team2 after round 1
        for (int i=0; i<4; i++){
            // All matches in round 1 ended 1-0
            var pair = fixtures.get(i);
            pair.setToreHeim(1);
            pair.setToreGast(0);
        }
        var matchFixtures = new MatchFixtures();
        matchFixtures.addFixtures(fixtures);
        var table = matchFixtures.getTable();
    }

    @Test
    void matchFixturesReplaceTwoTeamAtRound2Test() {
        var teams = create8Teams();
        var team9 = new TeamStats();
        var team10 = new TeamStats();
        team9.setTeamId(9);
        team10.setTeamId(10);
        var fixtures = MatchFixtures.createFixtures(HODateTime.fromHTWeek(new HODateTime.HTWeek(89, 1)), teams);
        var pair1 = fixtures.get(4);
        pair1.setHeimId(team9.getTeamId());     // Team9 is replaced by Team1 after round 1
        pair1.setGastId(team10.getTeamId());    // Team10 is replaced by Team2 after round 1
        for (int i = 0; i < 8; i++) {
            // All matches in round 1 and 2 ended 1-0
            var pair = fixtures.get(i);
            pair.setToreHeim(1);
            pair.setToreGast(0);
        }
        var matchFixtures = new MatchFixtures();
        matchFixtures.addFixtures(fixtures);
        var table = matchFixtures.getTable();
    }

    @Test
    void matchFixturesReplaceTeamEnteredInRound2() {
        var teams = create8Teams();
        var team9 = new TeamStats();
        var team10 = new TeamStats();
        team9.setTeamId(9);
        team10.setTeamId(10);
        var fixtures = MatchFixtures.createFixtures(HODateTime.fromHTWeek(new HODateTime.HTWeek(89, 1)), teams);
        var pair1 = fixtures.get(0);
        pair1.setHeimId(team9.getTeamId());     // Team9 is replaced by Team1 after round 1
        var pair8 = fixtures.get(7*4);
        pair1.setGastId(team10.getTeamId());    // Team1 is replaced by Team10 after round 8
        for (int i = 0; i < 14*4; i++) {
            // All matches ended 1-0
            var pair = fixtures.get(i);
            pair.setToreHeim(1);
            pair.setToreGast(0);
        }
        var matchFixtures = new MatchFixtures();
        matchFixtures.addFixtures(fixtures);
        var table = matchFixtures.getTable();
    }

    private List<TeamStats> create8Teams() {
        var teams = List.of(
            new TeamStats(),
            new TeamStats(),
            new TeamStats(),
            new TeamStats(),
            new TeamStats(),
            new TeamStats(),
            new TeamStats(),
            new TeamStats()
        );

        int i = 1;
        for (var team : teams) {
            team.setTeamId(i);
            ++i;
        }
        return teams;
    }

    @Test
    void createSeriesFixtures() {
        var teams = create8Teams();
        var fixtures = MatchFixtures.createFixtures(HODateTime.fromHTWeek(new HODateTime.HTWeek(89, 1)), teams);

        /*
        Spielplan
            Woche 1	1 - 2	3 - 4	5 - 6	7 - 8
            Woche 2	4 - 1	2 - 7	6 - 3	8 - 5
            Woche 3	1 - 8	3 - 5	4 - 2	7 - 6
            Woche 4	6 - 1	2 - 3	5 - 7	8 - 4
            Woche 5	1 - 7	4 - 5	3 - 8	2 - 6
            Woche 6	5 - 1	7 - 3	6 - 4	8 - 2
            Woche 7	1 - 3	2 - 5	4 - 7	6 - 8
            Woche 8	3 - 1	5 - 2	7 - 4	8 - 6
            Woche 9	1 - 5	3 - 7	4 - 6	2 - 8
            Woche 10	7 - 1	5 - 4	8 - 3	6 - 2
            Woche 11	1 - 6	3 - 2	7 - 5	4 - 8
            Woche 12	8 - 1	5 - 3	2 - 4	6 - 7
            Woche 13	1 - 4	7 - 2	3 - 6	5 - 8
            Woche 14	2 - 1	4 - 3	6 - 5	8 - 7
         */

        Assertions.assertEquals(14 * 4, fixtures.size());
        Assertions.assertTrue(containsFixtures(fixtures, 1, 1, 2));
        Assertions.assertTrue(containsFixtures(fixtures, 1, 3, 4));
        Assertions.assertTrue(containsFixtures(fixtures, 1, 5, 6));
        Assertions.assertTrue(containsFixtures(fixtures, 1, 7, 8));
        Assertions.assertTrue(containsFixtures(fixtures, 2, 4, 1));
        Assertions.assertTrue(containsFixtures(fixtures, 2, 2, 7));
        Assertions.assertTrue(containsFixtures(fixtures, 2, 6, 3));
        Assertions.assertTrue(containsFixtures(fixtures, 2, 8, 5));
        Assertions.assertTrue(containsFixtures(fixtures, 3, 1, 8));
        Assertions.assertTrue(containsFixtures(fixtures, 3, 3, 5));
        Assertions.assertTrue(containsFixtures(fixtures, 3, 4, 2));
        Assertions.assertTrue(containsFixtures(fixtures, 3, 7, 6));
        Assertions.assertTrue(containsFixtures(fixtures, 4, 6, 1));
        Assertions.assertTrue(containsFixtures(fixtures, 4, 2, 3));
        Assertions.assertTrue(containsFixtures(fixtures, 4, 5, 7));
        Assertions.assertTrue(containsFixtures(fixtures, 4, 8, 4));
        Assertions.assertTrue(containsFixtures(fixtures, 5, 1, 7));
        Assertions.assertTrue(containsFixtures(fixtures, 5, 4, 5));
        Assertions.assertTrue(containsFixtures(fixtures, 5, 3, 8));
        Assertions.assertTrue(containsFixtures(fixtures, 5, 2, 6));
        Assertions.assertTrue(containsFixtures(fixtures, 6, 5, 1));
        Assertions.assertTrue(containsFixtures(fixtures, 6, 7, 3));
        Assertions.assertTrue(containsFixtures(fixtures, 6, 6, 4));
        Assertions.assertTrue(containsFixtures(fixtures, 6, 8, 2));
        Assertions.assertTrue(containsFixtures(fixtures, 7, 1, 3));
        Assertions.assertTrue(containsFixtures(fixtures, 7, 2, 5));
        Assertions.assertTrue(containsFixtures(fixtures, 7, 4, 7));
        Assertions.assertTrue(containsFixtures(fixtures, 7, 6, 8));

    }

    private boolean containsFixtures(List<Paarung> fixtures, int matchDay, int home, int away) {
        return fixtures.stream().anyMatch(m->m.getSpieltag()==matchDay && m.getHeimId()==home && m.getGastId()== away) &&
                fixtures.stream().anyMatch(m->m.getSpieltag()==15-matchDay && m.getHeimId()==away && m.getGastId() == home);
    }

}
