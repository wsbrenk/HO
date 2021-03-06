package core.db;

import core.model.match.MatchLineupPlayer;
import core.model.match.MatchLineupTeam;
import core.model.enums.MatchType;
import core.util.HOLogger;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;


public final class MatchLineupTeamTable extends AbstractTable {

	/** tablename **/
	public final static String TABLENAME = "MATCHLINEUPTEAM";
	
	protected MatchLineupTeamTable(JDBCAdapter  adapter){
		super(TABLENAME, adapter);
	}

	@Override
	protected void initColumns() {
		columns = new ColumnDescriptor[]{
				new ColumnDescriptor("MatchID",Types.INTEGER,false),
				new ColumnDescriptor("MatchTyp",Types.INTEGER,false),
				new ColumnDescriptor("Erfahrung",Types.INTEGER,false),
				new ColumnDescriptor("TeamName",Types.VARCHAR,false,256),
				new ColumnDescriptor("TeamID",Types.INTEGER,false),
				new ColumnDescriptor("StyleOfPlay",Types.INTEGER,false)
		};
	}

	MatchLineupTeam getMatchLineupTeam(int iMatchType, int matchID, int teamID) {
		MatchLineupTeam team;
		String sql;
		ResultSet rs;
		
		try {
			sql = "SELECT * FROM " + getTableName() + " WHERE MatchTyp = " + iMatchType + " AND MatchID = " + matchID + " AND TeamID = " + teamID;
			
			rs = adapter.executeQuery(sql);

			rs.first();

			team = new MatchLineupTeam(MatchType.getById(iMatchType), matchID, DBManager.deleteEscapeSequences(rs.getString("TeamName")),
										teamID, rs.getInt("Erfahrung"), rs.getInt("StyleOfPlay"));
			team.setLineup(DBManager.instance().getMatchLineupPlayers(matchID, teamID));
			
			team.setSubstitutions(new ArrayList<>(DBManager.instance().getMatchSubstitutionsByMatchTeam(iMatchType, teamID, matchID)));
			
		} catch (Exception e) {
			HOLogger.instance().log(getClass(),"DB.getMatchLineupTeam Error" + e);
			team = null;
		}

		return team;
	}

	void storeMatchLineupTeam(MatchLineupTeam team, int matchID) {
		if (team != null) {
			final String[] where = { "MatchTyp", "MatchID" , "TeamID"};
			final String[] werte = { "" + team.getMatchType().getId(), "" + matchID, "" +team.getTeamID()};
			delete(where, werte);

			String sql;
			//saven
			try {
				//insert vorbereiten
				sql = "INSERT INTO "+getTableName()+" ( MatchTyp, MatchID, Erfahrung, TeamName, TeamID, StyleOfPlay ) VALUES(";
				sql += (team.getMatchType().getId() + "," +
						matchID + "," +
						team.getExperience() + ", '" +
						DBManager.insertEscapeSequences(team.getTeamName()) + "'," +
						team.getTeamID() + "," +
						team.getStyleOfPlay() + " )");
				adapter.executeUpdate(sql);

				//Store players
				for (int i = 0; i < team.getLineup().size(); i++) {
					
					((MatchLineupPlayerTable) DBManager.instance().getTable(
							MatchLineupPlayerTable.TABLENAME)).storeMatchLineupPlayer(
									(MatchLineupPlayer) team.getLineup().elementAt(i),
									matchID, team.getTeamID());
				}
				
				// Store Substitutions
				
				((MatchSubstitutionTable) DBManager.instance().getTable(MatchSubstitutionTable.TABLENAME))
						.storeMatchSubstitutionsByMatchTeam(team.getMatchType().getId(), matchID, team.getTeamID(), team.getSubstitutions());
				
			} catch (Exception e) {
				HOLogger.instance().log(getClass(),"DB.storeMatchLineupTeam Error" + e);
				HOLogger.instance().log(getClass(),e);
			}
		}
	}		
}
