package bin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import lib.Info;

public class Main {
		public static File in;
		public static int isServer = -1;
		public static int port = 4225;
		public static boolean transfer = false, close = false, connected = false, accept = false;
		public static String ip = "", myip = "", status = "", status1 = "", status2 = "", space = "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n";
		static ConnectionManager cm;
		static Console console;

	public static void main(String[] args) throws IOException, InterruptedException {
        /*Gui window = new Gui();
        window.setVisible(true);
        window.setResizable(false);
        window.setLocationRelativeTo(null);*/
        String ANSI_RED = "\u001B[31m";
        String ANSI_RESET = "\u001B[0m";
        System.out.println(ANSI_RED + "This text is red!" + ANSI_RESET);

		if(!new File("downloads").exists()) {
			if(!new File("downloads").mkdir()) {
				System.exit(0);
			}
		}
		if(!checkArguments(args)) {
			stopProgram();
		}
		myip = getCurrentIP();
		writeStatus();
		console = new Console();
		console.start();
		cm = new ConnectionManager();
		cm.start();
		String temp;
		while(true) {
			temp = console.s;
			console.s = "";
			if(temp.equalsIgnoreCase("exit")) {
				close = true;
				break;
			} else
			if(temp.equalsIgnoreCase("client")) {
				isServer = 0;
				writeStatus();
			} else
			if(temp.equalsIgnoreCase("server")) {
				isServer = 1;
				writeStatus();
			} else
			if(temp.equalsIgnoreCase("connect")) {
				connected = true;
				status = "Connecting...";
				writeStatus();
			} else
			if(temp.equalsIgnoreCase("disconnect")) {
				disconnect();
				writeStatus();
			} else
			if(temp.contains("send")) {
				if(connected) {
					String[] s = temp.split("\\s+");
					if(s.length > 1) {
						String name = s[1];
						File f = new File(name);
						if(!f.exists() && f.isFile()) {
							Info.writeln("File not found! " + name);
						}
						in = f;
						transfer = true;
						status = "Sending file: " + name + " (" + f.length()/1024 + " kbytes)";
						status1 = "";
						status2 = "";
						writeStatus();
					}
				}
			} else
			if(temp.contains("accept")) {
				String[] s = temp.split("\\s+");
				if(s.length > 1) {
					int i = Integer.parseInt(s[1]);
					if(i == 0) {
						accept = false;
					} else if(i == 1) {
						accept = true;
					}
					writeStatus();
				}
			} else
			if(temp.contains("ip")) {
				String[] s = temp.split("\\s+");
				if(s.length > 1) {
					ip = s[1];
					writeStatus();
				}
			} else
			if(temp.contains("port")) {
				String[] s = temp.split("\\s+");
				if(s.length > 1) {
					port = Integer.parseInt(s[1]);
					destroyConnection();
					writeStatus();
				}
			}
			Thread.sleep(20);
		}
		
		stopProgram();
	}
	
	public static void destroyConnection() {
		close = true;
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		cm.connection = null;
		cm = new ConnectionManager();
		cm.start();
		console = new Console();
		console.start();
		System.gc();
	}
	
	public static void disconnect() {
		cm.connection.close();
		Main.connected = false;
	}
	
	public static void writeStatus() {
		String[] lines = new String[24];
		lines[0] = "===File Transmitter v0.2===";
		String s = " ";
		if(isServer == 1) s = "server"; else
		if(isServer == 0) s = "client";
		lines[1] = "Mode: " + s;
		if(isServer == 0 && !ip.equals(""))
		lines[2] = "Server ip: " + ip; else
		lines[2] = "Your ip: " + myip;
		lines[3] = "Port: " + port;
		lines[4] = "Connected: " + connected;
		lines[5] = "Accept files: " + accept;
		lines[6] = "Commands:";
		lines[7] = "\"client\" - set client mode, \"server\" - set server mode,";
		lines[8] = "\"ip ****\" - set server ip (**** - ip), \"send ****\" - send file (**** - name),";
		lines[9] = "\"connect\" - connect to server, \"disconnect\" - close connection,";
		lines[10] = "\"accept 1/0\" - accept/not accept files, \"exit\" - close program,";
		lines[11] = "\"port ****\" - set port (**** - number)";
		lines[13] = status;
		lines[14] = status1;
		lines[15] = status2;
		
		Info.writeln(space);
		for (String line : lines) {
			if (line != null) {
				Info.writeln(line);
			} else {
				Info.ln();
			}
		}
	}
	
	static boolean checkArguments(String[] args) throws UnknownHostException {
		int a1 = -1;
		if(args.length == 0) {
			return true;
		}
		if(new File(args[0]).exists()) {
			in = new File(args[0]);
			return true;
		}
		if(contains(args, "-client") != -1 && contains(args, "-server") != -1) {
			a1 = contains(args, "-client");
		} else if(contains(args, "-client") != -1) {
			isServer = 0;
		} else if(contains(args, "-server") != -1) {
			isServer = 1;
		}
		if(contains(args, "-file") != -1) {
			if(new File(args[contains(args, "-file") + 1]).exists()) {
				in = new File(args[contains(args, "-file") + 1]);
			}
		}
		if(contains(args, "-ip") != -1) {
			ip = args[contains(args, "-ip") + 1];
		}
		if(a1 != -1) {
			Info.error("Invalid arguments!");
			return false;
		}
		
		return true;
	}
	
	static void stopProgram() throws IOException {
		System.exit(0);
	}
	
	static int contains(String[] array, String s) {
		for(int i = 0; i < array.length; i++) {
			if(array[i].equalsIgnoreCase(s)) {
				return i;
			}
		}
		return -1;
	}
	
	private static String getCurrentIP() {
        String result = null;
        try {
            BufferedReader reader = null;
            try {
                URL url = new URL("http://myip.by/");
                InputStream inputStream;
                inputStream = url.openStream();
                reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder allText = new StringBuilder();
                char[] buff = new char[1024];
 
                int count;
                while ((count = reader.read(buff)) != -1) {
                    allText.append(buff, 0, count);
                }
                Integer indStart = allText.indexOf("\">whois ");
                Integer indEnd = allText.indexOf("</a>", indStart);
 
                String ipAddress = allText.substring(indStart + 8, indEnd);
                if (ipAddress.split("\\.").length == 4) {
                    result = ipAddress;
                }
            } catch (MalformedURLException ex) {
                 ex.printStackTrace();
            } catch (IOException ex) {
                 ex.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
}
