package com.avinabaray.userinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    private static final int PICKFILE_REQUEST_CODE = 124;
    private RecyclerView usersRecycler;
    private ExtendedFloatingActionButton addUserFab, importFab;
    private MaterialButton cancelButton, doneButton;

    private TextInputLayout firstNameText, lastNameText, emailText, profilesText, usernameText, passwordText;
    private TextView addUserText;
    private AutoCompleteTextView profilesAutoText;

    private LinearLayout bottomSheet;
    private View rootLayout;
    private Activity mActivity;
    private ArrayList<UserModel> users;

    private BottomSheetBehavior bottomSheetBehavior;
    private UsersAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        mActivity = this;
        users = new ArrayList<>();
        setContentView(R.layout.activity_main);

        initViews();
        initBottomSheet();
        initListFromSharedPref();
        initRecycler();

        importFab.setOnClickListener(v -> {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mActivity);
            alertDialogBuilder.setTitle("Import from JSON File")
                    .setMessage("Select a JSON File to import user data")
                    .setCancelable(true)
                    .setIcon(R.drawable.ic_import_file)
                    .setPositiveButton("OK", (dialog, which) -> initImportFromJSON())
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        });
    }

    private void initListFromSharedPref() {
        SharedPreferences pref = mActivity.getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        String jsonData = pref.getString("json_data", "[]");
        users.addAll(new Gson().fromJson(jsonData, new TypeToken<ArrayList<UserModel>>() {
        }.getType()));
    }

    private void initViews() {
        rootLayout = findViewById(R.id.rootLayout);
        usersRecycler = findViewById(R.id.usersRecycler);
        addUserFab = findViewById(R.id.addUserFab);
        importFab = findViewById(R.id.importFab);
        doneButton = findViewById(R.id.doneButton);
        cancelButton = findViewById(R.id.cancelButton);
        bottomSheet = findViewById(R.id.bottomSheet);

        addUserText = findViewById(R.id.addUserText);
        firstNameText = findViewById(R.id.firstNameText);
        lastNameText = findViewById(R.id.lastNameText);
        emailText = findViewById(R.id.emailText);
        profilesText = findViewById(R.id.profilesText);
        profilesAutoText = findViewById(R.id.profilesAutoText);
        usernameText = findViewById(R.id.usernameText);
        passwordText = findViewById(R.id.passwordText);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
    }

    private void initImportFromJSON() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/json");
        mActivity.startActivityForResult(Intent.createChooser(intent, "Import from JSON File"), PICKFILE_REQUEST_CODE);
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

    }

    private void initRecycler() {
        int numberOfColumns = 2;
        GridLayoutManager glm = new GridLayoutManager(mActivity, numberOfColumns);

        usersRecycler.setLayoutManager(glm);
        usersAdapter = new UsersAdapter(mActivity, users, new UsersAdapter.OnUserDataChangeListener() {
            @Override
            public void onEdit(int pos) {
                addUser(true, pos, users.get(pos));
            }

            @Override
            public void onDelete(int pos) {
                users.remove(pos);
                usersAdapter.notifyDataSetChanged();
                updateSharedPref();
            }
        });

        usersRecycler.setAdapter(usersAdapter);

        addUserFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addUser(false, -1, null);
            }
        });

    }

    private void initBottomSheet() {
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                mActivity,
                android.R.layout.simple_dropdown_item_1line,
                mActivity.getResources().getStringArray(R.array.profiles));
        profilesAutoText.setAdapter(adapter);

        cancelButton.setOnClickListener(v -> {
            firstNameText.getEditText().setText("");
            lastNameText.getEditText().setText("");
            emailText.getEditText().setText("");
            profilesAutoText.setText("");

            profilesAutoText.clearListSelection();
            profilesAutoText.clearFocus();

            usernameText.getEditText().setText("");
            passwordText.getEditText().setText("");

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });


    }

    private void addUser(boolean editMode, int pos, @Nullable UserModel oldUser) {
        if (editMode) {
            addUserText.setText("Edit User");
            if (oldUser == null) {
                throw new IllegalStateException("oldUser object can't be null in editMode");
            }

            firstNameText.getEditText().setText(oldUser.getFirstName());
            lastNameText.getEditText().setText(oldUser.getLastName());
            emailText.getEditText().setText(oldUser.getEmail());

            // TODO see edit selection
            profilesText.getEditText().setText(oldUser.getProfiles());

            usernameText.getEditText().setText(oldUser.getUsername());
            passwordText.getEditText().setText(oldUser.getPassword());

            doneButton.setOnClickListener(v -> {
                String fName = normalizeString(firstNameText.getEditText().getText().toString());
                String lName = normalizeString(lastNameText.getEditText().getText().toString());
                String email = emailText.getEditText().getText().toString();
                String profile = profilesText.getEditText().getText().toString();
                String username = usernameText.getEditText().getText().toString();
                String password = passwordText.getEditText().getText().toString();

                if (fName.length() > 0 && lName.length() > 0) {
                    UserModel um = new UserModel(fName, lName, profile, username, password, email);
                    users.set(pos, um);
                    usersAdapter.notifyDataSetChanged();
                    updateSharedPref();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    Toast.makeText(mActivity, "Fill up your name correctly", Toast.LENGTH_SHORT).show();
                }

            });
        } else {
            addUserText.setText("Add New User");
            doneButton.setOnClickListener(v -> {
                String fName = normalizeString(firstNameText.getEditText().getText().toString());
                String lName = normalizeString(lastNameText.getEditText().getText().toString());
                String email = emailText.getEditText().getText().toString();
                String profile = profilesText.getEditText().getText().toString();
                String username = usernameText.getEditText().getText().toString();
                String password = passwordText.getEditText().getText().toString();

                if (fName.length() > 0 && lName.length() > 0) {
                    UserModel um = new UserModel(fName, lName, profile, username, password, email);
                    users.add(um);
                    usersAdapter.notifyDataSetChanged();
                    updateSharedPref();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                } else {
                    Toast.makeText(mActivity, "Fill up your name correctly", Toast.LENGTH_SHORT).show();
                }

            });
        }
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

    }

    private void updateSharedPref() {
        SharedPreferences pref = mActivity.getApplicationContext().getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString("json_data", new Gson().toJson(users));

        editor.apply();
    }

    /**
     * Capitalizes first letter of each word and rest small letters
     */
    private String normalizeString(String str) {
        char[] ch = str.toCharArray();
        for (int i = 0; i < str.length(); i++) {

            if (i == 0 && ch[i] != ' ' || ch[i] != ' ' && ch[i - 1] == ' ') {
                if (ch[i] >= 'a' && ch[i] <= 'z') {
                    ch[i] = (char) (ch[i] - 'a' + 'A');
                }
            } else if (ch[i] >= 'A' && ch[i] <= 'Z')
                ch[i] = (char) (ch[i] + 'a' - 'A');
        }
        return new String(ch);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICKFILE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                InputStream is = mActivity.getContentResolver().openInputStream(data.getData());
                StringBuilder sb = new StringBuilder();
                for (int ch; (ch = is.read()) != -1; ) {
                    sb.append((char) ch);
                }
                String jsonStringFromFile = sb.toString();

//                Log.e("File", sw);

                users.addAll(new Gson().fromJson(jsonStringFromFile, new TypeToken<ArrayList<UserModel>>() {
                }.getType()));
                usersAdapter.notifyDataSetChanged();
                updateSharedPref();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}