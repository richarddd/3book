package se.chalmers.threebook.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.LinkedList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import se.chalmers.threebook.core.Helper;
import se.chalmers.threebook.html.BreakElement;
import se.chalmers.threebook.html.PrintElement;
import se.chalmers.threebook.html.RenderElement;
import se.chalmers.threebook.html.StyleFlag;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class BookView extends View {

	private static final String TAG = "BookView";

	private int screenWidth;
	private int screenHeight;
	private String htmlSource = "";
	private int baseTextSize = 26;
	private int minWordSpace = 2;
	private int rowMargin = (int) (baseTextSize*0.2);
	private int heightMargin = 10;
	private int widthMargin = 10;

	private float h1TextSize = (float) (baseTextSize * 2);
	private float h2TextSize = (float) (baseTextSize * 1.5);
	private float h3TextSize = (float) (baseTextSize * 1.2);
	private float h4TextSize = (float) (baseTextSize * 1);
	private float h5TextSize = (float) (baseTextSize * 0.85);
	private float h6TextSize = (float) (baseTextSize * 0.7);

	// private float[] headingScale = { 2, 2, 2, 2, 2, 2 };

	LinkedList<RenderElement> printObjects;
	private int wordsPrinted = 0;
	private int drawFrom = 0;
	private Canvas canvas;
	private Paint paint;

	public BookView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initView(context);
	}

	public BookView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initView(context);
	}

	public BookView(Context context) {
		super(context);
		initView(context);
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

	private void initView(Context context) {
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

		try {
			this.htmlSource = streamToString(context.getResources().getAssets()
					.open("html.html"));
			praseHtml();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String stripWhiteSpace(String input) {
		input = input.replaceAll("(\\s{2,})|(\n)|(\t)|(\r)", "");
		return input;
	}

	public String stripHead(String input) {
		return input.substring(input.indexOf("<body>") + 6);
	}

	public String streamToString(InputStream is) throws IOException {
		if (is != null) {
			Writer writer = new StringWriter();

			char[] buffer = new char[1024];
			try {
				Reader reader = new BufferedReader(new InputStreamReader(is,
						"UTF-8"));
				int n;
				while ((n = reader.read(buffer)) != -1) {
					writer.write(buffer, 0, n);
				}
			} finally {
				is.close();
			}
			return writer.toString();
		} else {
			return "";
		}
	}

	private void praseHtml() {

		Document doc = Jsoup.parse(htmlSource);

		/*
		 * for (Node n : doc.body().children()) replaceTextNodes(n);
		 */

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
						printObjects.add(new BreakElement(baseTextSize));
					}
				}

				while (node.nextSibling() == null && node != rootNode) {
					node = node.parent();
				}
				node = node.nextSibling();
			}

		}
	}

	private String replaceCodes(String word) {
		return word.replaceAll("&#8211", "-").replaceAll("&quot;", "\"")
				.replaceAll("&nbsp;", " ");
	}

	public void nextPage() {
		drawFrom += wordsPrinted;
		onDraw(canvas);
	}

	public void prevPage() {
		drawFrom -= wordsPrinted;
		onDraw(canvas);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (this.canvas == null) {
			this.canvas = canvas;
		}

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
					if(curWordSpace < minWordSpace){
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

						float glue = (float) (newParagraph ? tGlue : curWordSpace);

						Log.d(TAG, e.getText());
						
						rowHeight = (int) (paint.getTextSize() + rowMargin);
						canvas.drawText(e.getText(), widthMargin + lastWidth,
								totalRowHeight + rowHeight, paint);
						lastWidth += paint.measureText(e.getText()) + glue;
						wordsPrinted++;
					}

					totalRowHeight += rowHeight + breakSize;

					breakSize = 0;

					if (newParagraph) {
						i--;
					}
					rowCurWidth = 0;
					rowWordCount = 0;
					firstWordInRow = true;
					shouldPrintRow = false;

					if (totalRowHeight + ((baseTextSize + rowMargin) * 3) //XXX 3an här är lite magisk faktiskt, kan va ute och cykla med den
							+ heightMargin > screenHeight) {
						break;
					}

				}
			}
		}

		Log.d("BookView",
				"Drawtime: "
						+ String.valueOf(System.currentTimeMillis() - milis)
						+ "ms");

		super.onDraw(canvas);
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