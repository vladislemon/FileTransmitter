package bin;

import java.io.*;
import java.net.Socket;

import lib.BytesUtil;
import lib.Info;

public class Connection extends Thread {
	
	Socket socket;
	BufferedOutputStream out;
	BufferedInputStream in;
	private int blockSize = 1024 * 1024; // 1 Mb

	public Connection(Socket socket) {
		this.socket = socket;
		try {
			out = new BufferedOutputStream(socket.getOutputStream());
			in = new BufferedInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while(true) {
			if(Main.close || socket.isClosed()) {
				close();
				/*try {
					this.finalize();
				} catch (Throwable e) {
					e.printStackTrace();
				}*/
				break;
			}
			try {
				if(Main.transfer || (Main.accept && in.available() > 0)) {
					BufferedInputStream fileIn = null;
					BufferedOutputStream fileOut = null;
					int nameSize, avaliable = 0;
					long size;
					String name, fileName = "";
					if(Main.transfer) {
						fileIn = new BufferedInputStream(new FileInputStream(Main.in));
						size = Main.in.length();
						name = Main.in.getName();
						nameSize = name.getBytes().length;
						out.write(nameSize);
						out.write(name.getBytes());
						out.write(BytesUtil.toBytes(size));
						avaliable = in.read();
						if(avaliable > 0) {
							fileIn.skip(avaliable);
						}
					} else {
						nameSize = in.read();
						byte[] b = new byte[nameSize];
						in.read(b);
						name = new String(b);
						byte[] bytes = new byte[8];
						in.read(bytes);
						size = BytesUtil.toLong(bytes);
						Main.status = "Downloading file " + name + " (" + size/1024 + "kbytes)";
						Main.status1 = "";
						Main.status2 = "";
						Main.writeStatus();
						fileName = "downloads" + File.separator + name;
						File file = new File(fileName + ".part");
						if(file.exists()) {
							avaliable = (int)file.length();
							fileOut = new BufferedOutputStream(new FileOutputStream(file, true));
						} else {
							fileOut = new BufferedOutputStream(new FileOutputStream(file, false));
						}
						out.write(avaliable);
					}
					byte[] bytes;
					long time, last = System.nanoTime()/1000000, write = System.nanoTime()/1000000;
					long n = size - avaliable;
					int a;
					
					while(true) {
						time = System.nanoTime()/1000000;
						if(Main.transfer) {
							assert fileIn != null;
							a = fileIn.available();
						} else {
							a = in.available();
						}
						
						if(a == 0) {
							try {
								Thread.sleep(50);
								continue;
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						if(!Main.transfer || a < blockSize) {
							n -= a;
							bytes = new byte[a];
						} else {
							n -= blockSize;
							bytes = new byte[blockSize];
						}
						
						if(Main.transfer) {
							assert fileIn != null;
							fileIn.read(bytes);
							writeToStream(out, bytes);
						} else {
							in.read(bytes);
							assert fileOut != null;
							fileOut.write(bytes);
						}
						
						if(time >= write + 1000) {
							write = System.nanoTime()/1000000;
							if(Main.transfer) {
								writeStat(n, size, time, last, size-n, true);
							} else {
								writeStat(n, size, time, last, size-n, false);
							}
						}
						if(n <= 0) {
							break;
						}
					}
					if(Main.transfer) {
						assert fileIn != null;
						fileIn.close();
						Main.status = "File " + name + " sent";
						Main.writeStatus();
						Main.transfer = false;
					} else {
						assert fileOut != null;
						fileOut.close();
						if(new File(fileName).exists()) {
							for(int i = 1; true; i++) {
								String[] s = fileName.split("\\.");
								if(!new File(s[0] + i + s[1]).exists()) {
									new File(fileName + ".part").renameTo(new File(s[0] + i + "." + s[1]));
									break;
								}
							}
						} else {
							new File("downloads" + File.separator + name + ".part")
								.renameTo(new File("downloads" + File.separator + name));
						}
						Main.status = "File " + name + " is loaded";
						Main.writeStatus();
					}
					writeFinalStat(time, last, size, avaliable);
				}
			} catch (IOException e) {
				e.printStackTrace();
				Main.disconnect();
			}
			try {
				Thread.sleep(5);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	void writeFinalStat(long time, long last, long size, int avaliable) {
		String elapsedTime = "Elapsed time: " + (int)((time-last)/1000) + "s";
		String averageSpeed = "Average speed: " +
				limitFraction((float)(size-avaliable)/1024f/(float)(time-last)*1000f, 1) + " kb/s";
		Main.status1 = elapsedTime+"|	"+averageSpeed;
		Main.writeStatus();
	}
	
	void writeStat(long n, long size, long time, long last, long length, boolean uploader) {
		String procent = limitFraction(100f-100f*(float)n/(float)size, 2) + "%";
		String speed = limitFraction((float)(size-n)/1024f/(float)(time-last)*1000f, 1) + " kb/s";
		String data;
		if(uploader) {
			data = "Up: " + (int)((float)length/1024f) + " kb";
		} else {
			data = "Down: " + (int)((float)length/1024f) + " kb";
		}
		String elapsedTime = "Elapsed: " + (int)((time-last)/1000) + "s";
		String timeLeft = "Left: " + (int)(((float)n/1024f)/((float)(size-n)/1024f/(float)(time-last)*1000f)) + "s";
		
		for(int i = 0; i < 47; ++i) Info.ln();
		Main.status1 = procent+"|	"+speed+"|	"+data+"|	"+elapsedTime+"|	"+timeLeft;
		Main.writeStatus();
	}
	
	float limitFraction(float f, int num) {
		float limit = (float)Math.pow(10, num);
		return (int)(f*limit)/limit;
	}
	
	void writeToStream(BufferedOutputStream dos, byte[] b) {
		while(true) {
			if(Main.close || socket.isClosed()) {
				close();
				/*try {
					this.finalize();
				} catch (Throwable e) {
					e.printStackTrace();
				}*/
				break;
			}
			try {
				dos.write(b);
				dos.flush();
				break;
			} catch (IOException e) {
				Main.status1 = "Waiting for socket...";
				Main.writeStatus();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public void close() {
		try {
			socket.close();
			out.close();
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
