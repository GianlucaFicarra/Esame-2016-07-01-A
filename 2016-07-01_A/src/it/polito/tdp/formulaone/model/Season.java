package it.polito.tdp.formulaone.model;

public class Season {

	private int year;// prima era oggetto year ma non ci piace lo cambio in int
	private String url;

	public Season(int year, String url) {
		this.year = year;
		this.url = url;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public String toString() {
		return String.valueOf(year);
		//year è un intero, uso String.valueOf() per convertirlo in string
	}

}
