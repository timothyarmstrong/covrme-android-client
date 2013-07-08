package me.covr;

import android.app.Activity;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by andy on 2013-07-07.
 */
public class DeliveryActivity extends Activity {
  public static String TAG = "covr.me";
  final private ByteArrayBody[] bab = {null};

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_delivery);

    final Camera c = Camera.open(0);

    Camera.PictureCallback jpegcb = new Camera.PictureCallback() {
      public void onPictureTaken(byte[] data, Camera camera) {
        bab[0] = new ByteArrayBody(data, "upload.jpg");
        c.release();
      }
    };

//    Camera.PictureCallback jpegCallback = (byte[] data, Camera camera) -> {
//      bab[0] = new ByteArrayBody(data, "upload.jpg");
//      c.release();
//    };

    try {
      SurfaceView dummy = new SurfaceView(this);
      c.setPreviewDisplay(dummy.getHolder());
      c.startPreview();
      c.takePicture(null, null, jpegcb);
    } catch (IOException e) {
      e.printStackTrace();
    }

    new RequestTask().execute(getFirstPost());
  }

  private HttpPost getFirstPost() {
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

  private class Args {
    public String url;
    public String arbitrary;
    public int id;
    public int doorid;

    public Args(String url, int id, String arb) {
      this.url = url;
      this.id = id;
      this.doorid = 65432353;
      this.arbitrary = arb;
    }
  }

  private static String parseResponse(HttpResponse resp) {
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

  private class UploadTask extends AsyncTask<Args, Void, Args> {
    @Override
    protected Args doInBackground(Args... args) {
      String r = "";
      Args a = args[0];
      HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost(a.url);

      MultipartEntity ent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      try {
        ent.addPart("doorbellId", new StringBody(Integer.toString(a.doorid)));
        ent.addPart("visitorId", new StringBody(Integer.toString(a.id)));
        ent.addPart("file", bab[0]);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      post.setEntity(ent);

      Log.d(TAG, Integer.toString(a.id));
      try {
        HttpResponse resp = client.execute(post);
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
      a.arbitrary = r;
      return a;
    }

    @Override
    protected void onPostExecute(Args a) {
      TextView t = (TextView) findViewById(R.id.textView);
      t.setText("Waiting for response...");
      new PollTask().execute(a);
    }

  }

  private class PollTask extends AsyncTask<Args, Void, String> {
    @Override
    protected String doInBackground(Args... reqs) {
      Args a = reqs[0];
      HttpClient client = new DefaultHttpClient();
      Log.d(TAG, Integer.toString(a.id));
      HttpGet get = new HttpGet("http://covrme-dev-armstrong-timothy.appspot.com/doorbells/65432353/visitors/" + Integer.toString(a.id) + "/messages");

      String content = "No answer received.";
      HttpResponse resp = null;
      int count = 60;
      while (count > 0) {
        Log.d(TAG, Integer.toString(a.id));
        Log.d(TAG, Integer.toString(count));
        try {
          resp = client.execute(get);
          String body = parseResponse(resp);
          Log.d(TAG, body);
          JSONArray ar = new JSONArray(body);
          if (ar.length() == 0) {
            count--;
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          } else {
            content = ar.getJSONObject(0).getString("Content");
            count = 0;
            return content;
          }
        } catch (IOException e) {
          e.printStackTrace();
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
      return content;
    }

    @Override
    protected void onPostExecute(String body) {
      TextView t = (TextView) findViewById(R.id.textView);
      t.setText(body);
    }

  }

  private class RequestTask extends AsyncTask<HttpPost, Void, String> {
    @Override
    protected String doInBackground(HttpPost... reqs) {
      // silly... but w/e
      String r = "";
      for (HttpPost post : reqs) {
        HttpClient client = new DefaultHttpClient();

        try {
          HttpResponse resp = client.execute(post);
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
      }
      return r;
    }

    @Override
    protected void onPostExecute(String body) {
      try {
        JSONObject json = new JSONObject(body);
        int id = json.getInt("Id");
        String upload = json.getString("PhotoUploadUrl");
        final Args arg = new Args(upload, id, "");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          public void run() {
            new UploadTask().execute(arg);
          }
        }, 1500);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }
}