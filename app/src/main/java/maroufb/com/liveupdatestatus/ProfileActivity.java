package maroufb.com.liveupdatestatus;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileActivity extends AppCompatActivity {

    @BindView(R.id.userImageViewProfile) ImageView mUserImageView;
    @BindView(R.id.userNameEditText)  EditText mUserNameEditText;
    @BindView(R.id.updateProfileButton) Button mUpdateButton;

    private DatabaseReference mUserProfileReference;
    private FirebaseUser currentUser;
    private StorageReference mStorage;
    private Uri mPhotoURI;

    private String passUserID;

    private static final int REQUEST_PHOTO_CAPTURE = 1;
    private static final int REQUEST_PHOTO_PICK = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);

        passUserID = getIntent().getStringExtra("USER_ID");

        mStorage = FirebaseStorage.getInstance().getReference();
        mUserProfileReference = FirebaseDatabase.getInstance().getReference().child("Users").child(passUserID);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mUserProfileReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("displayName").getValue(String.class);
                String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);

                mUserNameEditText.setText(name);
                if(!TextUtils.isEmpty(photoUrl))
                    Picasso.get().load(photoUrl).placeholder(R.mipmap.ic_launcher).into(mUserImageView);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(passUserID.equals(currentUser.getUid())){
            mUserImageView.setEnabled(true);
            mUserNameEditText.setFocusable(true);
            mUpdateButton.setVisibility(View.VISIBLE);
        }else {
            mUserImageView.setEnabled(false);
            mUserNameEditText.setFocusable(false);
            mUpdateButton.setVisibility(View.GONE);
        }

        mUserImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                builder.setMessage("How would you like to add your photo?");
                builder.setPositiveButton("Take photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                            if (ActivityCompat.shouldShowRequestPermissionRationale(getOuter(),
                                    Manifest.permission.CAMERA)) {
                                showExplanation("Permission Needed", "We nned permission to your camera so you can take a new profile picture."
                                        , Manifest.permission.CAMERA,REQUEST_PHOTO_CAPTURE);
                            } else {
                                // No explanation needed; request the permission
                                ActivityCompat.requestPermissions(getOuter(),
                                        new String[]{Manifest.permission.CAMERA},
                                        REQUEST_PHOTO_CAPTURE);
                            }
                        }else
                            dispatchTakePhotoIntent();


                    }
                });
                builder.setNegativeButton("Choose photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dispatchChoosePhotoIntent();
                    }
                });
                builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();
            }
        });



    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PHOTO_CAPTURE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ProfileActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                    dispatchTakePhotoIntent();
                } else {
                    Toast.makeText(ProfileActivity.this, "Permission Denied!", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showExplanation(String title,
                                 String message,
                                 final String permission,
                                 final int permissionRequestCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPermission(permission, permissionRequestCode);
                    }
                });
        builder.create().show();
    }

    private void requestPermission(String permissionName, int permissionRequestCode) {
        ActivityCompat.requestPermissions(this,
                new String[]{permissionName}, permissionRequestCode);
    }

    public ProfileActivity getOuter(){
        return this;
    }

    @OnClick(R.id.updateProfileButton)
    public void setUpdateButton(){
        final String userName = mUserNameEditText.getText().toString().trim();
        //update user name
        if(!TextUtils.isEmpty(userName)) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(userName)
                    .build();
            currentUser.updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Map<String,Object> updateUserNameMap = new HashMap<>();
                    updateUserNameMap.put("displayName",userName);
                    mUserProfileReference.updateChildren(updateUserNameMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getApplicationContext(),"Success updating name.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        }
        //update user photo
        updateUserPhoto(mPhotoURI);



    }

    private void updateUserPhoto(Uri photoUrl){
      if(photoUrl != null) {
          final StorageReference userImageRef = mStorage.child("userImages").child(currentUser.getUid())
                  .child(photoUrl.getLastPathSegment());
          userImageRef.putFile(photoUrl).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
              @Override
              public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                  if (!task.isSuccessful()) {
                      AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                      builder.setTitle("Error")
                              .setMessage(task.getException().getMessage());
                      builder.create().show();
                      return null;
                  }

                  // Continue with the task to get the download URL
                  return userImageRef.getDownloadUrl();
              }
          }).addOnCompleteListener(new OnCompleteListener<Uri>() {
              @Override
              public void onComplete(@NonNull Task<Uri> task) {


                  if (task.isSuccessful()) {
                      Uri downloadUri = task.getResult();
                      Map<String, Object> updatePhotoMap = new HashMap<>();
                      updatePhotoMap.put("photoUrl", downloadUri.toString());
                      mUserProfileReference.updateChildren(updatePhotoMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                          @Override
                          public void onSuccess(Void aVoid) {
                              Toast.makeText(getApplicationContext(),"Success updating photo.", Toast.LENGTH_SHORT).show();
                          }
                      });
                  } else {
                      AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                      builder.setTitle("Error")
                              .setMessage(task.getException().getMessage());
                      builder.create().show();
                  }
              }
          });

      }

    }

    private void dispatchTakePhotoIntent(){
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePhotoIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(takePhotoIntent,REQUEST_PHOTO_CAPTURE);
        }
    }

    private void dispatchChoosePhotoIntent(){
        Intent choosePhoto = new Intent(Intent.ACTION_PICK);
        choosePhoto.setType("image/*");
        startActivityForResult(choosePhoto,REQUEST_PHOTO_PICK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case REQUEST_PHOTO_CAPTURE:
                    mPhotoURI = data.getData();
                    mUserImageView.setImageURI(mPhotoURI);
                    return;
                case REQUEST_PHOTO_PICK:
                    mPhotoURI = data.getData();
                    mUserImageView.setImageURI(mPhotoURI);
                    return;
            }
        }


    }
}
