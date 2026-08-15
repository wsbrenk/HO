package module.series;

import core.db.AbstractTable;
import core.file.xml.TeamStats;
import core.model.TranslationFacility;
import core.model.series.*;
import core.net.OnlineWorker;
import core.util.HODateTime;
import core.util.HOLogger;
import org.javatuples.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * MatchFixtures represents a game schedule, i.e. a particular season in a series.
 */
public class MatchFixtures extends AbstractTable.Storable {

    /**
     * Team slot of a league series
     */
    private static class TeamSlot {

        /**
         * Id references index numbers in fixtureEntryIndices
         */
        int id;

        /**
         * Team id of the current team of the slot
         */
        Integer currentTeamId = null;

        /**
         * Ids of teams which were replaced during the series
         */
        List<Integer> replacedTeamIds = null;

        /**
         * Constructor initializes only the slot id
         * @param id int [1..8]
         */
        public TeamSlot(int id) {
            this.id = id;
        }

        /**
         * The team id is either the current team or contained in the list of replaced teams
         * @param teamId int
         * @return boolean
         */
        public boolean contains(int teamId) {
             return this.currentTeamId == teamId ||
                 this.replacedTeamIds != null && this.replacedTeamIds.contains(teamId);
        }
    }

    /**
     * Container of the 8 team slots of one series
     */
    private static class TeamSlots {
        /**
         * List of the 8 team slots
         */
        ArrayList<TeamSlot> teamSlots = new ArrayList<>(8);

        /**
         * Constructor creates 8 team slots
         */
        public TeamSlots() {
            for (int i = 1; i <= 8; i++) {
                teamSlots.add(new TeamSlot(i));
            }
        }

        /**
         * Find the team slot containing the given team id
         * @param teamId int
         * @return TeamSlot that contains the id. Null if no slot is found.
         */
        public TeamSlot findTeamSlot(int teamId) {
            for (TeamSlot slot : teamSlots) {
                if (slot.contains(teamId)) {
                    return slot;
                }
            }
            return null;
        }

        /**
         * Set the current team id of specified slot
         * @param teamSlot slot number [1..8]
         * @param teamId the team id
         */
        public void setCurrentTeamId(int teamSlot, int teamId) {
            if ( teamSlot > 0 && teamSlot <= 8 ) {
                this.teamSlots.get(teamSlot-1).currentTeamId = teamId;
            }
        }

        /**
         * Add a team id to the list of replaced teams in specified slot
         * @param teamSlot slot number [1..8]
         * @param teamId the team id
         */
        public void addReplacedTeamSlot(int teamSlot, int teamId) {
            if ( teamSlot > 0 && teamSlot <= 8 ) {
                var slot = this.teamSlots.get(teamSlot-1);
                if (slot.replacedTeamIds == null) {
                    slot.replacedTeamIds = new ArrayList<>();
                }
                slot.replacedTeamIds.add(teamId);
            }
        }

        /**
         * Get the team slot by slot number
         * @param teamSlot slot number [1..8]
         * @return TeamSlot, null if illegal slot number was specified
         */
        public TeamSlot get(int teamSlot) {
            if (teamSlot > 0 && teamSlot <= 8) {
                return this.teamSlots.get(teamSlot-1);
            }
            return null;
        }
    }

    protected LigaTabelle m_clTabelle;
    protected String m_sLigaName = "";
    protected Tabellenverlauf m_clVerlauf;
    protected HODateTime m_clFetchDate;
    protected List<Paarung> m_vEintraege = new ArrayList<>();
    protected int m_iLigaId = -1;
    protected int m_iSaison = -1;

    /**
     * Constructor
     */
    public MatchFixtures() {
    }

    /**
     * Get list of fixtures
     * @return List<Paarung>
     */
    public final List<Paarung> getMatches() {
        return m_vEintraege;
    }

