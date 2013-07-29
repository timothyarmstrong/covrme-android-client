package me.covr;

import org.apache.http.HttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
/**
 * Created by andy on 2013-07-28.
 */
public class Util {
  public static class Args {
    public String url;
    public String arbitrary;
    public String visitorId;
    public String doorid;

    public Args(String url, String id, String arb) {
      this.url = url;
      this.visitorId = id;
      //this.doorid = 65432353;
      this.doorid = "04450031";
      this.arbitrary = arb;
    }
  }

  public static String parseResponse(HttpResponse resp) {
    String r = "";
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
      String response;
      StringBuilder s = new StringBuilder();
      while ((response = reader.readLine()) != null) {
        s = s.append(response);
      }
      r = s.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return r;
  }
}
