package se.chalmers.threebook.html;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import se.chalmers.threebook.R;
import se.chalmers.threebook.content.MyBook;
import se.chalmers.threebook.ui.RendererConfig;
import se.chalmers.threebook.ui.RendererConfig.RenderConfigListener;
import se.chalmers.threebook.util.Helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

/**
 * 
 * TODO: Fix so that the renderer doesn't populate the pagePos list if the page
 * already has been rendered.
 * 
 */

public class HtmlRenderer implements RenderConfigListener {

	private static final int RENDER_LIST_INITIAL_CAPACITY = 2048;
	private static final int WORD_ROW_LIST_INITIAL_CAPACITY = 10;
	public static final int BOOK_OBJECT_HEIGHT_DP = 191;
	public static final int IMAGE_HEIGHT_LIMIT_DP = 400;

	private String htmlSource = "";
	 
	private String bookName;
	private PageTracker tracker = PageTracker.instance(); // tracks source word counts 
	private RendererConfig config = RendererConfig.instance();
	// shared objects:
	
	private int imageHeightLimit;
	private int bookObjectHeight;

	private int viewWidth;
	private int viewHeight;
	
	private File cacheDir;
	private int baseTextSize = 26;
	private int minWordSpace = 2;
	private int rowMargin = (int) (baseTextSize * 0.2);
	private int heightMargin = 10;
	private int widthMargin = 10;

	private float h1TextSize = (baseTextSize * 2);
	private float h2TextSize = (float) (baseTextSize * 1.5);
	private float h3TextSize = (float) (baseTextSize * 1.2);
	private float h4TextSize = (baseTextSize * 1);
	private float h5TextSize = (float) (baseTextSize * 0.85);
	private float h6TextSize = (float) (baseTextSize * 0.7);
	
	private ArrayList<RenderElement> printObjects; // NOTE: this is null-checked to 
												   // help laziness. handle with care!
	private Paint paint;
	private OnDrawCompleteListener onDrawCompleteListener;
	private String tag = "HtmlRenderer";
	
	private List<Integer> objsByPage; // provided and kept by the tracker
	private int objectsIterated = 0;	
	private int sourceIdent; // integer that uniquely identifies the current source

	// TODO ensure that it only contains TOC-valid IDs 
	// TODO enable notification of listeners upon rendering a TOC-valid ID
	// This is anchor --> "object number at which the anchor resides" 
	private Map<String, Integer> idMap = new HashMap<String, Integer>(); 

	public static class OnDrawCompleteListener{
		public void drawComplete(){

		}
	}
	
	/**
	 * Returns an empty renderer with the same basic settings as this one
	 * @return an empty renderer with the same basic settings as this one
	 */
	public HtmlRenderer getBlankRenderer(){
		return new HtmlRenderer(cacheDir, viewWidth, viewHeight, bookObjectHeight, imageHeightLimit, bookName);
	}
	
	private void renderNextPage(){
		// clarity-enhancing method that also can help if impl changes later.
		getRenderedPage(objsByPage.size()-1);
	}

	/**
	 * (EXPENSIVE) Returns the page number that ends the requested source
	 * 
	 * CALLER BEWARE: this method will, unless the end of source has been
	 * rendered before, render the entire passed source. If the source is
	 * of any respectable size, this will take a while. 
	 * 
	 */
	public int getEosPageNumber() {
		if (tracker.getEosPageNum(sourceIdent) > 0){
			// we know where it ends, fine!
			return tracker.getEosPageNum(sourceIdent);
		}
		int page = objsByPage.size()-1; // the last rendered page
		while (hasPage(page)){page++;} // render until we get "false"
		
		// TODO: remove this debug code once we're sure of the functionality
		if (tracker.getEosPageNum(sourceIdent) < 0){
			Log.d(tag, "MASSIVE WARNING: tried to get eos pNum but it didn't really work, we got -1");
		}
		
		Log.d(tag, "getEosPageNumber has worked hard! the source ends on page: " + tracker.getEosPageNum(sourceIdent) + ", while objSize is : " + objsByPage.size() );
		
		return tracker.getEosPageNum(sourceIdent);
	}
	
