package maroufb.com.liveupdatestatus;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import maroufb.com.liveupdatestatus.model.User;

public class RegisterationActivity extends AppCompatActivity {

    @BindView(R.id.nameEditTextRegiter)
    EditText mNameEditText;

    @BindView(R.id.emailEditTextRegiter)
    EditText mEmailEditText;

    @BindView(R.id.passwordEditTextRegiter)
    EditText mPasswordEditText;

    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
    }

    @OnClick(R.id.registerButton)
    public void setRegisterButton(){
        String name = mNameEditText.getText().toString().trim();
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        if(TextUtils.isEmpty(name)){
            showAlertDialog("Error", "Name cannot be empty.");

        }else if(TextUtils.isEmpty(email)){
            showAlertDialog("Error","email cannot be empty");
        }else if(TextUtils.isEmpty(password)){
            showAlertDialog("Error","password cannot be empty");
        }else{
            registerUserToFirebase(email,password,name);
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

    private void registerUserToFirebase(String email, String password, final String name){
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(!task.isSuccessful()){
                    //error registering user
                    showAlertDialog("Error",task.getException().getMessage());
                }else{
                    //success
                    final FirebaseUser currentUser = task.getResult().getUser();

                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                    currentUser.updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            User newUser = new User(currentUser.getDisplayName(),currentUser.getEmail(),"",currentUser.getUid());
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");

                            reference.child(currentUser.getUid()).setValue(newUser);
                            startActivity(new Intent(RegisterationActivity.this,HomeActivity.class));
                            finish();
                        }
                    });

                }
            }
        });
    }
}
