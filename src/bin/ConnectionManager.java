package bin;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class ConnectionManager extends Thread {
	
	Connection connection;
	
	public void run() {
		while(true) {
			if(Main.close) {
				assert connection != null;
				connection.close();
				break;
			}
			if((connection != null && connection.isAlive()) || (!Main.connected && Main.isServer == 0)) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			if(Main.isServer == 1) {
				try {
					ServerSocket socket = new ServerSocket(Main.port);
					//socket.setReceiveBufferSize(1024 * 1024 * 4);
					//socket.setPerformancePreferences(0, 1, 2);
					connection = new Connection(socket.accept());
					//connection.socket.setSendBufferSize(1024*1024*4);
					connection.start();
					socket.close();
					Main.connected = true;
					Main.status = "Client connected [" + connection.socket.getInetAddress() + "]";
					Main.writeStatus();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if(Main.isServer == 0 && !Main.ip.equals("")) {
				try {
					Socket socket = new Socket(InetAddress.getByName(Main.ip), Main.port);
					//socket.setReceiveBufferSize(1024*1024*4);
					//socket.setSendBufferSize(1024*1024*4);
					//socket.setPerformancePreferences(0, 1, 2);
					connection = new Connection(socket);
					connection.start();
					Main.connected = true;
					Main.status = "Connected to server [" + Main.ip + "]";
					Main.writeStatus();
				} catch (IOException e) {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}
}
