package parser.epub;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;

public class EpubChapter {

	// Tag enum used to request different parse forms. 
	enum ChapterForm{
		XHTML,
		BODY;
	}
	
	private final Book book; // The underlying book
	private final Resource source;
	private String xhtml = "";
	private String body = "";
	
	public EpubChapter (Book book, Resource source){
		this.book = book;
		this.source = source;
	}
	
	private String getBodyContents(String xhtml){
    	int end = xhtml.indexOf("</body>");
		int st1 = xhtml.indexOf("<body");
		int st2 = xhtml.indexOf(">", st1);
		return xhtml.substring(st2+1, end);
    }
	
	public String getXhtml(){
		ensureParsed(ChapterForm.XHTML);
		return xhtml;
	}
	
	public String getBody(){
		ensureParsed(ChapterForm.BODY);
		return body;
	}
	
	public String getEncoding(){
		return source.getInputEncoding();
	}
	
	public String getMediaType(){
		return source.getMediaType().getName();
	}
	
	private void ensureParsed(ChapterForm form){
		switch (form){
		case XHTML:
			if (xhtml.equals("")){
				xhtml = "a"; 
				// PARSE ROUTINE
			}
			break;
			
		case BODY:
			if (body.equals("")){
				ensureParsed(ChapterForm.XHTML);
				body = getBodyContents(xhtml);
			}
			break;
		}
	
	}
}
