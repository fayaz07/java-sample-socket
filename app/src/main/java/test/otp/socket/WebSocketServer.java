package test.otp.socket;

import org.json.JSONObject;

import java.io.IOException;
import java.util.*;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

class RequestTypes {
  static final String REQUEST_OTP = "request_otp";
  static final String HELLO = "hello";
  static final String VALIDATE_OTP = "otp_validate";
  static final String SET_FCM_CLIENT_ID = "send_fcm_token";
}

class ResponseTypes {
  static final String ERROR = "error";
  static final String VALIDATED_OTP = "otp_validated";
  static final String SENT_OTP = "otp_sent";
  static final String FCM_ACK = "fcm_ack";
}

class MobileClient {
  Session session;
  String clientId;
  String fcmToken;

  public MobileClient(Session session, String clientId, String fcmToken) {
    this.session = session;
    this.clientId = clientId;
    this.fcmToken = fcmToken;
  }
}

@ServerEndpoint("/")
public class WebSocketServer {

  private static Map<String, Session> webClients =
      new HashMap<String, Session>();

  private static Map<String, MobileClient> mobileClients =
      new HashMap<String, MobileClient>();

  String getOTP() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("type", ResponseTypes.SENT_OTP);
    jsonObject.put("data", 12345);
    return jsonObject.toString();
  }

  String validatedOTP() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("type", ResponseTypes.VALIDATED_OTP);
    jsonObject.put("data", "Found valid otp");
    return jsonObject.toString();
  }

  String validatedOTPError() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("type", ResponseTypes.VALIDATED_OTP);
    jsonObject.put("data", "Found invalid otp");
    return jsonObject.toString();
  }

  boolean isValidOtp(String request) {
    JSONObject jsonObject = new JSONObject(request);
    return Integer.parseInt(jsonObject.get("data").toString()) == 12345;
  }

  @OnMessage
  public void onMessage(String message, Session session)
      throws IOException {
    System.out.println(message);
    JSONObject object = new JSONObject(message);
    String type = object.get("type").toString();
    switch (type) {
      case RequestTypes.HELLO:
        if (object.get("client").toString().equals("mobile")) {
          System.out.println("adding mobile client");
          mobileClients.put(object.get("clientId").toString(), new MobileClient(session, "", ""));
        }
        session.getBasicRemote().sendText(sayHello());
        break;
      case RequestTypes.SET_FCM_CLIENT_ID:
        System.out.println("adding mobile client and fcm token");
        mobileClients.put(object.get("clientId").toString(),
            new MobileClient(session, object.getString("clientId"), object.getString("fcmToken"))
        );
        session.getBasicRemote().sendText(sendFCMAcknowledgement());
        break;
      case RequestTypes.REQUEST_OTP:
        System.out.println(mobileClients);
        if (mobileClients.size() == 0) {
          session.getBasicRemote().sendText(noMobileClients());
        } else {
          mobileClients.get(object.get("clientId").toString()).session.getBasicRemote().sendText(getOTP());
          session.getBasicRemote().sendText(sentOTPToMobileClients());
        }
        break;
      case RequestTypes.VALIDATE_OTP:
        if (isValidOtp(message))
          session.getBasicRemote().sendText(validatedOTP());
        else
          session.getBasicRemote().sendText(validatedOTPError());
        break;
      default:
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("type", ResponseTypes.ERROR);
        jsonObject.put("data", "Sorry buddy, I can't understand you");
        session.getBasicRemote().sendText(jsonObject.toString());
    }
  }

  private String sendFCMAcknowledgement() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("type", ResponseTypes.FCM_ACK);
    jsonObject.put("data", "I have stored the FCM Token");
    return jsonObject.toString();
  }

  private String sentOTPToMobileClients() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("type", ResponseTypes.SENT_OTP);
    jsonObject.put("data", "OTP has been sent");
    return jsonObject.toString();
  }

  private String noMobileClients() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("type", ResponseTypes.ERROR);
    jsonObject.put("data", "No mobile clients out there");
    return jsonObject.toString();
  }

  private String sayHello() {
    JSONObject jsonObject = new JSONObject();
    jsonObject.put("type", RequestTypes.HELLO);
    jsonObject.put("data", "Hey bud");
    return jsonObject.toString();
  }

  @OnOpen
  public void onOpen(Session session) {
    // Add session to the connected sessions set
//    webClients.set(session.getId(), session);
    System.out.println("added new client, " + session.getId());
  }

  @OnClose
  public void onClose(Session session) {
    // Remove session from the connected sessions set
//    clients.remove(session);
    System.out.println("removed client, " + session.getId());
  }
}