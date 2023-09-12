package lib;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Info {
	
	public static void write(Object obj) {
		System.out.print(obj);
	}
	
	public static void writeln(Object obj) {
		System.out.println(obj);
	}
	
	public static void ln() {
		System.out.println();
	}
	
	public static void title(Object obj) {
		System.out.println("==="+obj+"===");
	}
	
	public static void warning(Object obj) {
		System.out.println("WARNING! "+obj+"!");
	}
	
	public static void info(Object obj) {
		System.out.println("Info: "+obj);
	}
	
	public static void error(Object obj) {
		System.err.println("#ERR: "+obj);
	}
	
	public static void log(Object obj) {
		
		System.out.println("Log: "+obj);
	}
	
	public static String getDateAndTime() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/YY HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public static String getDate() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/YY");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	public static String getTime() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
}