	/**
	 * Checks whether a given pNum exists for the current source
	 * 
	 * CALLER BEWARE: this method will render until it can give a definitive answer. 
	 * Calling this for high pNums when only low have been rendered is a 
	 * costly operation - the intended usage is to call this only as far
	 * as needed for buffering purposes, i.e look ahead by at most 3-ish.   
	 * @param pageNum the pNum to determine existence of
	 * @return whether there's a page pNum
	 */
	public boolean hasPage(int pageNum){
		int eos = tracker.getEosPageNum(sourceIdent);
		if (eos > pageNum){
			// we know where the end is, and it's not there. Simple.
			return true; 
		} else if (eos > 0 && eos <= pageNum){
			// we know where the end is, and it's before your target
			return false; 
		} else {
			// we have no idea, so we'll recurse until we know.
			renderNextPage();
			return hasPage(pageNum);
		}
	}

	public HtmlRenderer(File cacheDir, int viewWidth, int viewHeight, int bookObjectHeight, int imageHeightLimit, String bookName){
		this.bookName = bookName;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.bookObjectHeight = bookObjectHeight;
		this.cacheDir = cacheDir;
		this.imageHeightLimit = imageHeightLimit;

		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setTextSize(baseTextSize);
	}


	public void setOnDrawCompleteListener(OnDrawCompleteListener l){
		this.onDrawCompleteListener = l;
	}

	public String getHtmlSource(){
		return htmlSource;
	}
	/**
	 * Returns true if the pNum passed is the final page in the source
	 * @param pageNumber 
	 * @return
	 */
	public boolean isEndOfSource(int pageNumber) {
		// refactor this once we're sure of the functionality
		Log.d(tag, "isEndOfSource called. PageNumber: " + pageNumber + ", source-end page number: " + tracker.getEosPageNum(sourceIdent));
		int eosPnum = tracker.getEosPageNum(sourceIdent);
		if (eosPnum < 0){
			// this means we don't know where we are; most likely at the beginning.
			Log.d(tag, "isEndOfSource: FALSE (we don't know where the thing ends)");
			return false;
		}
		if (eosPnum <= pageNumber){
			Log.d(tag, "At end of source " + sourceIdent + ", page " + pageNumber);
			if (tracker.getEosPageNum(sourceIdent) < pageNumber){ Log.d(tag, "MONKEY-WARNING: eos *less* than page number, this is probably bad. pn/eos: " + pageNumber + "/" + tracker.getEosPageNum(sourceIdent)); }
			Log.d(tag, "isEndOfSource: TRUE");
			return true;
		}
		Log.d(tag, "isEndOfSource: FALSE (we know where it ends tho)");
		return false;
	}
	
	public void setHtmlSource(String htmlSource, int sourceIdentifier) {
		Log.d(tag, "Changing htmlSource identifier " + sourceIdentifier);
		this.htmlSource = htmlSource;
		this.sourceIdent = sourceIdentifier;
		this.objectsIterated = 0;
		objsByPage = tracker.getPageStartList(sourceIdent);
	}

	public int getBaseTextSize(){
		return baseTextSize;
	}

	public void setBaseTextSize(int baseTextSize) {
		if (this.baseTextSize != baseTextSize) invalidate();
		this.baseTextSize = baseTextSize;
	}

	public int getMinWordSpace(){
		return minWordSpace;
	}

	public void setMinWordSpace(int minWordSpace) {
		if (this.minWordSpace != minWordSpace) invalidate();
		this.minWordSpace= minWordSpace;
	}

	public int getRowMargin(){
		return rowMargin;
	}

	public void setRowMargin(int rowMargin) {
		if (this.rowMargin != rowMargin) invalidate();

		this.rowMargin = rowMargin;
	}

