package se.chalmers.threebook.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Book {
	private Long id;
	private String title;
	private List<Author> authors;
//	private Date published;
//	private Date lastRead;
//	private Format format;
//	private File source;
//	private int rating;
//	private File cover;
//	private ContentStream contentStream;
	
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

//	public Date getPublished() {
//		return published;
//	}
//
//	public Book setPublished(Date published) {
//		this.published = published;
//		return this;
//	}
//
//	public Date getLastRead() {
//		return lastRead;
//	}
//
//	public Book setLastRead(Date lastRead) {
//		this.lastRead = lastRead;
//		return this;
//	}
//
//	public Format getFormat() {
//		return format;
//	}
//
//	public Book setFormat(Format format) {
//		this.format = format;
//		return this;
//	}
//
//	public File getSource() {
//		return source;
//	}
//
//	public Book setSource(File source) {
//		this.source = source;
//		return this;
//	}
//
//	public int getRating() {
//		return rating;
//	}
//
//	public Book setRating(int rating) {
//		this.rating = rating;
//		return this;
//	}
//
//	public File getCover() {
//		return cover;
//	}
//
//	public Book setCover(File cover) {
//		this.cover = cover;
//		return this;
//	}
//
//	public ContentStream getContentStream() {
//		return contentStream;
//	}
//
//	public Book setContentStream(ContentStream contentStream) {
//		this.contentStream = contentStream;
//		return this;
//	}
}
