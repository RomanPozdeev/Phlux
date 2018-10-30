package example.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import base.ServerAPI;
import example.demo.DemoActivity;
import info.android15.phlux.example.R;
import phlux.Scope;
import phlux.Transient;
import phlux.base.PhluxActivity;

public class MainActivity extends PhluxActivity<MainState> {

    private static final int REQUEST_ID = 1;

    CheckedTextView check1;
    CheckedTextView check2;
    ProgressBar progressBar;
    ArrayAdapter<ServerAPI.Item> adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.thread_demo).setOnClickListener(v ->
                startActivity(new Intent(this, DemoActivity.class)));

        check1 = findViewById(R.id.check1);
        check2 = findViewById(R.id.check2);

        check1.setText(MainState.NAME_1);
        check2.setText(MainState.NAME_2);

        check1.setOnClickListener(v -> switchTo(MainState.NAME_1));
        check2.setOnClickListener(v -> switchTo(MainState.NAME_2));

        ListView listView = findViewById(R.id.listView);
        listView.setAdapter(adapter = new ArrayAdapter<>(this, R.layout.item));

        progressBar = findViewById(R.id.progress_bar);
    }

    @Override
    public void onScopeCreated(Scope<MainState> scope) {
        super.onScopeCreated(scope);
        Log.d("MainActivity", "onScopeCreated");
        scope.background(REQUEST_ID, Request.create(MainState.DEFAULT_NAME));
    }

    @Override
    public MainState initial() {
        return MainState.initial();
    }

    @Override
    public void update(MainState state) {
        part("tabs", state.name(), name -> {
            check1.setChecked(name.equals(MainState.NAME_1));
            check2.setChecked(name.equals(MainState.NAME_2));
        });

        part("items", state.items(), items -> {
            adapter.clear();
            if (!items.isRestored() && items.get() != null) {
                adapter.addAll(items.get());
            } else {
                if (items.isRestored()) {
                    //IMPORTANT process restored
                    restoreState(state);
                }
            }
        });

        part("loading", state.loading(), loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        });

        part("error", state.error(), error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                post(this::removeError);
            }
        });
    }

    private void restoreState(MainState state) {
        scope().apply(oldState -> oldState.toBuilder()
                .items(Transient.empty())
                .build());
        scope().background(REQUEST_ID, Request.create(state.name()));
    }

    public void post(Runnable runnable) {
        getWindow()
                .getDecorView()
                .post(runnable);
    }

    private void switchTo(String name) {
        scope().apply(state -> state.toBuilder()
                .name(name)
                .build());
        scope().background(REQUEST_ID, Request.create(name));
    }

    private void removeError() {
        scope().apply(s -> s.toBuilder()
                .error(null)
                .build());
    }
}