	public int getHeightMargin(){
		return heightMargin;
	}

	public void setHeightMargin(int heightMargin) {
		if (this.heightMargin != heightMargin) invalidate();
		this.heightMargin = heightMargin;
	}

	public int getWidthMargin(){
		return widthMargin;
	}

	public void setWidthMargin(int widthMargin) {
		if (this.widthMargin != widthMargin) invalidate();
		this.widthMargin = widthMargin;
	}
	
	private void invalidate(){
		tracker.invalidate();
		objsByPage = tracker.getPageStartList(sourceIdent); // get new object to work with
		printObjects = null;
	}

	private void parseHtml(){
		objsByPage.add(0); // draw first page from word zero

		long t1 = System.currentTimeMillis();
		Document doc = Jsoup.parse(htmlSource);

		printObjects = new ArrayList<RenderElement>(RENDER_LIST_INITIAL_CAPACITY);

		Node rootNode = doc.body();
		Node node = rootNode;
		while(node != null){

			if(node instanceof Element){
				if(((Element) node).tagName().equals("p")){
					printObjects.add(new BreakElement(baseTextSize));
				}

				// Detect elements with IDs and squirrel them away
				String id = ((Element) node).id();
				if(id != null && id != ""){
					idMap.put(id, printObjects.size() + 1); // XXX +1 could mean
															// crazy EOF bugs if
															// nothing is
															// rendered on
															// last-anchor ?
				}
			}

			if(node.childNodes().size() != 0){
				node = node.childNodes().get(0);
			}else{ // leaf
				if(node instanceof TextNode){
					TextNode tNode = (TextNode) node;
					if(!tNode.text().equals("")){

						String parentTag = ((Element) tNode.parent()).tagName();
						StyleFlag flag = StyleFlag.NORMAL;

						for(StyleFlag f : StyleFlag.values()){
							if(f.name().equals(parentTag.toUpperCase())){
								flag = StyleFlag.valueOf(parentTag.toUpperCase());
							}
						}
						String[] words = tNode.text().split(" ");
						for(String word : words){
							printObjects.add(new TextElement(word, flag));
						}
						switch(flag){
						case H1:
							printObjects.add(new BreakElement(0));
							break;
						case H2:
							printObjects.add(new BreakElement(0));
							break;
						case H3:
							printObjects.add(new BreakElement(0));
							break;
						case H4:
							printObjects.add(new BreakElement(0));
							break;
						case H5:
							printObjects.add(new BreakElement(0));
							break;
						case H6:
							printObjects.add(new BreakElement(0));
							break;
						}
					}
				}else if(node instanceof Element){
					Element eNode = (Element) node;
					if(eNode.tagName().equals("br")){
						printObjects.add(new BreakElement(0));
					}

					if(eNode.tagName().equals("img")){
						String sWidth = eNode.attr("width").replace("[A-z]*",""); //TODO fix something better here to remove 250px
						String sHeight = eNode.attr("height").replace("[A-z]*","");
						//int width = sWidth.equals("")?-1:Integer.parseInt(sWidth);
						//int height = sHeight.equals("")?-1:Integer.parseInt(sHeight);
						printObjects.add(new ImageElement(eNode.attr("src"), 0, 0)); //TODO fix correct with and height;
					}

				}

				while(node.nextSibling() == null && node != rootNode){
					node = node.parent();
				}
				node = node.nextSibling();
			}

		}

		printObjects.add(new BreakElement(0)); // last word fix		

		long t2 = System.currentTimeMillis();
		Log.d(tag, "Parsing HTML took " + (t2 - t1) + "ms");

	}

