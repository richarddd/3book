package se.chalmers.threebook.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;


public class HtmlRenderer {

	private static final int RENDER_LIST_INITIAL_CAPACITY = 2048;
	private int viewWidth;
	private int viewHeight;
	private String htmlSource = "";
	private int baseTextSize = 26;
	private int minWordSpace = 2;
	private int rowMargin = (int) (baseTextSize * 0.2);
	private int heightMargin = 10;
	private int widthMargin = 10;

	

	private float h1TextSize = (float) (baseTextSize * 2);
	private float h2TextSize = (float) (baseTextSize * 1.5);
	private float h3TextSize = (float) (baseTextSize * 1.2);
	private float h4TextSize = (float) (baseTextSize * 1);
	private float h5TextSize = (float) (baseTextSize * 0.85);
	private float h6TextSize = (float) (baseTextSize * 0.7);

	
	//private LinkedList<RenderElement> printObjects; // TODO evaluate whether a LL is the best choice
	private ArrayList<RenderElement> printObjects; // AL has amortized O(n) insertion and O(1) retrieval
	
	

	private Paint paint;

	

	private OnDrawCompleteListener onDrawCompleteListener;
	private String tag = "HtmlRenderer";
	
	// tracker stuff!
	private List<Integer> objsByPage; // provided and kept by the tracker
	private int objectsIterated = 0;
	
	// Experimental tracker stuff!
	private PageTracker tracker = new PageTracker(); // tracks source word counts 
	private int sourceIdent; // integer that uniquely identifies the current source
	
	// TODO figure out whether this should be moved to the tracker
	// probably not as long as we re-parse on each source shift 
	private Map<String, Integer> idMap = new HashMap<String, Integer>(); // anchor, objectCount of anchor 

	public static class OnDrawCompleteListener {
		public void drawComplete() {

		}
	}

	public HtmlRenderer(int viewWidth, int viewHeight) {
		init(viewWidth, viewHeight);
	}
	
	
	public void setOnDrawCompleteListener(OnDrawCompleteListener l) {
		this.onDrawCompleteListener = l;
	}

	public String getHtmlSource() {
		return htmlSource;
	}

	
	public void setHtmlSource(String htmlSource, int sourceIdentifier) {
		this.htmlSource = htmlSource;
		this.sourceIdent = sourceIdentifier;
		this.objectsIterated = 0;
		objsByPage = tracker.getPageStartList(sourceIdent);
		parseHtml();
	}

	public int getBaseTextSize() {
		return baseTextSize;
	}

	public void setBaseTextSize(int baseTextSize) {
		if (this.baseTextSize != baseTextSize) invalidate();
		this.baseTextSize = baseTextSize;
	}

	public int getMinWordSpace() {
		return minWordSpace;
	}

	public void setMinWordSpace(int minWordSpace) {
		if (this.minWordSpace != minWordSpace) invalidate();
		this.minWordSpace = minWordSpace;
	}

	public int getRowMargin() {
		return rowMargin;
	}

	public void setRowMargin(int rowMargin) {
		if (this.rowMargin != rowMargin) invalidate();
		this.rowMargin = rowMargin;
	}

	public int getHeightMargin() {
		return heightMargin;
	}

	public void setHeightMargin(int heightMargin) {
		if (this.heightMargin != heightMargin) invalidate();
		this.heightMargin = heightMargin;
	}

	public int getWidthMargin() {
		return widthMargin;
	}

	public void setWidthMargin(int widthMargin) {
		if (this.widthMargin != widthMargin) invalidate();
		this.widthMargin = widthMargin;
	}
	
	private void invalidate(){
		tracker.invalidate();
		objsByPage = tracker.getPageStartList(sourceIdent); // get new object to work with
	}

	private void init(int viewWidth, int viewHeight) {
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;

		paint = new Paint();
		paint.setColor(Color.BLACK);
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		paint.setTextAlign(Paint.Align.LEFT);
		paint.setTextSize(baseTextSize);
	}

