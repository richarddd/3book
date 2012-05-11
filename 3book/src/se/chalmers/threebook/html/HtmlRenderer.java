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

public class HtmlRenderer{

	private static final int RENDER_LIST_INITIAL_CAPACITY = 2048;
	private static final int WORD_ROW_LIST_INITIAL_CAPACITY = 10;
	private static final int BOOK_OBJECT_HEIGHT_DP = 191;
	private static final int IMAGE_HEIGHT_LIMIT_DP = 400;

	
	private int imageHeightLimit;
	private int bookObjectHeight;
	private int viewWidth;
	private int viewHeight;
	private String htmlSource = "";
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
	


	// private LinkedList<RenderElement> printObjects; // TODO evaluate whether
	// a LL is the best choice
	private ArrayList<RenderElement> printObjects; // AL has amortized O(n)
													// insertion and O(1)
													// retrieval

	private int objectsIterated = 0;

	private Paint paint;

	private List<Integer> objsByPage = new ArrayList<Integer>(20);
	private int drawFrom = 0; // TODO see about removing this

	private OnDrawCompleteListener onDrawCompleteListener;
	private String tag = "HtmlRenderer";

	// TODO: implement these in a more reasonable manner. Likely they will not
	// be a part of
	// the HTML-renderer class but rather reside in the middle layer.
	private boolean endOfSource = false;
	private boolean startOfSource = true;

	private Map<String, Integer> idMap = new HashMap<String, Integer>(); // anchor,
																			// objectCount
																			// of
																			// anchor

	public static class OnDrawCompleteListener{
		public void drawComplete(){

		}
	}

	public HtmlRenderer(Context context, int viewWidth, int viewHeight){
		init(context, viewWidth, viewHeight);
	}

	/**
	 * Returns whether the last render was the end of source
	 * 
	 * @return whether the last render was the end of source
	 */
	public boolean atEndOfSource(){
		return endOfSource;
	}

	public boolean atStartOfSource(){
		return startOfSource;
	}

	public void setOnDrawCompleteListener(OnDrawCompleteListener l){
		this.onDrawCompleteListener = l;
	}

	public String getHtmlSource(){
		return htmlSource;
	}

	public void setHtmlSource(String htmlSource){
		this.htmlSource = htmlSource;
		objectsIterated = 0;
		endOfSource = false;
		startOfSource = true;
		parseHtml();
	}

	public int getBaseTextSize(){
		return baseTextSize;
	}

	public void setBaseTextSize(int baseTextSize){
		this.baseTextSize = baseTextSize;
	}

	public int getMinWordSpace(){
		return minWordSpace;
	}

	public void setMinWordSpace(int minWordSpace){
		this.minWordSpace = minWordSpace;
	}

	public int getRowMargin(){
		return rowMargin;
	}

	public void setRowMargin(int rowMargin){
		this.rowMargin = rowMargin;
	}

	public int getHeightMargin(){
		return heightMargin;
	}

	public void setHeightMargin(int heightMargin){
		this.heightMargin = heightMargin;
	}

	public int getWidthMargin(){
		return widthMargin;
	}

	public void setWidthMargin(int widthMargin){
		this.widthMargin = widthMargin;
	}

	private void init(Context context, int viewWidth, int viewHeight){
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.bookObjectHeight = (int) Helper.dpToPx(context, BOOK_OBJECT_HEIGHT_DP);
		this.cacheDir = context.getCacheDir();
		this.imageHeightLimit = (int) Helper.dpToPx(context, IMAGE_HEIGHT_LIMIT_DP);

		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setTextSize(baseTextSize);

		objsByPage.add(0); // draw first page from word zero
	}

	private void parseHtml(){

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

	public boolean lastPage(){
		return objectsIterated == printObjects.size();
	}

	public boolean firstPage(){
		return drawFrom == 0;
	}

	public int getPageNumber(String id){

		int anchorWord = idMap.get(id);
		int pageNumber = 0;

		Log.d(tag, "Going to anchor! AnchorWord for anchor " + id + " is: " + anchorWord);

		for(int i = 0, size = objsByPage.size(); i < size; i++){
			// The anchor is on the page before the page which begins with a
			// larger word count than what had been parsed when the anchor was
			// found.
			if(objsByPage.get(i) >= anchorWord){
				// If the page begins on exactly the desired word, we've found
				// the page
				// otherwise, we want the preceding page.
				pageNumber = objsByPage.get(i) == anchorWord?i:(i - 1);
				break; // the page number is found, we're done.
			}
		}

		if(pageNumber > 0){ // page already rendered
			getRenderedPage(pageNumber);
			return pageNumber; // XXX expensive - need to fix cache.
		}else{ // page is not rendered yet
			// Render one page at a time until the last rendered word
			// is greater than the word we're searching for.
			while(anchorWord > objsByPage.get(objsByPage.size() - 1)){
				getRenderedPage(objsByPage.size() - 1);
			}

			// TODO once caching is implemented this should return -1
			return objsByPage.size() - 2; // XXX
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
				String imagePath = new File(cacheDir, MyBook.get().book().getTitle()+"/"+((ImageElement)curElement).getUrl()).getAbsolutePath();
				Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
				((ImageElement)curElement).setBitmap(imageBitmap);
				((ImageElement)curElement).setAbsoluteUrl(imagePath);
				

				//if bitmap is small enough to just draw on canvas
				if(imageBitmap.getHeight() <= imageHeightLimit && imageBitmap.getWidth()*2 <= viewWidth-(2 * widthMargin)){
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
		int objCount = objsByPage.get(pageNumber) + objectsIterated;

		if(pageNumber + 1 >= objsByPage.size()){ // if this is the first time we
													// render this page
			objsByPage.add(objCount);
		}else{ // we've rendered this page before, so we update
			// TODO not sure this is needed - might be useful for size changes
			// later..
			objsByPage.set(pageNumber + 1, objCount);
		}

		objsByPage.set(pageNumber + 1, objCount);

		endOfSource = (printObjects.size() == objCount); // check if we're at
															// end of source
		startOfSource = (pageNumber == 0);

		Log.d("HtmlRenderer", "Render time: " + String.valueOf(System.currentTimeMillis() - milis)
				+ "ms");

		if(onDrawCompleteListener != null){
			onDrawCompleteListener.drawComplete();
		}
		
		Log.d(tag, "-------------------PAGE FINNISHED-----------------");
		
		return new RenderedPage(bitmap, drawFrom, charList, specialObjectsMap);
	}

	public int getBookObjectHeight(){	
		return bookObjectHeight;
	}

	
}
