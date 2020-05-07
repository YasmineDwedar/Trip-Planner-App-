package com.vaatu.tripmate.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.vaatu.tripmate.R;
import com.vaatu.tripmate.ui.home.UpcomingTripsActivity;

import static android.content.ContentValues.TAG;

//import com.vaatu.tripmate.R;

public class SignUp extends Fragment {

    public static final String username = "username";

    EditText emailField;
    EditText passField;
    EditText passField2;
    EditText nameField;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    Button btnBack;
    Button btnSignUp;

    TextView mStatusTextView;
    ProgressBar mProgressBar;


    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.up_sign, container, false);

        btnBack = view.findViewById(R.id.btnBack);
        btnSignUp = view.findViewById(R.id.btnSignUp);
        mProgressBar = view.findViewById(R.id.determinateBar);

        emailField = view.findViewById(R.id.emailField);
        passField = view.findViewById(R.id.passField);
        passField2 = view.findViewById(R.id.passField2);
        nameField = view.findViewById(R.id.nameField);


        mStatusTextView = view.findViewById(R.id.textViewStatus);

        Bundle b = this.getArguments();

        emailField.setText(b.getString("Email"));
        passField.setText(b.getString("Pass"));

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameField.getText().toString().equals("")) {
                    mStatusTextView.setText("Please enter your name");
                    hideProgressBar();

                } else {
                    if (passField.getText().toString().equals(passField2.getText().toString())) {
                        createUser(emailField.getText().toString(), passField.getText().toString());
                    } else {
                        mStatusTextView.setText("Password doesn't match");
                        hideProgressBar();

                    }
                }

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_signup_login);
            }
        });

        return view;
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    void createUser(String email, String password) {
        showProgressBar();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            hideProgressBar();

                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(getActivity(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        hideProgressBar();
        if (user != null) {

            Intent mainIntent = new Intent(getContext(), UpcomingTripsActivity.class);
            mainIntent.putExtra("username", nameField.getText().toString());
            startActivity(mainIntent);
            getActivity().finish();

        } else {
            mStatusTextView.setText("Invalid Credentials");
        }

    }

    private void hideProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);

    }
    private void showProgressBar(){
        mProgressBar.setVisibility(View.INVISIBLE);

    }
}
