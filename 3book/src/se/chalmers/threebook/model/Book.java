package se.chalmers.threebook.model;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Bitmap;

public class Book {
	private Long id;
	private String title;
	private List<Author> authors;
//	private Date published;
//	private Date lastRead;
//	private Format format;
	private String source;
//	private int rating;
	private Bitmap cover;

	private Position position;
	
	public enum Format {
		TXT, EPUB, PDF
	}
	
	public Book() {
		title = "";
		authors = new LinkedList<Author>();
	}

	public Long getId() {
		return id;
	}

	public Book setId(Long id) {
		this.id = id;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public Book setTitle(String title) {
		this.title = title;
		return this;
	}

	public List<Author> getAuthors() {
		return authors;
	}
	
	public String getSource() {
		return source;
	}

	public Book setSource(String source) {
		this.source = source;
		return this;
	}
	
	public Bitmap getCover() {
		return cover;
	}

	public Book setCover(Bitmap cover) {
		this.cover = cover;
		return this;
	}

	public Position getPosition() {
		return position;
	}

	public Book setPosition(Position position) {
		this.position = position;
		return this;
	}
	
	
}

