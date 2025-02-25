package com.example.hellotoastver2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private int mCount;
    private Button button_toast, button_zero, button_count;
    private TextView mShowCount;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        button_toast = (Button) findViewById(R.id.button_toast);
        button_count = (Button) findViewById(R.id.button_count);
        button_zero = (Button) findViewById(R.id.button_zero);
        mShowCount = (TextView) findViewById(R.id.show_count);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        mCount = getSharedPreferences("HelloToast", MODE_PRIVATE).getInt("count", 0);
        mShowCount.setText(String.valueOf(mCount));
    }

    @Override
    protected void onPause(){
        super.onPause();
        System.out.println("thoat app");
    }

    public void showToast(View v){
        Toast toast = Toast.makeText(MainActivity.this, R.string.toast_message, Toast.LENGTH_SHORT );
        toast.show();
    }

    public void resetCount(View v){
        v.setEnabled(false);
        mCount = 0;
        mShowCount.setText(String.format("%d",mCount));
        getSharedPreferences("HelloToast", MODE_PRIVATE)
                .edit()
                .putInt("count", mCount)
                .apply();

    }

    public void countUp(View v){
        mCount++;
        if(mShowCount != null)
            mShowCount.setText(String.format("%d",mCount));
        if(button_zero.isEnabled() == false)
            button_zero.setEnabled(true);
        getSharedPreferences("HelloToast", MODE_PRIVATE)
                .edit()
                .putInt("count", mCount)
                .apply();

    }


}