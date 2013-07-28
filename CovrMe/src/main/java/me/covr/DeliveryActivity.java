package me.covr;

import android.os.Bundle;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import java.io.UnsupportedEncodingException;

/**
 * Created by andy on 2013-07-07.
 */

// TODO(ampersandy): Rewrite anonymous classes as lambdas

public class DeliveryActivity extends ApproachActivity {
  public static String TAG = "covr.me";
  final private ByteArrayBody[] bab = {null};

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_delivery);
  }

  @Override
  HttpPost getFirstPost() {
    HttpPost post = new HttpPost("http://covrme-dev-armstrong-timothy.appspot.com/doorbells/65432353/visitors");
    MultipartEntity ent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

    try {
      ent.addPart("description", new StringBody("Delivery!"));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    post.setEntity(ent);
    return post;
  }
}