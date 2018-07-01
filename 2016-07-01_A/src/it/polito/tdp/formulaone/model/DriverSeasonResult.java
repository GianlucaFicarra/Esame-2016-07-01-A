package it.polito.tdp.formulaone.model;

/*la query per creare l'arco restituisce i piloti ed il peso
 * creo classe apposita dove salvare queste info:*/

public class DriverSeasonResult {

	private Driver d1; //sorgente
	private Driver d2; //destinazione
	private int counter; //peso

	public DriverSeasonResult(Driver d1, Driver d2, int counter) {
		this.d1 = d1;
		this.d2 = d2;
		this.counter = counter;
	}

	public Driver getD1() {
		return d1;
	}

	public void setD1(Driver d1) {
		this.d1 = d1;
	}

	public Driver getD2() {
		return d2;
	}

	public void setD2(Driver d2) {
		this.d2 = d2;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

}
