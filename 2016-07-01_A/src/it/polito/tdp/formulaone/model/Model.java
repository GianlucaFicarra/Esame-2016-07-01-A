package it.polito.tdp.formulaone.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.formulaone.db.DriverIdMap;
import it.polito.tdp.formulaone.db.FormulaOneDAO;

public class Model {
	
	private FormulaOneDAO fonedao; //collegamento al dao
	private SimpleDirectedWeightedGraph<Driver, DefaultWeightedEdge> grafo;
	private DriverIdMap driverIdMap;
	
	//per secodno punto
	private List<Driver> bestDreamTeam;
	private int bestDreamTeamValue;
	
	public Model() {
		fonedao = new FormulaOneDAO();
		driverIdMap = new DriverIdMap();
	}

	//modo per collgare metodo dao per accedere a season col controler
	public List<Season> getAllSeasons() {
		return fonedao.getAllSeasons();
	}

	public void creaGrafo(Season s) { //semplice, diretto e pesato
	
		//creo sempre nuovi oggetti grafo ogni volta che clicco su oulsante
		grafo = new SimpleDirectedWeightedGraph<Driver, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		//piloti che hanno gareggiato in quella stagione sono i vertici
		//archi sono le vittorie di un pilota sugli altri per tutte le gare della stagione
		List<Driver> drivers = fonedao.getAllDriversBySeason(s, driverIdMap);
		Graphs.addAllVertices(grafo, drivers);
		
		
		/*PER CREARE ARCHI TRA DUE PILOTI HO DUE ALTERNATIVE:
		   1)itero sulle due liste di piloti, considero ogni coppia di piloti (se dievrsi) e mi chiedo 
             quale sia il da dare a questo arco
           2)faccio fare tutto al DB e mi faccio restituire con quanti archi devo collegarli,
             cioe quante volte un pilota è arrivato prima della altro nelle gare di un anno specifico,
             sommando le volte ottengo il peso dell'arco che li collega
             --->
	             l'info sulla posizione di arrivo di un pilota in una gara viene salvata nella tabella result, 
	             seleziono numero di vittorie di ogni pilota su ciascun'altro pilota, considerando tutte le gare della stagione,
	             sommandoli otteno il peso dell'arco che collega il pilota A che ha battuto B con quel peso
	             OSS: teoria se A batte B 5 volte e B batte A 2 volte questi sono collegati da due archi con direzioni e pesi dievrsi   
        */
		
		
		//itero sul risultato ritornato dal dao e per ogni oggetto torno arco
		for (DriverSeasonResult dsr : fonedao.getDriverSeasonResults(s, driverIdMap)) {
			Graphs.addEdge(grafo, dsr.getD1(), dsr.getD2(), dsr.getCounter());
		}
		
		//stampa di controllo sul grafo creato
		System.out.format("Grafo creato: %d archi, %d nodi\n", grafo.edgeSet().size(), grafo.vertexSet().size());
		
		
	}
	
	
	//trovare miglior pilota: ricerco archi uscenti e ne sommo pesi, meno somma pesi archi entranti, prendo quello con differenza più alta
	public Driver getBestDriver() {
		
		//si riferisce al grafo creato, quindi non deve essere nullo
		if (grafo == null) {
			new RuntimeException("Creare il grafo!");
		}
		
		// Inizializzazione
		Driver bestDriver = null;
		int best = Integer.MIN_VALUE; //valore relativo al miglior driver= ricerca massimo
		
		for (Driver d : grafo.vertexSet()) {
			int sum = 0;
			
			//sfrutto metodi grafi per sapere lista archi uscenti ed entranti dato vertice
			
			// Itero sugli archi uscenti
			for (DefaultWeightedEdge e : grafo.outgoingEdgesOf(d)) {
				sum += grafo.getEdgeWeight(e);
			}
			
			// Itero sugli archi entranti
			for (DefaultWeightedEdge e : grafo.incomingEdgesOf(d)) {
				sum -= grafo.getEdgeWeight(e);
			}
			                 //al primo ciclo
			if (sum > best || bestDriver == null) {
				bestDriver = d;
				best = sum;
			}
		}
		
		if (bestDriver == null) {
			new RuntimeException("BestDriver not found!");
		}
		
		return bestDriver;
	}
	
	
	public List<Driver> getDremTeam(int k) { //valore k= dimensione
		
		//inizzializzo la miglior combinazione di piloti, ed il valore associato
		bestDreamTeam = new ArrayList();
		bestDreamTeamValue = Integer.MAX_VALUE;
		
		//passo alla ricorsiva step, val max della ricorsione(al max k livelli) e soluz parziale
		recursive(0, new ArrayList<Driver>(), k); 
		
		return bestDreamTeam; //ritorno lista migliore
	}

	
	//trova tutti i sottoinsiemi di dimensione k, ho al max k livelli di ricrsione
	private void recursive(int step, ArrayList<Driver> tempDreamTeam, int k) {
		
		// condizione di terminazione
		if (step >= k) { //ho raggiunto la dimensione della squdra ricercata
			if (evaluate(tempDreamTeam) < bestDreamTeamValue) {//confronto valore attuola al migliore
				bestDreamTeamValue = evaluate(tempDreamTeam);
				bestDreamTeam = new ArrayList<>(tempDreamTeam); //deepcopy
				return;
			}
		}
		
		//genero soluz parziale: piloti presenti nel grafo
		for (Driver d : grafo.vertexSet()) {
			if (!tempDreamTeam.contains(d)) {//se squadra non contiene pilota 
				tempDreamTeam.add(d);  //lo aggiungo
				recursive(step+1, tempDreamTeam, k); //chiamo la ricorsiva
				tempDreamTeam.remove(d); //lo rimuovo
			}
			//OSS hash e equals per add e remove
		}
		
	}
	
	//in input prende lista driver e ne da il suo valore complessivo
	private int evaluate(ArrayList<Driver> tempDreamTeam) { //valuta la soluzione per vedere se sia la migliore
		int sum = 0;
		
		/*se uso spesso contains, e liste parecchio grosse,
		 * contains col set usa concetto della hashmap, quindi
		 *posso usare il SET che ottimizza le performances,
		 *altrimenti senza le set l'if sotto sarebbe:
		 *if(tempDreamTeam.contains(grafo.getEdgeTarget(e)))*/
		
		Set<Driver> in = new HashSet<Driver>(tempDreamTeam);
		Set<Driver> out = new HashSet<Driver>(grafo.vertexSet());
		out.removeAll(in);
		
		//considero gli archi in entrata sul set di vertici:
		for (DefaultWeightedEdge e : grafo.edgeSet()) { //itero tutti gli archi
			//squadra contiene il vertice di destinazione(getEdgeTarget) dell'arco?
			if (out.contains(grafo.getEdgeSource(e)) && in.contains(grafo.getEdgeTarget(e))) {
				sum += grafo.getEdgeWeight(e);//se lo contiene ne conto il peso dell'arco
			}
		}
		return sum; //0 se non ho archi enrata, o pari al risultato
	}

}
