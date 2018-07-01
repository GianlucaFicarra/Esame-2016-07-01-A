package it.polito.tdp.formulaone.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

import it.polito.tdp.formulaone.model.Circuit;
import it.polito.tdp.formulaone.model.Constructor;
import it.polito.tdp.formulaone.model.Driver;
import it.polito.tdp.formulaone.model.DriverSeasonResult;
import it.polito.tdp.formulaone.model.Season;


public class FormulaOneDAO {

	//metodi generici per ottenere tutte le info
	public List<Integer> getAllYearsOfRace() {
		
		String sql = "SELECT year FROM races ORDER BY year" ;
		
		try {
			Connection conn = ConnectDB.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			ResultSet rs = st.executeQuery() ;
			
			List<Integer> list = new ArrayList<>() ;
			while(rs.next()) {
				list.add(rs.getInt("year"));
			}
			
			conn.close();
			return list ;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Query Error");
		}
	}
	
	public List<Season> getAllSeasons() {
		
		String sql = "SELECT year, url FROM seasons ORDER BY year" ;
		
		try {
			Connection conn = ConnectDB.getConnection() ;

			PreparedStatement st = conn.prepareStatement(sql) ;
			
			ResultSet rs = st.executeQuery() ;
			
			List<Season> list = new ArrayList<>() ;
			while(rs.next()) {
				list.add(new Season(rs.getInt("year"), rs.getString("url"))) ;
			}
			
			conn.close();
			return list ;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null ;
		}
	}
	
	public List<Circuit> getAllCircuits() {

		String sql = "SELECT circuitId, name FROM circuits ORDER BY name";

		try {
			Connection conn = ConnectDB.getConnection();

			PreparedStatement st = conn.prepareStatement(sql);

			ResultSet rs = st.executeQuery();

			List<Circuit> list = new ArrayList<>();
			while (rs.next()) {
				list.add(new Circuit(rs.getInt("circuitId"), rs.getString("name")));
			}

			conn.close();
			return list;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Query Error");
		}
	}
	
	public List<Constructor> getAllConstructors() {

		String sql = "SELECT constructorId, name FROM constructors ORDER BY name";

		try {
			Connection conn = ConnectDB.getConnection();

			PreparedStatement st = conn.prepareStatement(sql);

			ResultSet rs = st.executeQuery();

			List<Constructor> constructors = new ArrayList<>();
			while (rs.next()) {
				constructors.add(new Constructor(rs.getInt("constructorId"), rs.getString("name")));
			}

			conn.close();
			return constructors;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Query Error");
		}
	}

	/*metodo per prendere tutti id e nomi dei drivers  che gareggiano in una particolare
	 * stagione, cioè un anno preso in races, per passare da
	 * races a driverers passo da results (vedi tabelle)*/
	
	//uso implementazione idmap
	public List<Driver> getAllDriversBySeason(Season s,  DriverIdMap driverIdMap) {

		String sql = "Select DISTINCT drivers.driverId, forename, surname\n" + 
				"from drivers, races, results\n" + 
				"where races.year = ?\n" + 
				"and results.raceId = races.raceId\n" + 
				"and results.driverId = drivers.driverId\n"
				+ "and results.position is not null";


		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, s.getYear()); //setto anno
			ResultSet rs = st.executeQuery();

			//creo lista piloti dove aggiungo quelli presi dal DB
			List<Driver> drivers = new ArrayList<>();
			while (rs.next()) {
				drivers.add(driverIdMap.get(new Driver(rs.getInt("driverId"), rs.getString("forename"), rs.getString("surname"))));
			}

			conn.close();
			return drivers;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Query Error");
		}
	}
	
	
	/* -Dato anno e date due driver presi dala combinazione di due istante di result con race, 
	 *  valuto quante volte il primo driver è arrivato prima del secodno, 
	 *  join tra races e result è per selezonare una stagione
	 *  join tra due result per calcolare le coppie di piloti che si trovano nella stessa tabella(doppia istanza) 
	 *  r1>r2 perchè volevo tutti i casi in cui il driver della prima tabella fosse arrivato davanti al driver della seconda tabella
	 * -Voglio sapere quante sono le gare in cui r1 prima di r2 cosi le raggruppo in base alla coppia di id 
	 *  pongo clausola che la posizione nel torneo non sia nulla, cioè che si sia ritirato o che abbia avuto incidente
	 *  la query per creare l'arco restituisce i piloti ed il peso, creo classe apposita dove salvare queste info (DriverSeasonResult)*/
	
	//gli passo la stagione di cui voglio i risultati, e idmap per mettere in relazione id pilota con oggetto pilota
    public List<DriverSeasonResult> getDriverSeasonResults(Season s, DriverIdMap driverIdMap) {
		
		String sql = "select r1.driverId as d1, r2.driverId as d2, count(*) as cnt\n" + 
				"from results as r1, results as r2, races\n" + 
				"where r1.raceId = r2.raceId\n" +    //join tra due istanze della stessa tabella
				"and races.raceId = r1.raceId\n" +  //per collegare con races e particolare anno
				"and races.year = ?\n" + 
				"and r1.position is not null\n" + 
				"and r2.position is not null\n" + 
				"and r1.position < r2.position\n" + 
				"group by d1, d2";

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			st.setInt(1, s.getYear());
			ResultSet rs = st.executeQuery();

			List<DriverSeasonResult> result = new ArrayList<>();
			while (rs.next()) {
				
				//esplicito i driver per verificare se esiste realmente pilota
				Driver d1 = driverIdMap.get(rs.getInt("d1"));
				Driver d2 = driverIdMap.get(rs.getInt("d2"));
				int cnt = rs.getInt("cnt");
				
				//CONTROLLO: esplicito i driver per verificare se esiste realmente pilota
				if (d1 == null || d2 == null) { //uno dei due non esiste
					System.err.format("Skipping %d %d\n", rs.getInt("d1"), rs.getInt("d2"));
				} else {
					//se dati consistenti aggiungo oggetto a result che mi servirà per creare l'arco
					result.add(new DriverSeasonResult(d1, d2, cnt));
				}
			}

			conn.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("SQL Query Error");
		}
	}
	
}
