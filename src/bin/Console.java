package bin;

import java.util.Scanner;

public class Console extends Thread {
	
	public String last = "", s = "";
	
	public void run() {
		Scanner sc = new Scanner(System.in);
		while(!Main.close) {
			if(sc.hasNext()) {
				last = sc.nextLine();
				if(!last.equalsIgnoreCase(s)) {
					s = last;
				}
			} else {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		sc.close();
	}
}
