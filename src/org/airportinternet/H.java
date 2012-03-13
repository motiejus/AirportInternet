package org.airportinternet;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class H {
	public static String join(Object[] s, String delimiter) {
		return join(Arrays.asList(s), delimiter);
	}
	
	public static String join(Collection<?> s, String delimiter) {
	     StringBuilder builder = new StringBuilder();
	     Iterator<?> iter = s.iterator();
	     while (iter.hasNext()) {
	         builder.append(iter.next());
	         if (!iter.hasNext()) {
	           break;                  
	         }
	         builder.append(delimiter);
	     }
	     return builder.toString();
	 }
}
