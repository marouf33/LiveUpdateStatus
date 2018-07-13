package maroufb.com.liveupdatestatus;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import maroufb.com.liveupdatestatus.model.Status;

public class PostActivity extends AppCompatActivity {

    @BindView(R.id.postEditText) EditText postEditText;

    private DatabaseReference mStatusDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        ButterKnife.bind(this);

        mStatusDB = FirebaseDatabase.getInstance().getReference().child("Status");
    }

    @OnClick(R.id.postButton)
    public void setPostEditText(){
        String status = postEditText.getText().toString();

        if(TextUtils.isEmpty(status)){
            //error
        }else{
            //proceed
            postStatusToFirebase(status, FirebaseAuth.getInstance().getCurrentUser().getUid());
        }
    }


    private void postStatusToFirebase(String userStatus, String userId){
        Status status = new Status(userStatus,userId);
        mStatusDB.push().setValue(status).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                postEditText.setText(null);
                Toast.makeText(PostActivity.this,"Success",Toast.LENGTH_LONG).show();
            }
        });
    }
}
