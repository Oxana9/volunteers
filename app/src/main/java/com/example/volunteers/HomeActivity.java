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
    private String userId;

    private ProgressDialog loader;

    private String modelUserId = "";
    private String modelStatus = "";
    private String modelKey = "";
    private String modelTask;
    private String modelDescription;
    private long modelCategoryIdx;
    private long modelPlaceIdx;

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
        userId = mUser.getEmail();
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

            Model model = new Model(userId, mTask, mDescription, id, date, categoryIdx, placeIdx, "Создано");
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
                holder.setHeader(model.getTask());
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
                        modelKey = getRef(pos).getKey();
                        modelTask = model.getTask();
                        modelDescription = model.getDescription();
                        modelCategoryIdx = model.getCategory();
                        modelPlaceIdx = model.getPlace();
                        modelUserId = model.getUserId();
                        modelStatus = model.getStatus();

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
            mView.findViewById(R.id.taskLayout).setVisibility(View.GONE);
            mView.findViewById(R.id.description).setEnabled(false);
            mView.findViewById(R.id.spinnerCategory).setVisibility(View.GONE);
            mView.findViewById(R.id.spinnerPlace).setVisibility(View.GONE);
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

        public void setHeader(String header) {
            TextView dateTextView = mView.findViewById(R.id.header);
            dateTextView.setText(header);
        }
    }

    private void updateTask(){
        //if (this.modelStatus.equals("Завершено")) return;
        boolean sameUser = this.modelUserId.equals(this.userId);
        boolean statusCreated = this.modelStatus.equals("Создано");
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.input_file, null);
        ((TextView)view.findViewById(R.id.header)).setText("Изменить запись");
        myDialog.setView(view);

        AlertDialog dialog = myDialog.create();

        EditText mTask = view.findViewById(R.id.task);
        EditText mDescription = view.findViewById(R.id.description);

        mTask.setText(modelTask);
        mDescription.setText(modelDescription);

        Spinner spinnerCategory = view.findViewById(R.id.spinnerCategory);
        Spinner spinnerPlace = view.findViewById(R.id.spinnerPlace);

        spinnerCategory.setSelection((int) modelCategoryIdx);
        spinnerPlace.setSelection((int) modelPlaceIdx);

        ImageButton delButton = view.findViewById(R.id.deleteBtn);
        Button updateButton = view.findViewById(R.id.saveBtn);
        Button cancelButton = view.findViewById(R.id.cancelBtn);

        if (sameUser) {
            delButton.setVisibility(View.VISIBLE);
            mTask.setSelection(modelTask.length());
            mDescription.setSelection(modelDescription.length());
            if (statusCreated) {
                updateButton.setText("Изменить");
            }
            else {
                updateButton.setText("Завершить");
            }
        }
        else {
            mTask.setEnabled(false);
            mDescription.setEnabled(false);
            spinnerCategory.setEnabled(false);
            spinnerPlace.setEnabled(false);
            if (statusCreated) {
                updateButton.setText("Помочь");
            }
            else {
                if (this.modelStatus.equals(this.userId)) {
                    updateButton.setText("Отозвать");
                }
                else updateButton.setVisibility(View.GONE);
            }
        }


        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean sameUser = modelUserId.equals(userId);
                boolean statusCreated = modelStatus.equals("Создано");
                modelTask = mTask.getText().toString().trim();
                modelDescription = mDescription.getText().toString().trim();

                String date = DateFormat.getDateInstance().format(new Date());
                long categoryIdx = spinnerCategory.getSelectedItemId();
                long placeIdx = spinnerPlace.getSelectedItemId();
                if (sameUser && !statusCreated) {
                    modelStatus = "Завершено";
                    removeItem(dialog);
                    return;
                }
                if (!sameUser && modelStatus.equals(userId)) {
                    modelStatus = "Создано";
                } else
                if (!sameUser && statusCreated) {
                    modelStatus = userId;
                }

                Model model = new Model(modelUserId, modelTask, modelDescription, modelKey, date, categoryIdx, placeIdx, modelStatus);

                reference.child(modelKey).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                removeItem(dialog);
            }
        });

        cancelButton.setOnClickListener((v) -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void removeItem(AlertDialog dialog) {
        reference.child(modelKey).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    Toast.makeText(HomeActivity.this, "Запрос удален", Toast.LENGTH_SHORT).show();
                } else {
                    String err = task.getException().toString();
                    Toast.makeText(HomeActivity.this, "Ошибка при удалении запроса", Toast.LENGTH_SHORT).show();
                }
            }
        });

        dialog.dismiss();
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