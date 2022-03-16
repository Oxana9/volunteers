package com.example.volunteers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String onlineUserID;

    private ProgressDialog loader;

    private String currentUser = "";
    private String key = "";
    private String task;
    private String description;
    private long categoryIdx;
    private long placeIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        toolbar = findViewById(R.id.homeToolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Волонтёры");

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loader = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        onlineUserID = mAuth.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("tasks");//.child(onlineUserID);

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
            }
        });

    }

    private void addTask() {
//        String[] categories = { "Укажите категорию", "Общее", "Мебель и мягкий инвентарь", "Бытовая техника", "Дети", "Продукты" };
//        String[] places = { "Укажите пункт выдачи", "Зиповская, 31", "Красноармейская, 53", "Кирова 186" };
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.input_file, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        Spinner spinnerCategories = myView.findViewById(R.id.spinnerCategory);
//        ArrayAdapter<String> adapterCategories = new ArrayAdapter(this, android.R.layout.simple_spinner_item, categories);
//        adapterCategories.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerCategories.setAdapter(adapterCategories);
//
        Spinner spinnerPlaces = myView.findViewById(R.id.spinnerPlace);
//        ArrayAdapter<String> adapterPlaces = new ArrayAdapter(this, android.R.layout.simple_spinner_item, places);
//        adapterPlaces.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spinnerPlaces.setAdapter(adapterPlaces);

        final EditText task = myView.findViewById(R.id.task);
        final EditText description = myView.findViewById(R.id.description);
        Button save = myView.findViewById(R.id.saveBtn);
        Button cancel = myView.findViewById(R.id.cancelBtn);

        cancel.setOnClickListener((v) -> {
            dialog.dismiss();
        });

        save.setOnClickListener((v) -> {
            String mTask = task.getText().toString().trim();
            String mDescription = description.getText().toString().trim();
            String id = reference.push().getKey();
            String date = DateFormat.getDateInstance().format(new Date());
            long categoryIdx = spinnerCategories.getSelectedItemId();
            long placeIdx = spinnerPlaces.getSelectedItemId();

        if (TextUtils.isEmpty(mTask)) {
            task.setError("Введите запрос");
            return;
        }
        if (TextUtils.isEmpty(mDescription)) {
            task.setError("Введите описание");
            return;
        } else {
            loader.setMessage("Данные добавляются");
            loader.setCanceledOnTouchOutside(false);
            loader.show();

            Model model = new Model(onlineUserID, mTask, mDescription, id, date, categoryIdx, placeIdx);
            reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(HomeActivity.this, "Запрос добавлен", Toast.LENGTH_SHORT).show();
                        loader.dismiss();
                    } else {
                        String error = task.getException().toString();
                        Toast.makeText(HomeActivity.this, "Ошибка в добавлении запроса" + error, Toast.LENGTH_SHORT).show();
                        loader.dismiss();
                    }
                }
            });
        }
        dialog.dismiss();
    });

        dialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Model> options = new FirebaseRecyclerOptions.Builder<Model>()
                .setQuery(reference, Model.class)
                .build();

        FirebaseRecyclerAdapter<Model, MyViewHolder> adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull Model model) {
                holder.setDate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDesc(model.getDescription());
                holder.setCategory(model.getCategory());
                holder.setPlace(model.getPlace());
                
                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (getItemCount() < position) {
                            Log.i("pos", String.valueOf(position));
                            Log.i("items", String.valueOf(getItemCount()));
                            return;
                        }
                        int pos = position;
                        if (getItemCount() == pos) {
                            pos -= 1;
                        }
                        key = getRef(pos).getKey();
                        task = model.getTask();
                        description = model.getDescription();
                        categoryIdx = model.getCategory();
                        placeIdx = model.getPlace();
                        currentUser = model.getUserId();

                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.input_file, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
            mView.findViewById(R.id.saveBtn).setVisibility(View.GONE);
            mView.findViewById(R.id.cancelBtn).setVisibility(View.GONE);
            mView.findViewById(R.id.task).setEnabled(false);
            mView.findViewById(R.id.description).setEnabled(false);
            mView.findViewById(R.id.spinnerCategory).setEnabled(false);
            mView.findViewById(R.id.spinnerPlace).setEnabled(false);
        }
        public void setTask(String task) {
            TextView taskTextView = mView.findViewById(R.id.task);
            taskTextView.setText(task);
        }

        public void setDesc(String desc) {
            TextView descTextView = mView.findViewById(R.id.description);
            descTextView.setText(desc);
        }

        public void setCategory(long categoryIdx) {
            Spinner category = mView.findViewById(R.id.spinnerCategory);
            category.setSelection((int) categoryIdx);
        }

        public void setPlace(long placeIdx) {
            Spinner place = mView.findViewById(R.id.spinnerPlace);
            place.setSelection((int) placeIdx);
        }

        public void setDate(String date) {
            TextView dateTextView = mView.findViewById(R.id.header);
            dateTextView.setText(date);
        }
    }

    private void updateTask(){
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.input_file, null);
        ((TextView)view.findViewById(R.id.header)).setText("Изменить запись");
        view.findViewById(R.id.deleteBtn).setVisibility((currentUser==onlineUserID)?View.VISIBLE:View.GONE);
        myDialog.setView(view);

        AlertDialog dialog = myDialog.create();

        EditText mTask = view.findViewById(R.id.task);
        EditText mDescription = view.findViewById(R.id.description);

        mTask.setText(task);
        mTask.setSelection(task.length());

        mDescription.setText(description);
        mDescription.setSelection(description.length());

        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Spinner spinnerPlace = view.findViewById(R.id.spinnerPlace);

        spinnerCategory.setSelection((int) categoryIdx);
        spinnerPlace.setSelection((int) placeIdx);

        ImageButton delButton = view.findViewById(R.id.deleteBtn);
        Button updateButton = view.findViewById(R.id.saveBtn);
        Button cancelButton = view.findViewById(R.id.cancelBtn);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task = mTask.getText().toString().trim();
                description = mDescription.getText().toString().trim();

                String date = DateFormat.getDateInstance().format(new Date());
                long categoryIdx = spinnerCategory.getSelectedItemId();
                long placeIdx = spinnerPlace.getSelectedItemId();

                Model model = new Model(onlineUserID, task, description, key, date, categoryIdx, placeIdx);

                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Данные успешно изменены", Toast.LENGTH_SHORT).show();
                        } else {
                            String err = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Изменений не произошло" + err, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();
            }
        });

        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Запрос удалено", Toast.LENGTH_SHORT).show();
                        } else {
                            String err = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Ошибка при удалении запроса", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                dialog.dismiss();
            }
        });

        cancelButton.setOnClickListener((v) -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                mAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}