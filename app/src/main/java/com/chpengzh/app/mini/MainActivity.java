package com.chpengzh.app.mini;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.EditText;

import com.android.volley.VolleyError;

import org.aquery.core.annotation.param.NetError;
import org.aquery.core.annotation.param.ResponseBody;
import org.aquery.core.annotation.method.Handler;
import org.aquery.core.Function;
import org.aquery.core.AQuery;

import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/***
 * @author chpengzh
 */
public class MainActivity extends Activity {


    ProgressDialog dialog;
    @Bind(R.id.username)
    EditText usernameInput;
    @Bind(R.id.password)
    EditText pwInput;

    AQuery $;

    @OnClick(R.id.btnLogin)
    void login() {
        final String uid = usernameInput.getText().toString();
        final String password = pwInput.getText().toString();
        $.post("/api/v1/login")
                .body($.dict().with("name", uid).with("password", password))
                .param("user", "陈鹏志")
                .header("X-Auth", "ahifadf")
                .start(new Function() {
                    @Handler
                    public void start() {
                        dialog = ProgressDialog.show(MainActivity.this, null, "Login now...");
                    }
                })
                .done(new Function() {
                    @Handler
                    public void done(@ResponseBody Map<String, String> resp) {
                        $.alert("Login success, user token = %s", resp.get("token"));
                    }
                })
                .fail(new Function() {
                    @Handler
                    public void fail(@NetError VolleyError error) {
                        $.alert(error.getMessage());
                    }
                })
                .always(new Function() {
                    @Handler
                    public void always() {
                        dialog.dismiss();
                    }
                })
                .send(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        $ = AQuery.getInstance(this);
        ButterKnife.bind(this);
        usernameInput.setText("chpengzh");
        pwInput.setText("qwerty");
    }
}
