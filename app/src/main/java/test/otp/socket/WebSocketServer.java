package test.otp.socket;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

class RequestTypes{
  static final String SEND_OTP = "send_otp";
  static final String SENT_OTP = "otp_sent";
  static final String VALIDATE_OTP = "otp_validate";
  static final String VALIDATED_OTP = "otp_validated";
}

@ServerEndpoint("/")
public class WebSocketServer {

  String getOTP(){
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("type", RequestTypes.SENT_OTP);
    jsonObject.put("data", 12345);
    return jsonObject.toString();
  }

  String validatedOTP(){
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("type", RequestTypes.VALIDATED_OTP);
    jsonObject.put("data", "Found valid otp");
    return jsonObject.toString();
  }

  String validatedOTPError(){
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("type", RequestTypes.VALIDATED_OTP);
    jsonObject.put("data", "Found invalid otp");
    return jsonObject.toString();
  }

  boolean isValidOtp(String request){
    JSONObject jsonObject = new JSONObject(request);
    return Integer.parseInt(jsonObject.get("data").toString()) == 12345;
  }

  private static Set<Session> clients =
      Collections.synchronizedSet(new HashSet<Session>());

  @OnMessage
  public void onMessage(String message, Session session)
      throws IOException {
    JSONObject object = new JSONObject(message);
    String type = object.get("type").toString();
    switch (type) {
      case RequestTypes.SEND_OTP:
        session.getBasicRemote().sendText(getOTP());
        break;
      case RequestTypes.VALIDATE_OTP:
        if (isValidOtp(message))
          session.getBasicRemote().sendText(validatedOTP());
        else
          session.getBasicRemote().sendText(validatedOTPError());
        break;
      default:
        session.getBasicRemote().sendText("Sorry buddy, I can't understand you");
    }
  }

  @OnOpen
  public void onOpen (Session session) {
    // Add session to the connected sessions set
    clients.add(session);
    System.out.println("added new client, " + session.getId());
  }

  @OnClose
  public void onClose (Session session) {
    // Remove session from the connected sessions set
    clients.remove(session);
    System.out.println("removed client, " + session.getId());
  }
}