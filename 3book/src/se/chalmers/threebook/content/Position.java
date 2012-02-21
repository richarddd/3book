package se.chalmers.threebook.content;

public interface Position {
	public int getPercentage();
	public String persistableString();
	public Position parsePersistableString();
}