    /**
     * Setter for property m_clFetchDate.
     *
     * @param m_clFetchDate New value of property m_clFetchDate.
     */
    public final void setFetchDate(HODateTime m_clFetchDate) {
        this.m_clFetchDate = m_clFetchDate;
    }

    /**
     * Getter for property m_clFetchDate.
     *
     * @return Value of property m_clFetchDate.
     */
    public final HODateTime getFetchDate() {
        return m_clFetchDate;
    }

    /**
     * Setter for property m_iLigaId.
     *
     * @param m_iLigaId New value of property m_iLigaId.
     */
    public final void setLigaId(int m_iLigaId) {
        this.m_iLigaId = m_iLigaId;
    }

    /**
     * Getter for property m_iLigaId.
     *
     * @return Value of property m_iLigaId.
     */
    public final int getLigaId() {
        return m_iLigaId;
    }

    /**
     * Setter for property m_sLigaName.
     *
     * @param m_sLigaName New value of property m_sLigaName.
     */
    public final void setLigaName(String m_sLigaName) {
        this.m_sLigaName = m_sLigaName;
    }

    /**
     * Getter for property m_sLigaName.
     *
     * @return Value of property m_sLigaName.
     */
    public final String getLigaName() {
        return m_sLigaName;
    }

    /**
     * Returns the list of fixtures for a match day.
     *
     * @param gameDay Day number.
     * @return List – List of fixtures for a given match day.
     */
    public final List<Paarung> getFixturesOfMatchDay(final int gameDay) {
        return m_vEintraege
                .stream()
                .filter(fixture -> fixture.getSpieltag() == gameDay)
                .collect(Collectors.toList());
    }

    /**
     * Setter for property m_iSaison.
     *
     * @param m_iSaison New value of property m_iSaison.
     */
    public final void setSaison(int m_iSaison) {
        this.m_iSaison = m_iSaison;
    }

    /**
     * Getter for property m_iSaison.
     *
     * @return Value of property m_iSaison.
     */
    public final int getSaison() {
        return m_iSaison;
    }

    /**
     * Get league table
     * @return LigaTabelle
     */
    public final LigaTabelle getTable() {
        if (m_clTabelle == null) {
            m_clTabelle = calculateSeriesTable();
        }
        return m_clTabelle;
    }

    /**
     * Getter for property m_clVerlauf.
     *
     * @return Value of property m_clVerlauf.
     */
    public final Tabellenverlauf getVerlauf() {
        if (m_clVerlauf == null) {
            m_clVerlauf = generateTabellenVerlauf();
        }

        return m_clVerlauf;
    }

    /**
     * Add one fixture
     * @param spiel Paarung
     */
    public final void addEintrag(Paarung spiel) {
        if ((spiel != null) && (!m_vEintraege.contains(spiel))) {
            m_vEintraege.add(spiel);
        }
    }

    @Override
	public final boolean equals(Object o) {
        if (o instanceof MatchFixtures) {
            return (m_iLigaId == ((MatchFixtures) o).getLigaId())
                    && (m_iSaison == ((MatchFixtures) o).getSaison());
        } else {
            return false;
        }
    }

    @Override
	public final String toString() {
        return TranslationFacility.tr("Season")
               + " " + getSaison() + " "
               + TranslationFacility.tr("Liga")
               + " " + getLigaName() + " (" + getLigaId() + ")";
    }

    /**
     * Retrieves the previous position in series table for each current position in <code>tabelle</code>.
     *
     * @param tabelle       Current series table for which the previous positions are being set.
     * @param currentTeams  List containing the current teams of the series
     */
    protected final void calculatePreviousTablePositions(LigaTabelle tabelle, TeamSlots currentTeams) {

        if (tabelle.getEntries().isEmpty()) {
            return;
        }

        var spieltag = (tabelle.getEntries().elementAt(0)).getAnzSpiele() - 1;

        if (spieltag > 0) {
            var compare = calculateSeriesTable(spieltag, currentTeams);
            compare.sort();

            for (int i = 0; i < tabelle.getEntries().size(); i++) {
                var tmp = tabelle.getEntries().elementAt(i);
                var tmp2 = compare.getEintragByTeamId(tmp.getTeamId());

                if (tmp2 != null) {
                    tmp.setAltePosition(tmp2.getPosition());
                }
            }
        }
    }