	private void setStyle(StyleFlag flag){
		switch(flag){
		case H1:
			paint.setTextSize(h1TextSize);
			paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			break;
		case H2:
			paint.setTextSize(h2TextSize);
			paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			break;
		case H3:
			paint.setTextSize(h3TextSize);
			paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			break;
		case H4:
			paint.setTextSize(h4TextSize);
			paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			break;
		case H5:
			paint.setTextSize(h5TextSize);
			paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			break;
		case H6:
			paint.setTextSize(h6TextSize);
			paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			break;
		case EM:
			paint.setTextSize(baseTextSize);
			paint.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
			break;
		case NORMAL:
			paint.setTextSize(baseTextSize);
			paint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			break;
		}

	}

	/**
	 * Returns the page number at which a certain ID resides
	 * 
	 * The behaviour of this function is undefined if the ID is not part of the
	 * underlying HTML source
	 * @param id the id to search for
	 * @return the page at which it resides
	 */
	public int getPageNumber(String id){
		Log.d(tag, "getPageNumber(String id) called with id: " + id);
		// null implies that we dont know where in the file this ID is - 
		// so we'll return 0, the beginning of the file.
		int anchorWord = idMap.get(id) != null ? idMap.get(id) : 0;  
		int pageNumber = 0;
		
		Log.d(tag, "Going to anchor! AnchorWord for anchor " + id + " is: " + anchorWord);
		
		for (int i = 0, size = objsByPage.size(); i < size; i++){
			// The anchor is on the page before the page which begins with a 
			// larger word count than what had been parsed when the anchor was found.
			if (objsByPage.get(i) >= anchorWord){
				// If the page begins on exactly the desired word, we've found the page
				// otherwise, we want the preceding page.
				pageNumber = objsByPage.get(i) == anchorWord ? i : (i-1);
				break; // the page number is found, we're done. 
			}
		}
		
		if (pageNumber > 0){ // page already rendered
			getRenderedPage(pageNumber);
			return pageNumber; // XXX expensive - need to fix cache. 
		} else { // page is not rendered yet
			//Render one page at a time until the last rendered word 
			// is greater than the word we're searching for.
			while (anchorWord > objsByPage.get(objsByPage.size()-1)){
				getRenderedPage(objsByPage.size()-1);
			}

			// TODO once caching is implemented this should return -1 
			Log.d(tag, "Found anchor, returning page number: " + (objsByPage.size()-2));
			return objsByPage.size()-2; // XXX
		} 
		 
		
	}	
	
	private int renderTextRow(Canvas canvas, List<WordPosition> wordList, List<TextElement> words, int totalRowHeight, boolean justified, int curWordSpace, float rowCurWidth, float wordCurWidth){
		int count = words.size();
		int rowHeight = 0;
		float lastWidth = 0;	
		float glue = curWordSpace;
		int diffFix = 6;
		
		if(justified){
			glue = ((viewWidth - (2 * widthMargin)) - (rowCurWidth - wordCurWidth - (curWordSpace * (count+1))))
				/ (count-1);
		}
		
		for(TextElement e : words){
			setStyle(e.getStyle());
			rowHeight = (int) (paint.getTextSize() + rowMargin);
			float xPos = widthMargin + lastWidth;
			float yPos = totalRowHeight + rowHeight;
			canvas.drawText(e.getText(), xPos, yPos, paint);
			float wordWidth = paint.measureText(e.getText());
			lastWidth += wordWidth + glue;
			wordList.add(new WordPosition(xPos, yPos-paint.getTextSize()+diffFix, xPos+wordWidth, yPos+diffFix, e.getText()));
			wordList.add(new WordPosition(xPos+wordWidth, yPos-paint.getTextSize()+diffFix, xPos+wordWidth+glue, yPos+diffFix," ")); //add space as well
			objectsIterated++;
		}
		
		words.clear();
		totalRowHeight += rowHeight;
		return totalRowHeight;
	}
	
