package utils;

import java.util.ArrayList;

/**
 * Contains utility methods
 */
public class Utils {

	/**
	 * Joins a list of strings given a delimiter
	 * @param delimiter The delimiter
	 * @param list The strings to join
	 * @return The joined string
	 */
	public static String join(String delimiter, ArrayList<String> list) {
		
		String res = "";
		int size = list.size();
		
		for (int i = 0; i < size; i++) {
			String str = list.get(i);
			
			if (i == size - 1) {
				res += str;
			}
			else {
				res += (str + delimiter);
			}
		}
		
		return res;
	}
	
	/**
	 * Check if 'text' contains the given 'keywords'
	 * @param text The text to search
	 * @param keywords The keywords
	 * @return True if one keyword is in the text, false otherwise.
	 */
	public static boolean contains(String text, String[] keywords) {
		
		for (String keyword : keywords) {
			if (text.contains(keyword)) {
				return true;
			}
		}
		
		return false;
	}
	
}