    /**
     * The team slots values [1..8] are referenced in the fixtureEntryIndices
     * For each slot a list of at least one team id is returned. If teams were replaced during the series
     * a slot contains more than one entry. The first entry corresponds to the current team in the league,
     * the following entries are replaced teams.
     * @return List of team slots
     */
    private TeamSlots getTeamSlotsInSeries() {

        // Current teams
        var knownTeamSlots = findTeamSlots();
        if (knownTeamSlots == null) {
            return null;
        }

        // Find replaced teams
        for (var matchDay = 1; matchDay <= 14; matchDay++) {
            var fixtures = getFixturesOfMatchDay(matchDay);
            var unknownTeamSlots = new ArrayList<Integer>();
            var indexPairs = getMatchDayIndexPairs(matchDay);
            for (var fixture : fixtures) {
                var team0 = fixture.getHeimId();
                var team1 = fixture.getGastId();
                var team0Slot = knownTeamSlots.findTeamSlot(team0);
                var team1Slot = knownTeamSlots.findTeamSlot(team1);
                if (team0Slot != null && team1Slot != null) {
                    // Both teams are already known
                    Integer finalTeam0Slot = team0Slot.id;
                    Integer finalTeam1Slot = team1Slot.id;
                    indexPairs.removeIf(p -> p.getValue0() == finalTeam0Slot && p.getValue1() == finalTeam1Slot);
                    continue;
                }

                if (team0Slot != null) {
                    // Team1 is replaced team
                    // First check opponent of team 0 in reverse match
                    int reverseMatchDay = 15 - matchDay;
                    var reverseMatch = m_vEintraege.stream().filter(f -> f.getSpieltag() == reverseMatchDay && f.getGastId() == team0).findFirst().orElse(null);
                    if (reverseMatch != null) {
                        var newTeam = reverseMatch.getHeimId();
                        if (newTeam != team1) {
                            team1Slot = knownTeamSlots.findTeamSlot(newTeam);
                            if (team1Slot != null) {
                                knownTeamSlots.addReplacedTeamSlot(team1Slot.id, team1);
                                continue;
                            }
                        } // if matchDay > 7 reverse match is in first leg of the season
                    } // Team 0 is also replaced later on

                    // Try to find slot of team in next match days
                    for (var m = matchDay + 1; m < 15; m++) {
                        team1Slot = findTeamSlot(knownTeamSlots, team1, m);
                        if (team1Slot != null) {
                            knownTeamSlots.addReplacedTeamSlot(team1Slot.id, team1);
                            break;
                        }
                    }
                    if (team1Slot != null) {
                        // Found it
                        continue;
                    }
                }

                if (team1Slot != null) {
                    // Team0 is replaced team
                    // First check opponent of team 1 in reverse match
                    int reverseMatchDay = 15 - matchDay;
                    var reverseMatch = m_vEintraege.stream().filter(f -> f.getSpieltag() == reverseMatchDay && f.getHeimId() == team1).findFirst().orElse(null);
                    if (reverseMatch != null) {
                        var newTeam = reverseMatch.getGastId();
                        if (newTeam != team0) {
                            team0Slot = knownTeamSlots.findTeamSlot(newTeam);
                            if (team0Slot != null) {
                                knownTeamSlots.addReplacedTeamSlot(team0Slot.id, team0);
                                continue;
                            }
                        } // if matchDay > 7 reverse match is in first leg of the season
                    } // Team 0 is also replaced later on

                    for (var m = matchDay + 1; m < 15; m++) {
                        team0Slot = findTeamSlot(knownTeamSlots, team0, m);
                        if (team0Slot != null) {
                            knownTeamSlots.addReplacedTeamSlot(team0Slot.id, team0);
                            break;
                        }
                    }
                    if (team0Slot != null) {
                        continue;
                    }
                }

                if (team0Slot == null) {
                    unknownTeamSlots.add(team0);
                }
                if (team1Slot == null) {
                    unknownTeamSlots.add(team1);
                }
            }

            if (indexPairs.isEmpty()) {continue;}
            if (indexPairs.size() == 1) {
                for (var fixture : fixtures) {
                    if (unknownTeamSlots.contains(fixture.getHeimId())) {
                        knownTeamSlots.addReplacedTeamSlot(indexPairs.get(0).getValue0(), fixture.getHeimId());
                        knownTeamSlots.addReplacedTeamSlot(indexPairs.get(0).getValue1(), fixture.getGastId());
                    }
                }
            } else {
                // more than 2 teamSlots are unknown
                // Hopefully the match ids are ordered correctly by the hattrick engine
                var orderedFixtures = fixtures.stream().sorted(Comparator.comparing(Paarung::getMatchId)).toList();
                indexPairs = getMatchDayIndexPairs(matchDay);
                int matchIndex = 0;
                for (var fixture : orderedFixtures) {
                    var indexPair = indexPairs.get(matchIndex++);
                    var team0Slot = knownTeamSlots.findTeamSlot(fixture.getHeimId());
                    if (team0Slot != null) {
                        if (team0Slot.id != indexPair.getValue0()) {
                            HOLogger.instance().warning(getClass(), "Team slot mismatch");
                            break;
                        }
                    } else {
                        knownTeamSlots.addReplacedTeamSlot(indexPair.getValue0(), fixture.getHeimId());
                    }
                    var team1Slot = knownTeamSlots.findTeamSlot(fixture.getGastId());
                    if (team1Slot != null) {
                        if (team1Slot.id != indexPair.getValue1()) {
                            HOLogger.instance().warning(getClass(), "Team slot mismatch");
                            break;
                        }
                    } else {
                        knownTeamSlots.addReplacedTeamSlot(indexPair.getValue1(), fixture.getGastId());
                    }
                }
            }
        }
        return knownTeamSlots;
    }

