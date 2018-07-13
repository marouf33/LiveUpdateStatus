package maroufb.com.liveupdatestatus;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import maroufb.com.liveupdatestatus.model.Status;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mStatusDB;
    private DatabaseReference mUserDB;
    private RecyclerView mRecyclerView;

    FirebaseRecyclerAdapter<Status, StatusViewHolder> firebaseRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser() == null){
            gotoActivity(LoginActivity.class);
        }
        mStatusDB = FirebaseDatabase.getInstance().getReference().child("Status");
        mUserDB = FirebaseDatabase.getInstance().getReference().child("Users");
        mRecyclerView = findViewById(R.id.homeRecyclerView);
        mRecyclerView.setHasFixedSize(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mAuth.getCurrentUser() == null){
            gotoActivity(LoginActivity.class);
        }

        FirebaseRecyclerOptions<Status> options =
                new FirebaseRecyclerOptions.Builder<Status>()
                        .setQuery(mStatusDB, Status.class)
                        .build();



        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Status, StatusViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final StatusViewHolder holder, int position, @NonNull final Status model) {

                mUserDB.child(model.getUserId()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String userName = dataSnapshot.child("displayName").getValue(String.class);
                        String photoURL = dataSnapshot.child("photoUrl").getValue(String.class);

                        holder.setUserName(userName);
                        try {
                            holder.setUserPhotoUrl(photoURL);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                holder.setUserStatus(model.getUserStatus());
                holder.mImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent gotoProfile = new Intent(HomeActivity.this,ProfileActivity.class);
                        gotoProfile.putExtra("USER_ID",model.getUserId());
                        startActivity(gotoProfile);
                    }
                });


            }

            @NonNull
            @Override
            public StatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.status_row, parent, false);
                StatusViewHolder shoppingListViewHolder = new StatusViewHolder(view);
                return shoppingListViewHolder;
            }
        };
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseRecyclerAdapter.stopListening();
    }

    private void gotoActivity(Class<?> targetActivity){
        startActivity(new Intent(HomeActivity.this,targetActivity));
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logoutMenu:
                mAuth.signOut();
                gotoActivity(LoginActivity.class);
                return true;
            case R.id.addNewMenu:
                gotoActivity(PostActivity.class);
                return true;
            case R.id.myProfileMenu:
                Intent gotoProfile = new Intent(HomeActivity.this,ProfileActivity.class);
                gotoProfile.putExtra("USER_ID",mAuth.getCurrentUser().getUid());
                startActivity(gotoProfile);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    public static class StatusViewHolder extends RecyclerView.ViewHolder{

        View mView;

        @BindView(R.id.userImageButton) public ImageButton mImageButton;
        @BindView(R.id.userNameTextView) TextView mUserNameTextView;
        @BindView(R.id.userStatusTextView) TextView mUserStatusTextView;

        public StatusViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            ButterKnife.bind(this,itemView);
        }



        public void setUserPhotoUrl(String imageUrl){
            Picasso.get().load(imageUrl).placeholder(R.mipmap.ic_launcher).into(mImageButton);
        }

        public void setUserName(String name){
            mUserNameTextView.setText(name);
        }

        public void setUserStatus(String status){
            mUserStatusTextView.setText(status);
        }
    }
}
