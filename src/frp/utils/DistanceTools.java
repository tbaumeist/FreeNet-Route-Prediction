package frp.utils;

import java.text.DecimalFormat;

public class DistanceTools {
	private final static String doublePrecision = "#.######";
	
	
	public static double getDistance(double a, double b) {
		if (a > b)
			return Math.min(a - b, 1.0 - a + b);
		return Math.min(b - a, 1.0 - b + a);
	}
	
	public static double calcMidPt(double locA, double locB) {
		// distance = (( next +1 ) - me) % 1
		double dist = ((locB + 1) - locA) % 1;
		// middle = (( distance/2 ) + me ) % 1
		return ((dist / 2.0) + locA) % 1;
	}
	
	public static double round(double d) {
		return round(d, doublePrecision);
	}
	
	public static double round(double d, String format) {
		DecimalFormat twoDForm = new DecimalFormat(format);
		return Double.valueOf(twoDForm.format(d));
	}
}
