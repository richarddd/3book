package se.chalmers.threebook.html;

import java.util.LinkedList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import se.chalmers.threebook.util.Helper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

public class HtmlRenderer {
	private static HtmlRenderer instance;
	private static Object syncObject; // for thread safe singleton

	private int screenWidth;
	private int screenHeight;
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

	private LinkedList<RenderElement> printObjects;
	private int wordsPrinted = 0;
	private int drawFrom = 0;
	private Paint paint;

	private OnDrawCompleteListener onDrawCompleteListener;

	private HtmlRenderer() {

	}

	public static HtmlRenderer getInstance() {
		if (instance == null) {
			synchronized (syncObject) {
				if (instance == null) {
					instance = new HtmlRenderer();
				}
			}
		}
		return instance;
	}



	public static class OnDrawCompleteListener {

		public void drawComplete() {

		}
	}

	public void setOnDrawCompleteListener(OnDrawCompleteListener l) {
		this.onDrawCompleteListener = l;
	}

	public String getHtmlSource() {
		return htmlSource;
	}

	public void setHtmlSource(String htmlSource) {
		this.htmlSource = htmlSource;
		wordsPrinted = 0;
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

	private void init(Context context) {
		Display display = ((WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

		if (Helper.SupportsNewApi()) {
			Point size = new Point();
			display.getSize(size);
			screenWidth = size.x;
			screenHeight = size.y;
		} else {
			screenWidth = display.getWidth();
			screenHeight = display.getHeight();
		}

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
							printObjects.add(new PrintElement(word, flag));
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
		drawFrom += wordsPrinted;
	}

	public void prevPage() {
		drawFrom -= wordsPrinted;
	}

	public Bitmap getRenderedBitmap() {

		Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_4444);

		Canvas canvas = new Canvas(bitmap);

		long milis = System.currentTimeMillis();

		wordsPrinted = 0;
		float rowCurWidth = 0;
		int rowWordCount = 0;
		int totalRowHeight = 0;
		boolean firstWordInRow = true;
		boolean shouldPrintRow = false;
		boolean newParagraph = true;
		int breakSize = 0;

		int curWordSpace = 0;

		if (printObjects != null) {

			int size = printObjects.size();

			for (int i = drawFrom; i < size; i++) {

				float wordCurWith = 0;
				if (printObjects.get(i) instanceof BreakElement) {
					shouldPrintRow = true;
					newParagraph = false;
					rowWordCount++;
					breakSize = ((BreakElement) printObjects.get(i)).getSpan();
				} else {
					PrintElement e = (PrintElement) printObjects.get(i);
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
					if (rowCurWidth <= (screenWidth - (2 * widthMargin))) {
						rowWordCount++;
					} else {
						newParagraph = true;
						shouldPrintRow = true;
					}
				}
				if (shouldPrintRow) {
					float tGlue = ((screenWidth - (2 * widthMargin)) - (rowCurWidth
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
						PrintElement e = (PrintElement) printObjects.get(ii);
						setStyle(e.getStyle());
						float glue = (float) (newParagraph ? tGlue
								: curWordSpace);
						rowHeight = (int) (paint.getTextSize() + rowMargin);
						canvas.drawText(e.getText(), widthMargin + lastWidth,
								totalRowHeight + rowHeight, paint);
						lastWidth += paint.measureText(e.getText()) + glue;
						wordsPrinted++;
					}

					totalRowHeight += rowHeight + breakSize;

					breakSize = 0;

					if (newParagraph) {
						i--; //hej!
					}
					rowCurWidth = 0;
					rowWordCount = 0;
					firstWordInRow = true;
					shouldPrintRow = false;

					//XXX 3an här är lite magisk, borde fixas
					if (totalRowHeight + ((baseTextSize + rowMargin) * 3) 
							+ heightMargin > screenHeight) {
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

		return bitmap;
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
}
