package se.chalemers.threebook.content;

public interface Position {
	public int getPercentage();
	public String persistableString();
	public Position parsePersistableString();
}