	public RenderedPage getRenderedPage(int pageNumber){
		if(pageNumber < 0){
			throw new IllegalArgumentException("pageNumber must be > 0. Number was: " + pageNumber);
		}
		if (printObjects == null){
			parseHtml(); // helps laziness
		}
		
		if (pageNumber >= objsByPage.size()){
			// TODO: consider IAE here, though how would callers check the contract?
			// XXX experimental method to ignore look-ahead buffering - just ignore invaid calls
			Log.d(tag, "WARNING: page number requested is too big. pnum requested[0index]/pages rendered[1index]:" + pageNumber + "/" + objsByPage.size());
			// throw new IllegalArgumentException("")
		}

		long milis = System.currentTimeMillis();

		int drawFrom = objsByPage.get(pageNumber);

		List<WordPosition> charList = new ArrayList<WordPosition>();
		Map<Integer, RenderElement> specialObjectsMap = new HashMap<Integer, RenderElement>(5);

		Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_4444);

		Canvas canvas = new Canvas(bitmap);

		// TODO document the purpose of these variables
		objectsIterated = 0;
		float rowCurWidth = 0;
		int totalRowHeight = heightMargin;
		boolean firstWordInRow = true;
		boolean firstRow = true;
		int breakSize = 0;
		int curWordSpace = 0;
		
		if(printObjects == null){
			throw new NullPointerException(
					"HtmlRenderer: printObjects was null when asked to render, most likely HTML is not parsed yet!");
		}
		
		List<TextElement> currentRowWords = new ArrayList<TextElement>(WORD_ROW_LIST_INITIAL_CAPACITY);
		for(int i = drawFrom, size = printObjects.size(); i < size; i++){
			
			
			float wordCurWidth = 0;
			
			RenderElement curElement = printObjects.get(i);
			
			if(curElement instanceof BreakElement){		
				if(!currentRowWords.isEmpty()){		
					totalRowHeight = renderTextRow(canvas, charList, currentRowWords, totalRowHeight, false, curWordSpace, rowCurWidth, wordCurWidth);
					rowCurWidth = 0;
				}	
				breakSize = ((BreakElement) curElement).getSpan() + rowMargin;		
				totalRowHeight += breakSize;
				objectsIterated++; // this is also an object in the array
			}else if(curElement instanceof TextElement){		
				
				setStyle(((TextElement)curElement).getStyle());
				curWordSpace = (int) (paint.getTextSize() * 0.2);
				
				if(firstRow){ //first row fix
					rowCurWidth += curWordSpace;
					firstRow = false;
				}
				
				if(curWordSpace < minWordSpace){
					curWordSpace = minWordSpace;
				}
				wordCurWidth = paint.measureText(((TextElement)curElement).getText());
				rowCurWidth += wordCurWidth;
				if(!firstWordInRow){
					rowCurWidth += curWordSpace;
				}
				firstWordInRow = false;
				
				//TODO fix if the word does not fit screen
				if(wordCurWidth > (viewWidth - (2 * widthMargin))){ //the word itself does not fit, render it anyway
					currentRowWords.add(((TextElement)curElement));
					totalRowHeight = renderTextRow(canvas, charList, currentRowWords, totalRowHeight, true, curWordSpace, rowCurWidth, wordCurWidth); //we should print words justified
					rowCurWidth = 0;
				}else{
					if(rowCurWidth <= (viewWidth - (2 * widthMargin))){ //there is room for more words
						currentRowWords.add(((TextElement)curElement));
					}else{ //there is not room for more words
						totalRowHeight = renderTextRow(canvas, charList, currentRowWords, totalRowHeight, true, curWordSpace, rowCurWidth, wordCurWidth); //we should print words justified
						rowCurWidth = 0;
						i--; //the last word we checked did not fit, so dont forget that
					}
				}
				
				
			}else if(curElement instanceof ImageElement){
				
				
				boolean textBefore = false;
				if(!currentRowWords.isEmpty()){		
					totalRowHeight = renderTextRow(canvas, charList, currentRowWords, totalRowHeight, false, curWordSpace, rowCurWidth, wordCurWidth);
					rowCurWidth = 0;
					textBefore = true;
				}
				objectsIterated++;
				String imagePath = new File(cacheDir, bookName+"/"+((ImageElement)curElement).getUrl()).getAbsolutePath(); 
				Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
				((ImageElement)curElement).setBitmap(imageBitmap);
				((ImageElement)curElement).setAbsoluteUrl(imagePath);
				

				//if bitmap is small enough to just draw on canvas
				if(imageBitmap.getHeight() <= imageHeightLimit && imageBitmap.getWidth() <= viewWidth-(2 * widthMargin)){
					Matrix matrix = new Matrix();
					matrix.setTranslate((float) ((viewWidth-imageBitmap.getWidth())*0.5), totalRowHeight+(textBefore?baseTextSize:0));
					canvas.drawBitmap(imageBitmap,matrix, null);
					totalRowHeight += imageBitmap.getHeight() + rowMargin+(textBefore?baseTextSize:0);
				}else{
					if(viewHeight - totalRowHeight - heightMargin >= bookObjectHeight+rowMargin+baseTextSize){				
						specialObjectsMap.put(totalRowHeight+rowMargin+(textBefore?baseTextSize:0), printObjects.get(i)); //just ad
						totalRowHeight += bookObjectHeight;
					}else{ //no room for image, render on next page
						objectsIterated--;
						break;
					}

				}

			}
			
			if(totalRowHeight + ((baseTextSize + rowMargin)) + heightMargin > viewHeight){
				Log.d(tag, "reached end of page");
				break;
			}
		}
		
