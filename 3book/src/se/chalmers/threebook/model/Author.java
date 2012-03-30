package se.chalmers.threebook.model;

public class Author {
	private long id;
	private String FirstName;
	private String LastName;
	public long getId() {
		return id;
	}
	public Author setId(long id) {
		this.id = id;
		return this;
	}
	public String getFirstName() {
		return FirstName;
	}
	public Author setFirstName(String firstName) {
		FirstName = firstName;
		return this;
	}
	public String getLastName() {
		return LastName;
	}
	public Author setLastName(String lastName) {
		LastName = lastName;
		return this;
	}
	
	
}
