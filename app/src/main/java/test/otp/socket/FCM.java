package test.otp.socket;

import okhttp3.*;
import okio.Buffer;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;

public class FCM {
  private static String apiKey = "";

  static void sendNotification(String clientFCM, String otp) {
    OkHttpClient client = new OkHttpClient().newBuilder()
        .build();

    JSONObject dataObject = new JSONObject();
    dataObject.put("otp", otp);

//    System.out.println(dataObject.toString());

    JSONObject notificationObject = new JSONObject();
    notificationObject.put("title", "Authenticator");
    notificationObject.put("body", "You have a new authentication token");

//    System.out.println(notificationObject.toString());

    JSONArray jsonArray = new JSONArray();
    jsonArray.put(clientFCM);

    JSONObject jsonBody = new JSONObject();
    jsonBody.put("priority", "HIGH");
    jsonBody.put("data", dataObject);
    jsonBody.put("notification", notificationObject);
    jsonBody.put("registration_ids", jsonArray);

    MediaType mediaType = MediaType.parse("application/json");

    RequestBody body = RequestBody.create(jsonBody.toString(), mediaType);
//    .create(mediaType, "{\n  \"priority\":\"HIGH\",\n  \"data\":{\n\n  },\n  \"to\":\"VALID_FCM_TOKENS\"\n}");
    Request request = new Request.Builder()
        .url("https://fcm.googleapis.com/fcm/send")
        .method("POST", body)
        .addHeader("Content-Type", "application/json")
        .addHeader("Authorization", "key=" + apiKey)
        .build();

    try {
      System.out.println(bodyToString(request));
//      System.out.println("clientFCM = " + clientFCM + ", otp = " + otp);
      Response response = client.newCall(request).execute();
      System.out.println("Notification status: " + response.body().string() + " statusCode: " + response.code());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String bodyToString(final Request request) {
    try {
      final Request copy = request.newBuilder().build();
      final Buffer buffer = new Buffer();
      copy.body().writeTo(buffer);
      return buffer.readUtf8();
    } catch (final IOException e) {
      return "did not work";
    }
  }
}
