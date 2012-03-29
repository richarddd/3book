package se.chalmers.threebook.model;

import java.io.File;
import java.util.Date;

import se.chalmers.threebook.content.ContentStream;

public class Book {
	private Long id;
	private String title;
//	private String author;
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

//	public String getAuthor() {
//		return author;
//	}
//
//	public Book setAuthor(String author) {
//		this.author = author;
//		return this;
//	}
//
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
