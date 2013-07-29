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
 * Created by andy on 2013-07-28.
 */
abstract public class ApproachActivity extends Activity {
  public static String TAG = "covr.me";
  final private ByteArrayBody[] bab = {null};
  abstract protected String getDesc();

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_delivery);

    final Camera c = Camera.open(0);

    Camera.PictureCallback jpegcb = new Camera.PictureCallback() {
      @Override
      public void onPictureTaken(byte[] bytes, Camera camera) {
        bab[0] = new ByteArrayBody(bytes, "upload.jpg");
        if (c != null) {
          c.release();
        }
      }
    };

    try {
      if (c != null) {
        SurfaceView dummy = new SurfaceView(this);
        c.setPreviewDisplay(dummy.getHolder());
        c.startPreview();
        c.takePicture(null, null, jpegcb);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    String doorId = "65432353";
    //String doorId = "04450031";
    HttpPost post = new HttpPost("http://covrme-dev-armstrong-timothy.appspot.com/doorbells/" + doorId + "/visitors");
    MultipartEntity ent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

    try {
      ent.addPart("description", new StringBody(this.getDesc()));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }

    post.setEntity(ent);
    new RequestTask().execute(post);
  }

  private class RequestTask extends AsyncTask<HttpPost, Void, String> {
    @Override
    protected String doInBackground(HttpPost... reqs) {
      // silly... but w/e
      String r = "";
      try {
        for (HttpPost post : reqs) {
          HttpClient client = new DefaultHttpClient();
          HttpResponse resp = client.execute(post);
          BufferedReader reader = new BufferedReader(new InputStreamReader(resp.getEntity().getContent(), "UTF-8"));
          String response;
          StringBuilder s = new StringBuilder();
          while ((response = reader.readLine()) != null) {
            s = s.append(response);
          }
          r = s.toString();
        }
      } catch (IOException e) {
        e.printStackTrace();
      } catch (Exception e) {
        Log.d(TAG, "catch all?");
      }
      return r;
    }

    @Override
    protected void onPostExecute(String body) {
      try {
        JSONObject json = new JSONObject(body);
        int id = json.getInt("id");
        String visitorId = Integer.toString(id);
        String upload = json.getString("photo_upload_url");
        final Util.Args arg = new Util.Args(upload, visitorId);
        (new Handler()).postDelayed(new Runnable() {
          @Override
          public void run() {
            new UploadTask().execute(arg);
          }
        }, 1500);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  private class UploadTask extends AsyncTask<Util.Args, Void, Util.Args> {
    @Override
    protected Util.Args doInBackground(Util.Args... args) {
      String r = "";
      Util.Args a = args[0];
      if (bab[0] == null) {
        return a;
      }
      HttpClient client = new DefaultHttpClient();
      HttpPost post = new HttpPost(a.url);


      MultipartEntity ent = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      try {
        ent.addPart("doorbellId", new StringBody(a.doorid));
        ent.addPart("visitorId", new StringBody(a.visitorId));
        ent.addPart("file", bab[0]);
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      post.setEntity(ent);

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
      return a;
    }

    @Override
    protected void onPostExecute(Util.Args a) {
      TextView t = (TextView) findViewById(R.id.textView);
      t.setText("Waiting for response...");
      new PollTask().execute(a);
    }
  }

  private class PollTask extends AsyncTask<Util.Args, Void, String> {
    @Override
    protected String doInBackground(Util.Args... reqs) {
      Util.Args a = reqs[0];
      HttpClient client = new DefaultHttpClient();
      String doorid = "65432353";
      //String doorid = "04450031";
      HttpGet get = new HttpGet("http://covrme-dev-armstrong-timothy.appspot.com/doorbells/" + doorid + "/visitors/" + a.visitorId + "/messages");

      String content = "No answer received.";
      HttpResponse resp = null;
      int count = 60;
      while (count > 0) {
        try {
          resp = client.execute(get);
          String body = Util.parseResponse(resp);
          JSONArray ar = new JSONArray(body);
          if (ar.length() == 0) {
            count--;
            try {
              Thread.sleep(1000);
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
          } else {
            content = ar.getJSONObject(0).getString("content");
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
}