package se.chalmers.threebook.util;

public enum Constants {
	FILE_STORAGE_BASE_PATH("threebook/"),
	SETTINGS_MARGIN_TOP_BOTTOM_KEY("marginTopBottom"),
	SETTINGS_MARGIN_RIGHT_LEFT_KEY("marginRightLeft"),
	TEMP_BOOK_STORAGE_BASE_PATH(FILE_STORAGE_BASE_PATH.value() + "tmp/"),
	SETTINGS_FONT_SIZE("fontSize"),
	SETTINGS_FONT_COLOR("fontColor"),
	SETTINGS_BACKGROUND_COLOR("backgroundColor");
	private String value;
	
	private Constants(String val){
		this.value = val;
	}
	
	public String value(){return value;}
}
