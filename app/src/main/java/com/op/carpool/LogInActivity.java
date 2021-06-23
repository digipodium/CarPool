package com.op.carpool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class LogInActivity extends AppCompatActivity {

    private final String TAG = "HALOOOOO";
    EditText userEdit;
    EditText passEdit;
    String userNameStr = "";
    String passwordStr = "";
    private FirebaseAuth mAuth;
    private DatabaseHandler db;
    private DocumentReference mUsersDocRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = new DatabaseHandler();

        userEdit = findViewById(R.id.usernameEdit);
        passEdit = findViewById(R.id.passwordEdit);

        FirebaseAuth.AuthStateListener als = firebaseAuth -> {
            if (mAuth.getCurrentUser() != null) {
                checkProfileCreated(this);
            }
        };
        mAuth.addAuthStateListener(als);
    }

    public void checkProfileCreated(final Context context) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mUsersDocRef = FirebaseFirestore.getInstance().collection("users").document(uid);
        if (uid.equals("")) {
            return;
        }
        mUsersDocRef = FirebaseFirestore.getInstance().collection("users").document(uid);
        final Context varContext = context;
        mUsersDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        // Creating User object based on document and getting bool
                        boolean created = doc.toObject(User.class).getProfileCreated();
                        if (created) {
                            FirebaseHelper.loggedIn = true;
                            MainActivity.setImageSettings(context);
                            Intent intent = new Intent(varContext, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            varContext.startActivity(intent);
                        } else {
                            FirebaseHelper.loggedIn = false;
                            Intent intent = new Intent(varContext, EditProfileActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            varContext.startActivity(intent);
                        }
                        finish();
                    }
                }
            }
        });
    }


    //Login button press
    public void login(View V) {
        String email = userEdit.getText().toString();
        String password = passEdit.getText().toString();

        if (email.equals("")) {
            noEntry(userEdit);
        } else if (password.equals("")) {
            noEntry(passEdit);
        } else {
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                onBackPressed();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LogInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    //SignUp button press
    public void signUp(View v) {
        Intent signUpIntent = new Intent(this, SignUp.class);
        startActivity(signUpIntent);
    }

    //execute if username or password is empty
    public void noEntry(EditText et) {
        et.setText("");
        et.setHintTextColor(Color.parseColor("#B75252"));
        if (et.getId() == R.id.usernameEdit) userEdit.setHint("Username*");
        else passEdit.setHint("Password*");
    }

    //Exit app with pressing back putton on your phone
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        return;
    }

}
