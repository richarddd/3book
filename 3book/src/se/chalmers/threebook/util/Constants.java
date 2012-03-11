package se.chalmers.threebook.util;

public enum Constants {
	FILE_STORAGE_BASE_PATH("threebook/"),
	TEMP_BOOK_STORAGE_BASE_PATH(FILE_STORAGE_BASE_PATH.value() + "tmp/");
	
	private String value;
	
	private Constants(String val){
		this.value = val;
	}
	
	public String value(){return value;}
}
