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

			// with html: \\<h2.*\\>.*\\</h2>
			// without end tag: ((<h2)(.*?)(>))(.*?)(?=(</h2>))
			Pattern wrapperPattern = Pattern
					.compile("((<h2)(.*?)(>))(.*?)(</h2>)");
			Matcher html = wrapperPattern.matcher(original);

			Map<Integer, String> indexList = new HashMap<Integer, String>();

			while (html.find()) {
				String match = html.group();

				int start = match.indexOf(">");
				int end = match.indexOf("<", start + 1);
				String title = (match.subSequence(start + 1, end)).toString();

				int insert = html.start() - 1;

				indexList.put(insert, title);
			}

			ArrayList<Integer> indexes = new ArrayList<Integer>(
					indexList.keySet());
			Collections.sort(indexes);

			int offset = 0;
			for (Integer i : indexes) {
				String anchorName = String.valueOf(indexList.get(i).hashCode());
				String tag = "<a name=\"" + anchorName + "\"/>";
				mod.insert(i + offset, tag);
				offset += tag.length();

				headings.put(indexList.get(i), anchorName);
			}

			this.headings = headings;
		}

		return this.headings;
	}

	public List<String> getImg() {

		if (imagePaths == null) {

			Pattern srcPattern = Pattern
					.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>");
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

			String startDiv = "<div class=\"threebookImageContainer\">" + 
					"<a class=\"threeBookImageLink\" href=\"#\" onClick=\"application.fireImageIntent(\""+"images/chap22.jpg"+"\");\" >";
			String endDiv = "</a></div>";
			String height = "height=\"252px\"";
			int offset = 0;

			List<Integer> indexArray = new ArrayList<Integer>(
					indexList.keySet());
			Collections.sort(indexArray);

			for (Integer i : indexArray) {
				Integer valueIndex = indexList.get(i);

				mod.insert(i + offset, startDiv);
				offset += startDiv.length();

				mod.insert(i+offset+5, height);
				offset += height.length();

				mod.insert(valueIndex + offset, endDiv);
				offset += endDiv.length();
			}

			imagePaths = imgList;
		}

		return imagePaths;
	}
}