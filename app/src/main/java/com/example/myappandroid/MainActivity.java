package com.example.myappandroid;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;
import com.scalified.fab.ActionButton;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

public class MainActivity extends AppCompatActivity {
    private CustomAdapter mAdapter;
    private List<Project> projects;
    public List<Todo> todos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        android.support.v7.widget.Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ActionButton fab = this.findViewById(R.id.action_button);
        fab.setButtonColor(getResources().getColor(R.color.pinkSearch));
        fab.setButtonColorPressed(getResources().getColor(R.color.pinkSearchDark));
        fab.setImageResource(R.drawable.fab_plus_icon);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTodoActivity.class);
                startActivity(intent);
            }
        });
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getString(R.string.default_font))
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Ion.with(this)
                .load(getString(R.string.projectsRequest))
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {

                    @Override
                    public void onCompleted(Exception e, JsonArray result) {
                        if (result != null) {
                            projects = new ArrayList<>();
                            for (final JsonElement projectJsonElement : result) {
                                projects.add(new Gson().fromJson(projectJsonElement, Project.class));
                            }
                            Ion.with(MainActivity.this)
                                    .load(getString(R.string.todosRequest))
                                    .asJsonArray()
                                    .setCallback(new FutureCallback<JsonArray>() {

                                        @Override
                                        public void onCompleted(Exception e, JsonArray result) {
                                            todos = new ArrayList<>();
                                            for (final JsonElement todoJsonElement : result) {
                                                todos.add(new Gson().fromJson(todoJsonElement, Todo.class));
                                            }
                                            ListView listView = MainActivity.this.findViewById(R.id.list_view);
                                            mAdapter = new CustomAdapter(MainActivity.this, todos);
                                            for (int i = 0; i < projects.size(); i++) {
                                                Project currentProject = projects.get(i);
                                                mAdapter.addSectionDataItem(currentProject.title);
                                                for (int j = 0; j < todos.size(); j++) {
                                                    Todo currentTodo = todos.get(j);
                                                    if (currentTodo.project_id == currentProject.id) {
                                                        mAdapter.addItem(currentTodo.text);
                                                    }
                                                }
                                            }
                                            listView.setAdapter(mAdapter);
                                        }
                                    });
                        }
                    }
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}