    /**
     * Try to find the slot of team at specified match day
     * @param knownTeamSlots
     * @param teamId
     * @param matchDay
     * @return Integer Team slot [1..8] or null if not found
     */
    private TeamSlot findTeamSlot(TeamSlots knownTeamSlots, int teamId, int matchDay) {
        if (matchDay > 14) return null;
        var match = m_vEintraege.stream().filter(f -> f.getSpieltag() == matchDay && (f.getGastId() == teamId || f.getHeimId() == teamId)).findFirst().orElse(null);
        if (match == null) {
            // teamId is no longer part of the game
            return null;
        }

        var team0 = match.getHeimId();
        var team1 = match.getGastId();

        var reverseMatchDay = 15 - matchDay;
        if (team0 == teamId) {
            var reversedMatch = m_vEintraege.stream().filter(f -> f.getSpieltag() == reverseMatchDay && f.getHeimId() == team1).findFirst().orElse(null);
            if (reversedMatch != null) {
                var newTeam = reversedMatch.getGastId();
                if (newTeam != team0) {
                    return knownTeamSlots.findTeamSlot(newTeam);
                }
            }
        }
        else { // Team is team1
            var reversedMatch = m_vEintraege.stream().filter(f -> f.getSpieltag() == reverseMatchDay && f.getGastId() == team0).findFirst().orElse(null);
            if (reversedMatch != null) {
                var newTeam = reversedMatch.getHeimId();
                if (newTeam != team1) {
                    return knownTeamSlots.findTeamSlot(newTeam);
                }
            }
        }
        return null;
    }

