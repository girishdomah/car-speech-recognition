package com.example.carassistant;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NewContactActivity extends AppCompatActivity {
    private EditText editTextName;
    private EditText editTextPhone;

    private static final String TAG = "NewContactActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_contact);

        editTextName = findViewById(R.id.edit_text_name);
        editTextPhone = findViewById(R.id.edit_text_phone);
    }

    public void saveContact(View view) {
        String name = editTextName.getText().toString();
        String phone = editTextPhone.getText().toString();

        if (name.trim().isEmpty()) {
            Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        if (phone.trim().isEmpty()) {
            Toast.makeText(this, "Enter a phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference contactsRef = FirebaseFirestore.getInstance()
                .collection("Contacts");
        contactsRef.add(new Contact(name, phone));
        Toast.makeText(this, "New contact saved", Toast.LENGTH_SHORT).show();
        finish();
    }

    public void cancelContact(View view) {
        finish();
    }
}