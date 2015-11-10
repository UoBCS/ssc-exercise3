package utils;

import java.util.ArrayList;

public class Utils {

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
	
}