    /**
     * Get the slot of team
     * @param knownSlots
     * @param teamId
     * @return Team slot [1..8] of null if not found
     */
    private Integer getTeamSlot(List<ArrayList<Integer>> knownSlots, int teamId) {
        int slot = 0;
        for (var ids : knownSlots) {
            slot++;
            if ( ids.contains(teamId) ) {return slot;}
        }
        return null;
    }

    /**
     * Find the slots of the current teams of the league
     * @return 8 slots with exactly one current team
     */
    private TeamSlots findTeamSlots() {
        final List<Paarung> fixturesOfLastMatchDay = getFixturesOfMatchDay(14);
        int[] arr = {0, 1, 2, 3};
        int matchesPerRound = 4;
        int[] indexes = new int[matchesPerRound]; // Control array for Heap's algorithm
        Arrays.fill(indexes, 0);

        int i = 0;
        var ret = getTeamSlotMapping(fixturesOfLastMatchDay, arr);
        if (checkTeamSlotMapping(ret, 13)) {
            return ret;
        }

        while (i < matchesPerRound) {
            if (indexes[i] < i) {
                // Swap depending on even/odd index
                if (i % 2 == 0) {
                    swap(arr, 0, i);
                } else {
                    swap(arr, indexes[i], i);
                }
                ret = getTeamSlotMapping(fixturesOfLastMatchDay, arr);
                if (checkTeamSlotMapping(ret, 13)) {
                    return ret;
                }
                indexes[i]++;
                i = 0; // Reset index
            } else {
                indexes[i] = 0;
                i++;
            }
        }
        return null;
    }

