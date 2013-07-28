package me.covr;

import android.app.Activity;
import android.os.Bundle;

import org.apache.http.client.methods.HttpPost;

/**
 * Created by andy on 2013-07-28.
 */
public class PersonalActivity extends ApproachActivity {
  @Override
  HttpPost getFirstPost() {
    return null;
  }

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //TODO: activity_personal
    setContentView(R.layout.activity_delivery);
  }
}