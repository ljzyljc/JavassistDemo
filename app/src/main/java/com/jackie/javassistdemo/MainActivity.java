package com.jackie.javassistdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.jackie.testlib.MyClass;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        Log.i("jackie========","=========: "+testTryCatch(99));
        MyClass myClass = new MyClass();
        myClass.testCrash();
        Log.i("jackie========","=========: ");
    }

    private double testTryCatch(long a1) {
        System.out.println("======try-catch===float==");
        long a = 10/0;
        return 9;
    }
}
