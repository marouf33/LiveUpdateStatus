package maroufb.com.liveupdatestatus;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.emailEditTextLogin)    EditText mEmailEditText;

    @BindView(R.id.passwordEditTextLogin) EditText mPasswordEditText;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_main);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.loginButton)
    public void setLoginButton(){
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            showAlertDialog("Error", "email cannot be empty");
        }else if(TextUtils.isEmpty(password)){
            showAlertDialog("Error","password cannot be empty");
        }else{
            loginViaFirebase(email,password);
        }
    }

    private void showAlertDialog(String title, String message){
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        dialog.show();
    }

    private void loginViaFirebase(String email, String password){
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    showAlertDialog("Error!",task.getException().getMessage());
                }else{
                    startActivity(new Intent(LoginActivity.this,HomeActivity.class));
                    finish();

                }
            }
        });
    }

    private void gotoRegister(){
        startActivity(new Intent(LoginActivity.this,RegisterationActivity.class));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.login_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.createAccountMenu:
                mAuth.signOut();
                gotoRegister();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
