package demo.skdroid.sunkaisens.com.demo.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;

import demo.skdroid.sunkaisens.com.demo.R;

public class LoginActivity extends AppCompatActivity {

    private EditText userName;
    private EditText pwd;
    private Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = findViewById(R.id.user_name);
        pwd = findViewById(R.id.pwd);
        login = findViewById(R.id.login);

//        login.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                NgnSKClient.getInstence().login(userName.getText().toString().trim(), pwd.getText().toString().trim());
//            }
//        });
//        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                NgnSKClient.getInstence().logout();
//            }
//        });
//        findViewById(R.id.imageButton).setOnClickListener(new View.OnClickListener() {
//            @Override

//            public void onClick(View v) {
//                userName.setText("");
//            }
//        });
    }
}
