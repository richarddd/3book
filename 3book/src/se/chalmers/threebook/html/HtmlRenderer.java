package se.chalmers.threebook.html;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

	private LinkedList<RenderElement> printObjects;
	private int objectsIterated = 0;
	private int drawFrom = 0;
	private Paint paint;

	private List<Integer> pagePosList = new ArrayList<Integer>(20);

	private OnDrawCompleteListener onDrawCompleteListener;

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

	public void setHtmlSource(String htmlSource) {
		this.htmlSource = htmlSource;
		objectsIterated = 0;
		praseHtml();
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
	}

	private void praseHtml() {

		Document doc = Jsoup.parse(htmlSource);

		printObjects = new LinkedList<RenderElement>();

		Node rootNode = doc.body();
		Node node = rootNode;
		while (node != null) {

			if (node instanceof Element) {
				if (((Element) node).tagName().equals("p")) {
					printObjects.add(new BreakElement(baseTextSize));
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
	}

	public void nextPage() {

		offset++;

		// drawFrom += objectsIterated;
	}

	public void prevPage() {
		if (offset > 0) {
			offset--;
		}

		// drawFrom -= objectsIterated;
	}

	public RenderedPage getNextPage() {
		nextPage();
		return getRenderedPage();
	}

	public RenderedPage getPrevPage() {
		prevPage();
		return getRenderedPage();
	}

	public RenderedPage getRenderedPage() {

		long milis = System.currentTimeMillis();

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

		pagePosList.add(drawFrom);

		if (printObjects != null) {

			int size = printObjects.size();

			for (int i = drawFrom; i < size; i++) {

				float wordCurWith = 0;
				if (printObjects.get(i) instanceof BreakElement) {
					shouldPrintRow = true;
					newParagraph = false;
					rowWordCount++;
					objectsIterated++; // this is also an object in the array
					breakSize = ((BreakElement) printObjects.get(i)).getSpan();
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
						i--; // hej!
					}
					rowCurWidth = 0;
					rowWordCount = 0;
					firstWordInRow = true;
					shouldPrintRow = false;

					// XXX 3an här är lite magisk, borde fixas
					if (totalRowHeight + ((baseTextSize + rowMargin) * 3)
							+ heightMargin > viewHeight) {
						break;
					}

				}
			}
		}

		Log.d("HtmlRenderer",
				"Render time: "
						+ String.valueOf(System.currentTimeMillis() - milis)
						+ "ms");

		if (onDrawCompleteListener != null)
			onDrawCompleteListener.drawComplete();

		return new RenderedPage(bitmap, drawFrom, charList);
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
}
