package com.vodre.labs;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        
//        Intent intent = new Intent(MainActivity.this, BubbleDrawerService.class);
//        startService(intent);
        finish();
    }

    
}