		// We've rendered this many objects when we're done with this page
		int objCount = objsByPage.get(pageNumber)+ objectsIterated; 
		
		if (pageNumber+1 >= objsByPage.size()){ // if this is the first time we render this page
			objsByPage.add(objCount);
		} else { // we've rendered this page before, so we update 
			// TODO not sure this is needed - might be useful for size changes later..
			objsByPage.set(pageNumber+1, objCount);
		}
		
		Log.d(tag, "Determining page end: object count/printObjSize (max objects): " + objCount + "/" +  printObjects.size());
		if (objCount >= printObjects.size()){ 
			Log.d(tag, "Setting end of sourcePage for current source to " + pageNumber);
			tracker.setEosPageNum(sourceIdent, pageNumber);
		}
		
		
		Log.d("HtmlRenderer",
				"Render time: "
						+ String.valueOf(System.currentTimeMillis() - milis)
						+ "ms");

		if (onDrawCompleteListener != null){
			onDrawCompleteListener.drawComplete();
		}

		Log.d(tag, "-------------------PAGE FINNISHED-----------------");
		
		return new RenderedPage(bitmap, drawFrom, charList, specialObjectsMap);
	}

	public int getBookObjectHeight(){	
		return bookObjectHeight;
	}

	@Override
	public int hashCode(){
		// This is written to be consistent with the overriden equals, see its documentation.
		return sourceIdent;
	}
	
	@Override
	public boolean equals(Object other){
		// TODO figure out whether we should reject non-HtmlRenderer objects - this could, concieveably, collide. 
		
		// This implementation uses the fact that clients supply a supposedly uniquely identifying ID
		// This *can* return true if the renderer is backed by the same file (under the current implementation).
		// all should be well. 
		
		return hashCode() == other.hashCode();
	}

	public void onConfigChanged(int baseTextSize, int bookObjectHeight,
			int xMargin, int yMargin, int rowMargin, int textColor,
			Bitmap backgroundBitmap) {
		Log.d(tag, "Renderer " + sourceIdent + "recieved onConfigChange call, changing like a boss!");
		setBaseTextSize(baseTextSize);
		setWidthMargin(xMargin);
		setHeightMargin(yMargin);
		setRowMargin(rowMargin);
		this.bookObjectHeight = bookObjectHeight; // TODO replace with appropriate setter
		// TODO add support for background bitmap
		paint.setColor(textColor); // TODO replace with appropriate setter method
	}
	
	public void onRotation(){
		int newHeight = viewWidth;
		viewWidth = viewHeight;
		viewHeight = newHeight;
	}

	

	
}
