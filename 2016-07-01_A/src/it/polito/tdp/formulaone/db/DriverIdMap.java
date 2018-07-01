package it.polito.tdp.formulaone.db;

//creo idmap per mettere in relazione id con oggetto, per il metodo dao (getDriverSeasonResults)

import java.util.HashMap;

import it.polito.tdp.formulaone.model.Driver;


/*IDMAP DIVERSA PER CAMBIARE : invece di creare nell idmap una mappa, 
 * e poi creare la relazione all'interno di questa mappa salvata come variabile privata
 * all'interno dell'oggetto, il driveridmap estende hashmap per evitare di implementare
 * alcuni metodi perchè sono già implementati*/

                               //ad un id associo oggetto tipo driver
public class DriverIdMap extends HashMap<Integer, Driver> {

	//ridefinisco metodo get che prende oggetto di tipo driver
	public Driver get(Driver driver) {
		Driver old = super.get(driver.getDriverId()); //chiamo get del padre con super
		if (old != null) {
			return old;
		}
		super.put(driver.getDriverId(), driver);
		return driver;
	}
	
}