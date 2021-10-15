package test.otp.socket;

import org.glassfish.tyrus.server.Server;

import javax.websocket.WebSocketContainer;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class App {
  public static void main(String[] args) throws Exception {
    runServer();
  }
  static public void runServer() {
    Server server = new Server("localhost", 8080, "/", null, WebSocketServer.class);

    try {
      server.start();
      BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
      System.out.print("Please press a key to stop the server.");
      reader.readLine();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      server.stop();
    }
  }
}