	private void parseHtml() {

		long t1 = System.currentTimeMillis();
		Document doc = Jsoup.parse(htmlSource);

		printObjects = new ArrayList<RenderElement>(RENDER_LIST_INITIAL_CAPACITY);

		Node rootNode = doc.body();
		Node node = rootNode;
		while (node != null) {

			if (node instanceof Element) {
				if (((Element) node).tagName().equals("p")) {
					printObjects.add(new BreakElement(baseTextSize));
				}
				
				// Detect elements with IDs and squirrel them away
				String id = ((Element) node).id();
				if (id != null && id != ""){
					idMap.put(id, printObjects.size()+1); // XXX +1 could mean crazy EOF bugs if nothing is rendered on last-anchor ?
				}
			}

			if (node.childNodes().size() != 0) {
				node = node.childNodes().get(0);
			} else { // leaf
				if (node instanceof TextNode) {
					TextNode tNode = (TextNode) node;
					if (!tNode.text().equals("")) {

						String parentTag = ((Element) tNode.parent()).tagName();
						StyleFlag flag = StyleFlag.NORMAL;

						for (StyleFlag f : StyleFlag.values()) {
							if (f.name().equals(parentTag.toUpperCase())) {
								flag = StyleFlag.valueOf(parentTag
										.toUpperCase());
							}
						}
						String[] words = tNode.text().split(" ");
						for (String word : words) {
							printObjects.add(new TextElement(word, flag));
						}
						switch (flag) {
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
				} else if (node instanceof Element) {
					Element eNode = (Element) node;
					if (eNode.tagName().equals("br")) {
						printObjects.add(new BreakElement(0));
					}
					
				}

				while (node.nextSibling() == null && node != rootNode) {
					node = node.parent();
				}
				node = node.nextSibling();
			}

		}
		long t2 = System.currentTimeMillis();
		Log.d(tag, "Parsing HTML took " + (t2-t1) + "ms");
	}

	private void setStyle(StyleFlag flag) {
		switch (flag) {
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
		
		int anchorWord = idMap.get(id);
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
			return objsByPage.size()-2; // XXX
		} 
		 
		
	}
	
	public RenderedPage getRenderedPage(int pageNumber) {
		if (pageNumber < 0) {
			throw new IllegalArgumentException("pageNumber must be > 0. Number was: " + pageNumber);
		}
		
		long milis = System.currentTimeMillis();

		int drawFrom = objsByPage.get(pageNumber);

		List<CharPosition> charList = new ArrayList<CharPosition>();

		Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight,
				Bitmap.Config.ARGB_4444);

		Canvas canvas = new Canvas(bitmap);

		// TODO document the purpose of these variables
		objectsIterated = 0;
		float rowCurWidth = 0;
		int rowWordCount = 0;
		int totalRowHeight = 0;
		boolean firstWordInRow = true;
		boolean shouldPrintRow = false;
		boolean newParagraph = true;
		int breakSize = 0;
		int curWordSpace = 0;

		if (printObjects == null){
			throw new NullPointerException("HtmlRenderer: printObjects was null when asked to render, most likely HTML is not parsed yet!");
		}
		
		for (int i = drawFrom, size = printObjects.size(); i < size; i++) {

			float wordCurWith = 0;
			if (printObjects.get(i) instanceof BreakElement) {
				shouldPrintRow = true;
				newParagraph = false;
				rowWordCount++;
				objectsIterated++; // this is also an object in the array
				breakSize = ((BreakElement) printObjects.get(i)).getSpan()+rowMargin;
			} else { // TODO make this less fragile by not presupposing ONLY break or text elements 
				TextElement e = (TextElement) printObjects.get(i);
				setStyle(e.getStyle());
				curWordSpace = (int) (paint.getTextSize() * 0.2);
				if (curWordSpace < minWordSpace) {
					curWordSpace = minWordSpace;
				}
				wordCurWith = paint.measureText(e.getText());
				rowCurWidth += wordCurWith;
				if (!firstWordInRow) {
					rowCurWidth += curWordSpace;
				}
				firstWordInRow = false;
				if (rowCurWidth <= (viewWidth - (2 * widthMargin))) {
					rowWordCount++;
				} else {
					newParagraph = true;
					shouldPrintRow = true;
				}
			}
			
			if (shouldPrintRow) {
				float tGlue = ((viewWidth - (2 * widthMargin)) - (rowCurWidth
						- wordCurWith - (curWordSpace * rowWordCount)))
						/ (rowWordCount - 1);
				rowWordCount--;
				float lastWidth = 0;
				int from = i - rowWordCount - (newParagraph ? 1 : 0);
				int to = i;

				if (from < 0) {
					from = 0;
				}

				int rowHeight = 0;
				for (int j = from; j < to; j++) {
					TextElement e = (TextElement) printObjects.get(j);
					setStyle(e.getStyle());
					float glue = (float) (newParagraph ? tGlue
							: curWordSpace);
					rowHeight = (int) (paint.getTextSize() + rowMargin);
					float xPos = widthMargin + lastWidth;
					float yPos = totalRowHeight + rowHeight;
					canvas.drawText(e.getText(), xPos, yPos, paint);
					float wordWidth = paint.measureText(e.getText());
					lastWidth += wordWidth + glue;
					char[] charArray = e.getText().toCharArray();
					int wordLength = charArray.length;
					float charWidth = wordWidth / wordLength;
					for (char c : charArray) {
						charList.add(new CharPosition(xPos, yPos
								- paint.getTextSize(), xPos + charWidth,
								yPos, c));
					}
					objectsIterated++;
				}

				totalRowHeight += rowHeight + breakSize;

				breakSize = 0;

				if (newParagraph) {
					// we rewind the word counter if we couldn't fit the
					// word on the just-finished line, so the word gets a
					// fit-attempt on the next line.
					i--;
				}
				rowCurWidth = 0;
				rowWordCount = 0;
				firstWordInRow = true;
				shouldPrintRow = false;

				if (totalRowHeight + ((baseTextSize + rowMargin))
						+ heightMargin > viewHeight) {
					break;
				}

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
		
		objsByPage.set(pageNumber + 1, objCount);
		
		if (printObjects.size() == objCount){ // 
			tracker.setEosPageNum(sourceIdent, pageNumber);
		}
		
		
		Log.d("HtmlRenderer",
				"Render time: "
						+ String.valueOf(System.currentTimeMillis() - milis)
						+ "ms");

		if (onDrawCompleteListener != null){
			onDrawCompleteListener.drawComplete();
		}
		return new RenderedPage(bitmap, drawFrom, charList);
	}
}
