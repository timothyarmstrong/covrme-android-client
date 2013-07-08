package me.covr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class FullscreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);
    }


  public void deliver(View view) {
    Intent intent = new Intent(this, DeliveryActivity.class);
    startActivity(intent);
  }
}
