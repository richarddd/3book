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

	private int index;

	public static final String BASIC_STYLE = "<style>*{margin:0;padding:0;}body{text-align:justify;padding:0px 10px;line-height:18px;font-size:16px;}h1{line-height:36px;font-size:32px;margin-bottom:36px;}h2{line-height:36px;font-size:32px;margin-bottom:36px;}h3{line-height:36px;font-size:32px;margin-bottom:36px;}hr{line-height:18px}p{margin-bottom:18px;}</style>";

	public HtmlParser(String html) {
		original = html;
		mod = new StringBuilder(html);
		index = 0;
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

			Pattern wrapperPattern = Pattern.compile("(<h[1-7])(.*?)(</h[1-7]>)",
					Pattern.DOTALL);
			Matcher html = wrapperPattern.matcher(mod);

			Map<Integer, String> titleList = new HashMap<Integer, String>();

			Pattern tagPattern = Pattern.compile("<.*?>", Pattern.DOTALL);

			while (html.find()) {
				String match = html.group();
				Matcher heading = tagPattern.matcher(match);
				String title = heading.replaceAll("");
				titleList.put(html.start(), title);
			}

			List<Integer> indexArray = new ArrayList<Integer>(titleList.keySet());
			Collections.sort(indexArray);
			Collections.reverse(indexArray);

			for (Integer i : indexArray) {
				String anchorName = String.valueOf(titleList.get(i).hashCode()) + "_" + this.index++;
				String tag = "<a name=\"" + anchorName + "\"/>";
				mod.insert(i, tag);

				headings.put(titleList.get(i), anchorName);
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
			Matcher srcs = srcPattern.matcher(mod);

			List<String> imgList = new ArrayList<String>();

			Map<Integer, Integer> indexList = new HashMap<Integer, Integer>();
			Map<Integer, String> uriList = new HashMap<Integer, String>();

			while (srcs.find()) {
				String match = srcs.group();
				int start = match.indexOf("src=");
				int end = match.indexOf(match.charAt(start + 4), start + 5);
				uriList.put(srcs.start(), match.subSequence(start + 5, end).toString());

				indexList.put(srcs.start(), srcs.end());
			}

			String startDiv = "<div class=\"threebookImageContainer\"><a class=\"threebookImageLink\" href=\"#\", onClick=\"application.fireImageIntent('";
			String endDiv = "</a></div>";
			String height = "height=\"252px\"";

			List<Integer> indexArray = new ArrayList<Integer>(indexList.keySet());
			Collections.sort(indexArray);
			Collections.reverse(indexArray);

			for (Integer i : indexArray) {
				mod.insert(indexList.get(i), endDiv);

				mod.insert(i + 5, height);

				String modifiedStartDiv = startDiv + uriList.get(i) + "');\" >"; 
				mod.insert(i, modifiedStartDiv);
			}

			imagePaths =  new ArrayList<String>(uriList.values());
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