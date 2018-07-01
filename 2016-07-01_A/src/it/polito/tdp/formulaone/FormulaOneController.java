package it.polito.tdp.formulaone;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import it.polito.tdp.formulaone.model.Driver;
import it.polito.tdp.formulaone.model.Model;
import it.polito.tdp.formulaone.model.Season;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class FormulaOneController {

	Model model;

	@FXML
	private ResourceBundle resources;

	@FXML
	private URL location;

	@FXML
	private ComboBox<Season> boxAnno; 
	/*riempio menu a tendina:
	 * dal model carico metodo dao che restituisce tutte la stagioni
	 * lo restituisco al controller che lo visualizza*/

	@FXML
	private TextField textInputK;

	@FXML
	private TextArea txtResult;

	@FXML
	void doCreaGrafo(ActionEvent event) {
		
		try { //per gestire tutte le eccezioni
			
			Season s = boxAnno.getValue();//utente seleziona anno
			if (s == null) { //controllo se anno nullo cioè se non seleziono valori
				System.out.println("Selezionare una stagione!");
				txtResult.setText("Selezionare una stagione!");
				return;
			}
			
			model.creaGrafo(s);
			Driver d = model.getBestDriver(); //salvo il miglior pilota
			txtResult.setText(d.toString()); //stampo valore pilota
			
		} catch(RuntimeException e) {//unica che viene fuori è quella della connessione al db
			e.printStackTrace();
			System.out.println("Errorre di connessione al DB");
			txtResult.setText("Errore di connessine al DB!");
		}
	}

	/*RICORSIONE:
	 * non controlli particolari, ma trovo tutti i sottoinsiemi di insieme
	 * iniziale di piloti, cercarre il migliore di dimensione k che
	 * minimizza il tasso di sconfitta, minimizza la somma dei pesi archi entranti del sottoinsieme considerato: 
	 * num tot di vittorie di un pilota non appartentenete al team su un qualsiasi pilota appartenente al team  */
	@FXML
	void doTrovaDreamTeam(ActionEvent event) {
		try {
				try {
					
					int k = Integer.parseInt(textInputK.getText());
					if (k <= 0) {
						txtResult.setText("inserire K > 0");
						return;
					}
					
					List<Driver> drivers = model.getDremTeam(k);
					txtResult.setText(drivers.toString());
				
				} catch (NumberFormatException e) {//se non metto numero
					txtResult.setText("inserire K > 0");
					return;
				}
			

		} catch (RuntimeException e) {
			e.printStackTrace();
			System.out.println("Errorre di connessione al DB");
			txtResult.setText("Errore di connessine al DB!");
		}
	}

	@FXML
	void initialize() {
		assert boxAnno != null : "fx:id=\"boxAnno\" was not injected: check your FXML file 'FormulaOne.fxml'.";
		assert textInputK != null : "fx:id=\"textInputK\" was not injected: check your FXML file 'FormulaOne.fxml'.";
		assert txtResult != null : "fx:id=\"txtResult\" was not injected: check your FXML file 'FormulaOne.fxml'.";

	}

	public void setModel(Model model) {
		this.model = model;
		boxAnno.getItems().addAll(model.getAllSeasons());
		/*anno dato dall elenco di seasons, collegata a race
		 * dove ho l'elenco di gare in quel anno*/
	}
}
