package se.chalmers.threebook.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParser {

	private String original;
	private StringBuilder mod;
	Map<String, String> headings;
	List<String> imagePaths;

	public static final String basicStyle = "<style>*{margin:0;padding:0;}body{text-align:justify;padding:0px 10px;line-height:18px;font-size:16px;}h1{line-height:36px;font-size:32px;margin-bottom:36px;}p{margin-bottom:18px;}</style>";

	public HtmlParser(String html) {
		original = html;
		mod = new StringBuilder(html);
	}

	public String getOriginalHtml() {
		return original;
	}

	public String getModifiedHtml() {
		return mod.toString();
	}

	public Map<String, String> getHeadings() {

		if (headings == null) {

			Map<String, String> headings = new HashMap<String, String>();

			Pattern wrapperPattern = Pattern.compile("(<h2)(.*?)(</h2>)",
					Pattern.DOTALL);
			Matcher html = wrapperPattern.matcher(original);

			Map<Integer, String> indexList = new HashMap<Integer, String>();

			while (html.find()) {
				String match = html.group();

				int start = match.indexOf(">")+1;
				int end = match.indexOf("<", start);
				String title = (match.subSequence(start, end)).toString();

				int insert = html.start();

				indexList.put(insert, title);
			}

			List<Integer> indexArray = new ArrayList<Integer>(indexList.keySet());
			Collections.sort(indexArray);
			Collections.reverse(indexArray);

			for (Integer i : indexArray) {
				String anchorName = String.valueOf(indexList.get(i).hashCode());
				String tag = "<a name=\"" + anchorName + "\"/>";
				mod.insert(i, tag);

				headings.put(indexList.get(i), anchorName);
			}

			this.headings = headings;
		}

		return this.headings;
	}

	public List<String> getImg() {

		if (imagePaths == null) {

			Pattern srcPattern = Pattern.compile(
					"<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
					Pattern.DOTALL);
			Matcher srcs = srcPattern.matcher(original);

			List<String> imgList = new ArrayList<String>();

			Map<Integer, Integer> indexList = new HashMap<Integer, Integer>();

			while (srcs.find()) {
				String match = srcs.group();
				int start = match.indexOf("src=");
				int end = match.indexOf(match.charAt(start + 4), start + 5);
				imgList.add((match.subSequence(start + 5, end)).toString());

				indexList.put(srcs.start(), srcs.end());
			}

			String startDiv = "<div class=\"threebookImageContainer\">";
			String endDiv = "</div>";
			String height = "height=\"252px\"";

			List<Integer> indexArray = new ArrayList<Integer>(indexList.keySet());
			Collections.sort(indexArray);
			Collections.reverse(indexArray);

			for (Integer i : indexArray) {
				mod.insert(indexList.get(i), endDiv);

				mod.insert(i + 5, height);

				mod.insert(i, startDiv);
			}

			imagePaths = imgList;
		}

		return imagePaths;
	}

	public void injectCss(String style) {
		removePattern("(<style)(.*?)(</style>)"); // in-file style tags
		removePattern("(<link)(.*?)(rel=\"stylesheet\")(.*?)(/>)"); // linked
																	// css

		// Inject css
		Pattern pattern = Pattern.compile("(<head)(.*?)(</head>)",
				Pattern.DOTALL);
		Matcher html = pattern.matcher(mod);

		if (html.find()) {
			mod.insert(html.end() - 7, style);
		} else {
			// No header tag
			Pattern bodyPattern = Pattern.compile("<body");
			Matcher bodyMatcher = bodyPattern.matcher(mod);

			if (bodyMatcher.find()) {
				mod.insert(bodyMatcher.start(), "<head>" + style + "</head>");
			}
		}
	}

	private void removePattern(String p) {
		Pattern pattern = Pattern.compile(p, Pattern.DOTALL);
		Matcher html = pattern.matcher(mod);

		Map<Integer, Integer> indexList = new HashMap<Integer, Integer>();

		while (html.find()) {
			indexList.put(html.start(), html.end());
		}

		List<Integer> indexArray = new ArrayList<Integer>(indexList.keySet());
		Collections.sort(indexArray);
		Collections.reverse(indexArray);

		for (Integer i : indexArray) {
			mod.delete(i, indexList.get(i));
		}
	}
}