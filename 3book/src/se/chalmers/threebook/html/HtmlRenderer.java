package se.chalmers.threebook.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

/**
 * 
 * TODO: Fix so that the renderer doesn't populate the pagePos list if the page already has been rendered.
 *
 */

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

	private int offset = 0;

	private float h1TextSize = (float) (baseTextSize * 2);
	private float h2TextSize = (float) (baseTextSize * 1.5);
	private float h3TextSize = (float) (baseTextSize * 1.2);
	private float h4TextSize = (float) (baseTextSize * 1);
	private float h5TextSize = (float) (baseTextSize * 0.85);
	private float h6TextSize = (float) (baseTextSize * 0.7);

	
	//private LinkedList<RenderElement> printObjects; // TODO evaluate whether a LL is the best choice
	private ArrayList<RenderElement> printObjects; // AL has amortized O(n) insertion and O(1) retrieval
	
	private int objectsIterated = 0;

	private Paint paint;

	private List<Integer> pagePosList = new ArrayList<Integer>(20);
	private int drawFrom = 0; // the value of the
	private int curPosIndex = 0; // index to the pagePosList that was last drawn

	private OnDrawCompleteListener onDrawCompleteListener;
	private String tag = "HtmlRenderer";
	
	private boolean endOfSource = false;
	private boolean startOfSource = true;
	
	private Map<String, Integer> idMap = new HashMap<String, Integer>(); // id, objectCount of ID 
	private int myRender = -1; // XXX remove after testing this is nothing
	private int wordCount;

	public static class OnDrawCompleteListener {
		public void drawComplete() {

		}
	}

	public HtmlRenderer(int viewWidth, int viewHeight) {
		init(viewWidth, viewHeight);
	}
	
	/**
	 * Returns whether the last render was the end of source 
	 * @return whether the last render was the end of source
	 */
	public boolean atEndOfSource(){
		return endOfSource;
	}
	public boolean atStartOfSource(){
		return startOfSource;
	}
	
	public void setOnDrawCompleteListener(OnDrawCompleteListener l) {
		this.onDrawCompleteListener = l;
	}

	public String getHtmlSource() {
		return htmlSource;
	}

	public void setHtmlSource(String htmlSource) {
		this.htmlSource = htmlSource;
		wordCount = 0;
		objectsIterated = 0;
		endOfSource = false;
		startOfSource = true;
		parseHtml();
	}

	public int getBaseTextSize() {
		return baseTextSize;
	}

	public void setBaseTextSize(int baseTextSize) {
		this.baseTextSize = baseTextSize;
	}

	public int getMinWordSpace() {
		return minWordSpace;
	}

	public void setMinWordSpace(int minWordSpace) {
		this.minWordSpace = minWordSpace;
	}

	public int getRowMargin() {
		return rowMargin;
	}

	public void setRowMargin(int rowMargin) {
		this.rowMargin = rowMargin;
	}

	public int getHeightMargin() {
		return heightMargin;
	}

	public void setHeightMargin(int heightMargin) {
		this.heightMargin = heightMargin;
	}

	public int getWidthMargin() {
		return widthMargin;
	}

	public void setWidthMargin(int widthMargin) {
		this.widthMargin = widthMargin;
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

		pagePosList.add(0); // draw first page from word zero
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
				/* ACTUALLY - why don't we just store all IDs? shouldn't be too expensive. 
				// XXX performance might be bad for large ID-sets
				// Check if this element has an ID. if it has an ID, see if 
				// the ID is one we're interested in. If so, inject a
				String id = ((Element) node).id();
				if (id != null && id != ""){
					Log.d(tag, "We found a non-null id! it is: " + id);
					if (idList.remove(id)){
						Log.d(tag, "The id was in the list and is now removed. We're storing it and referencing the printobjsize " + printObjects.size()+1 + ".");
						idMap.put(id, printObjects.size()+1); // XXX +1 could mean crazy EOF bugs if nothing is rendered on last-anchor ?
					}
				} */
				String id = ((Element) node).id();
				if (id != null && id != ""){
					Log.d(tag, "Found ID tag! Adding it!" + id + ", printobjsize " + printObjects.size()+1 + ".");
					idMap.put(id, printObjects.size()+1); // XXX +1 could mean crazy EOF bugs if nothing is rendered on last-anchor ?
					//idMap.put(id, wordCount+1); // XXX +1 could mean crazy EOF bugs if nothing is rendered on last-anchor ?
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
							wordCount++; // XXX experimental 
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

	public boolean lastPage() {
		return objectsIterated == printObjects.size();
	}

	public boolean firstPage() {
		return drawFrom == 0;
	}
	

	public RenderedPage getRenderedPage(String id){
		
		int anchorWord = idMap.get(id);
		int pageNumber = -1;
		
		Log.d(tag, "anchorWord is: " + anchorWord);
		
		for (Entry<String, Integer> me : idMap.entrySet())
			Log.d(tag, "id/val: " + me.getKey() + "/" + me.getValue());
		
		for (int i = 0, size = pagePosList.size(); i < size; i++){
			// The anchor is on the page before the page which begins with a 
			// larger word count than what had been parsed when the anchor was found.  
			if (pagePosList.get(i) >= anchorWord){
			//if (anchorWord >= pagePosList.get(i)){ // hey the word should be higher right.
				pageNumber = pagePosList.get(i) == anchorWord ? i : (i-1);
				break;
			}
		}
		
		if (pageNumber > 0){ // page already rendered
			Log.d(tag, "PageIndex found! returning a page. index: " + pageNumber);
			myRender = 1; // XXX remove after testing
			return getRenderedPage(pageNumber);
		} else { // page is not rendered yet
			Log.d(tag, "The page we're trying to reach is not rendered yet, alas...");
		}
		 
		return getRenderedPage(0);
	}
	
	public RenderedPage getRenderedPage(int pageNumber) {
		Log.d(tag, "Rendering page number: " + pageNumber);
		if (pageNumber < 0) {
			throw new IllegalArgumentException("pageNumber must be > 0");
		}
		
		long milis = System.currentTimeMillis();

		int drawFrom = pagePosList.get(pageNumber);
		

		List<CharPosition> charList = new ArrayList<CharPosition>();

		Bitmap bitmap = Bitmap.createBitmap(viewWidth, viewHeight,
				Bitmap.Config.ARGB_4444);

		Canvas canvas = new Canvas(bitmap);

		objectsIterated = 0;
		float rowCurWidth = 0;
		int rowWordCount = 0;
		int totalRowHeight = 0;
		boolean firstWordInRow = true;
		boolean shouldPrintRow = false;
		boolean newParagraph = true;
		int breakSize = 0;
		int curWordSpace = 0;

		pagePosList.add(drawFrom); // XXX this should only be done if its the first time we render this page.

		if (printObjects != null) {

			int size = printObjects.size(); // XXX this is a very expensive operation if done w/ linkedlist

			for (int i = drawFrom; i < size; i++) {

				float wordCurWith = 0;
				if (printObjects.get(i) instanceof BreakElement) {
					shouldPrintRow = true;
					newParagraph = false;
					rowWordCount++;
					objectsIterated++; // this is also an object in the array
					breakSize = ((BreakElement) printObjects.get(i)).getSpan()+rowMargin;
				} else {
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
					for (int ii = from; ii < to; ii++) {
						TextElement e = (TextElement) printObjects.get(ii);
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
						if (myRender > 0 && myRender < 15){ // XXX DEBUG REMOVE
							Log.d(tag, "word : " + e.getText());
							myRender++;
						}
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

					// XXX 3an här är lite magisk, borde fixas
					if (totalRowHeight + ((baseTextSize + rowMargin) * 1)
							+ heightMargin > viewHeight) {
						break;
					}

				}
			}
		}
		
		int wordPosition = pagePosList.get(pageNumber)+ objectsIterated;
		pagePosList.set(pageNumber + 1, wordPosition);
		endOfSource = (printObjects.size() == wordPosition); // check if we're at end of source
		startOfSource = (pageNumber == 0);
		
		Log.d("HtmlRenderer",
				"Render time: "
						+ String.valueOf(System.currentTimeMillis() - milis)
						+ "ms");

		if (onDrawCompleteListener != null)
			onDrawCompleteListener.drawComplete();

		
		
			 
		
		myRender = -1;
		return new RenderedPage(bitmap, drawFrom, charList);
	}
}