    /**
     * Check if the current teams are mapped to the correct slots
     * Checks if the correct matches of the last 3 match days will be created by the current mapping
     * @param ret
     * @param previousRound
     * @return true, if mapping is oK
     */
    private boolean checkTeamSlotMapping(TeamSlots ret, int previousRound) {
        var fixtures = getFixturesOfMatchDay(previousRound);
        var index = 14 - previousRound;
        var fixtureIndices = fixtureEntryIndices.get(index);
        for (var f : fixtures) {
            var val0 = f.getHeimId();
            var val1 = f.getGastId();
            var found = false;
            for (var i : fixtureIndices) {
                var list0 = ret.get(i.getValue1());
                var list1 = ret.get(i.getValue0());
                if (list0.contains(val0) && list1.contains(val1)) {
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }
        if ( previousRound > 12 ) return checkTeamSlotMapping(ret, previousRound-1);
        return true;
    }

    private static @NotNull TeamSlots getTeamSlotMapping(List<Paarung> fixturesOfMatchDay, int[] arr) {
        var teamSlots =  new TeamSlots();
        var fixtureIndicesOfRound14 = fixtureEntryIndices.get(0); // First round (same as round 14, but home and guest swapped
        for (int k = 0; k < fixturesOfMatchDay.size(); k++) {
            var pair = fixturesOfMatchDay.get(arr[k]);
            var fixtureIndexPair = fixtureIndicesOfRound14.get(k);
            var teamSlot = fixtureIndexPair.getValue0();
            var guestTeamId = pair.getGastId();

            teamSlots.setCurrentTeamId(teamSlot, guestTeamId);
            teamSlot = fixtureIndexPair.getValue1();
            var homeTeamId = pair.getHeimId();
            teamSlots.setCurrentTeamId(teamSlot, homeTeamId);
        }
        return teamSlots;
    }

    // Swap helper method
    private static void swap(int[] arr, int a, int b) {
        int temp = arr[a];
        arr[a] = arr[b];
        arr[b] = temp;
    }

    /**
     * Calculates the series table
     * @return LigaTabelle – Computed series table.
     */
    private LigaTabelle calculateSeriesTable() {
        return calculateSeriesTable(14, getTeamSlotsInSeries());
    }

    /**
     * Get the team slot indexes of fixtures at specified round
     * @param matchDay Round [1..14]
     * @return List of 4 fixtures (pairs)
     */
    private List<Pair<Integer, Integer>> getMatchDayIndexPairs(int matchDay) {
        if (matchDay < 8) return new ArrayList<>(fixtureEntryIndices.get(matchDay-1));
        var i = 14 - matchDay;
        var ret = new ArrayList<Pair<Integer, Integer>>();
        var pairs = fixtureEntryIndices.get(i);
        for (var pair : pairs) {
            ret.add(new Pair<>(pair.getValue1(), pair.getValue0()));
        }
        return ret;
    }

    /**
     * Calculates the series table of given match day
     * @param maxMatchDay   1..14
     * @param teams  List of list of team ids
     * @return LigaTabelle
     */
    private LigaTabelle calculateSeriesTable(int maxMatchDay, TeamSlots teams) {
        final LigaTabelle ligaTabelle = new LigaTabelle();
        ligaTabelle.setLigaId(m_iLigaId);
        ligaTabelle.setLigaName(m_sLigaName);

        for( var ids : teams.teamSlots) {
            ligaTabelle.addEintrag(calculateTableEntry(ids, maxMatchDay));
        }

        if(ligaTabelle.getEntries().get(0).getAnzSpiele() > 0) {
            ligaTabelle.sort();
            calculatePreviousTablePositions(ligaTabelle, teams);
        }
        else {
            var seriesDetails = OnlineWorker.getSeriesDetails(this.getLigaId());
            for ( var t : ligaTabelle.getEntries()){
                var details = seriesDetails.get(String.valueOf(t.getTeamId()));
                var position = details.getPosition();
                t.setPosition(position);
                t.setAltePosition(position);
            }
            ligaTabelle.sortByPosition();
        }

        return ligaTabelle;
    }

    /**
     * Get all matches of the current team and eventually the team which was replaced by the current team
     * during the series
     * @param ids Of the teams
     * @return List of fixtures
     */
    private List<Paarung> getMatchesByTeamIds(TeamSlot ids) {
        return m_vEintraege.stream()
                .filter(fixture -> (ids.contains(fixture.getHeimId()) || (ids.contains(fixture.getGastId()))))
                .sorted()
                .toList();
    }

    /**
     * Creates a league table from the matches of a team.
     *
     * @param maxMatchDay Day until which the table is being calculated (1–14)
     */
    private SerieTableEntry calculateTableEntry(TeamSlot ids, int maxMatchDay) {
        var matches = getMatchesByTeamIds(ids);
        final SerieTableEntry eintrag = new SerieTableEntry();
        int gameNumber = 0;
        int homeVictories = 0;
        int homeDraws = 0;
        int homeDefeats = 0;
        int awayVictories = 0;
        int awayDraws = 0;
        int awayDefeats = 0;
        int homeGoalsFor = 0;
        int homeGoalsAgainst = 0;
        int awayGoalsFor = 0;
        int awayGoalsAgainst = 0;
        int homePoints = 0;
        int awayPoints = 0;

        eintrag.setTeamId(ids.currentTeamId); // First entry is the current existing teams
        var name = "";

        for ( var match : matches) {
            if ( match.getSpieltag() > maxMatchDay) { break; }

            var isHomeTeam = ids.contains(match.getHeimId());
            name = isHomeTeam?match.getHeimName():match.getGastName();

            // Games already played
            if (match.getToreHeim() > -1) {
                gameNumber++;

                // Home game
                if (isHomeTeam) {
                    // Win
                    if (match.getToreHeim() > match.getToreGast()) {
                        eintrag.addSerienEintrag(match.getSpieltag() - 1, SerieTableEntry.H_SIEG);
                        homePoints += 3;
                        homeVictories += 1;
                        homeGoalsAgainst += match.getToreGast();
                        homeGoalsFor += match.getToreHeim();
                    }
                    // Draw
                    else if (match.getToreHeim() == match.getToreGast()) {
                        eintrag.addSerienEintrag(match.getSpieltag() - 1, SerieTableEntry.H_UN);
                        homePoints += 1;
                        homeDraws += 1;
                        homeGoalsAgainst += match.getToreGast();
                        homeGoalsFor += match.getToreHeim();
                    }
                    // Defeat
                    else {
                        eintrag.addSerienEintrag(match.getSpieltag() - 1, SerieTableEntry.H_NIED);
                        homeDefeats += 1;
                        homeGoalsAgainst += match.getToreGast();
                        homeGoalsFor += match.getToreHeim();
                    }
                }
                // Away
                else {
                    // Defeat
                    if (match.getToreHeim() > match.getToreGast()) {
                        eintrag.addSerienEintrag(match.getSpieltag() - 1, SerieTableEntry.A_NIED);
                        awayDefeats += 1;
                        awayGoalsAgainst += match.getToreHeim();
                        awayGoalsFor += match.getToreGast();
                    }
                    // Draw
                    else if (match.getToreHeim() == match.getToreGast()) {
                        eintrag.addSerienEintrag(match.getSpieltag() - 1, SerieTableEntry.A_UN);
                        awayPoints += 1;
                        awayDraws += 1;
                        awayGoalsAgainst += match.getToreHeim();
                        awayGoalsFor += match.getToreGast();
                    }
                    // Win
                    else {
                        eintrag.addSerienEintrag(match.getSpieltag() - 1, SerieTableEntry.A_SIEG);
                        awayPoints += 3;
                        awayVictories += 1;
                        awayGoalsAgainst += match.getToreHeim();
                        awayGoalsFor += match.getToreGast();
                    }
                }
            }
        }

        eintrag.setTeamName(name);
        eintrag.setAnzSpiele(gameNumber);

        //home
        eintrag.setH_Nied(homeDefeats);
        eintrag.setH_Siege(homeVictories);
        eintrag.setH_Un(homeDraws);
        eintrag.setH_Punkte(homePoints);
        eintrag.setH_ToreFuer(homeGoalsFor);
        eintrag.setH_ToreGegen(homeGoalsAgainst);

        //Away
        eintrag.setA_Nied(awayDefeats);
        eintrag.setA_Siege(awayVictories);
        eintrag.setA_Un(awayDraws);
        eintrag.setA_Punkte(awayPoints);
        eintrag.setA_ToreFuer(awayGoalsFor);
        eintrag.setA_ToreGegen(awayGoalsAgainst);

        // Total
        eintrag.setPunkte(awayPoints + homePoints);
        eintrag.setToreFuer(awayGoalsFor + homeGoalsFor);
        eintrag.setToreGegen(awayGoalsAgainst + homeGoalsAgainst);
        eintrag.setG_Nied(awayDefeats + homeDefeats);
        eintrag.setG_Siege(awayVictories + homeVictories);
        eintrag.setG_Un(awayDraws + homeDraws);

        return eintrag;
    }

    /**
     * Creates the table history.
     *
     * @return Tabellenverlauf – Table position history.
     */
    protected final Tabellenverlauf generateTabellenVerlauf() {

        final Tabellenverlauf verlauf = new Tabellenverlauf();
        TabellenVerlaufEintrag[] eintraege = null;

        try {
        	var spieltag = getTable().getEntries().elementAt(0).getAnzSpiele();
            var tabelle = new LigaTabelle[spieltag];

            var currentTeams = getTeamSlotsInSeries();
            for (int i = spieltag; i > 0; i--) {
                tabelle[i - 1] = calculateSeriesTable(i, currentTeams);
            }

            // Create history entries
            if (tabelle.length > 0) {
                eintraege = new TabellenVerlaufEintrag[tabelle[spieltag - 1].getEntries().size()];

                for (int j = 0; j < tabelle[spieltag - 1].getEntries().size(); j++) {
                    final int[] positionen = new int[tabelle.length];

                    eintraege[j] = new TabellenVerlaufEintrag();
                    eintraege[j].setTeamId(tabelle[spieltag - 1].getEntries().elementAt(j).getTeamId());
                    eintraege[j].setTeamName(tabelle[spieltag - 1].getEntries().elementAt(j).getTeamName());

                    for (int i = 0; i < tabelle.length; i++) {
                        var tmp = tabelle[i].getEintragByTeamId(eintraege[j].getTeamId());

                        if (tmp != null) {
                            positionen[i] = tmp.getPosition();
                        } else {
                            positionen[i] = -1;
                        }
                    }

                    eintraege[j].setPlatzierungen(positionen);
                }
            }

            verlauf.setEintraege(eintraege);
            return verlauf;
        } catch (Exception e) {
        	HOLogger.instance().error(getClass(), "Error(generateTabellenVerlauf):" + e);
            return new Tabellenverlauf();
        }
    }

    /**
     * Add a list of fixtures
     * @param fixtures List<Paarung>
     */
    public void addFixtures(List<Paarung> fixtures) {
        m_vEintraege.addAll(fixtures);
    }

    /**
     * Map of indices used to generate series' fixtures
     */
    private static final List<List<Pair<Integer, Integer>>> fixtureEntryIndices = List.of(
            List.of(new Pair<>(1,2), new Pair<>(3,4), new Pair<>(5,6), new Pair<>(7,8)),
            List.of(new Pair<>(4,1), new Pair<>(2,7), new Pair<>(6,3), new Pair<>(8,5)),
            List.of(new Pair<>(1,8), new Pair<>(3,5), new Pair<>(4,2), new Pair<>(7,6)),
            List.of(new Pair<>(6,1), new Pair<>(2,3), new Pair<>(5,7), new Pair<>(8,4)),
            List.of(new Pair<>(1,7), new Pair<>(4,5), new Pair<>(3,8), new Pair<>(2,6)),
            List.of(new Pair<>(5,1), new Pair<>(7,3), new Pair<>(6,4), new Pair<>(8,2)),
            List.of(new Pair<>(1,3), new Pair<>(2,5), new Pair<>(4,7), new Pair<>(6,8))
    );

    /**
     * Create one fixture of match day
     *
     * @param matchId preliminary match id (will be replaced, when hattrick's plan is released)
     * @param date  Match date
     * @param round Match day
     * @param team1 Id of home team
     * @param team2 Id of away team
     * @return Paarung  Fixture
     */
    private static Paarung createFixture(int matchId, HODateTime date, int round, TeamStats team1, TeamStats team2) {
        var ret = new Paarung();
        ret.setDatum(date);
        ret.setHeimId(team1.getTeamId());
        ret.setGastId(team2.getTeamId());
        ret.setHeimName(team1.getTeamName());
        ret.setGastName(team2.getTeamName());
        ret.setSpieltag(round);
        ret.setMatchId(matchId);
        return ret;
    }

    /**
     * Create all fixtures of a series
     * @param seriesStartDate   Start date of the series
     * @param teams             List of 8 teams
     * @return List of fixtures
     */
    public static List<Paarung> createFixtures(HODateTime seriesStartDate, List<TeamStats> teams) {
        assert teams.size() == 8;
        var newFixtures = new ArrayList<Paarung>();
        var date = seriesStartDate;
        int roundNumber = 1;
        int matchId = 0;

        // First series half
        for (var round : fixtureEntryIndices){
            for ( var match  : round){
                newFixtures.add(createFixture(matchId++, date, roundNumber, teams.get(match.getValue0()-1), teams.get(match.getValue1()-1)));
            }
            roundNumber++;
            date = date.plusDaysAtSameLocalTime(7);
        }

        var copy = new ArrayList<>(fixtureEntryIndices);
        Collections.reverse(copy);
        // Second series half
        for (var round : copy){
            for ( var match  : round){
                newFixtures.add(createFixture(matchId++, date, roundNumber, teams.get(match.getValue1()-1), teams.get(match.getValue0()-1)));
            }
            roundNumber++;
            date = date.plusDaysAtSameLocalTime(7);
        }
        return newFixtures;
    }
}
