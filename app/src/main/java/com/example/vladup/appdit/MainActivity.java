package com.example.vladup.appdit;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.apollographql.apollo.ApolloClient;
import okhttp3.OkHttpClient;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyAdapter monAdapter;
    public List<Model> ListModel;
    FloatingActionButton buttonAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonAdd = (FloatingActionButton) findViewById(R.id.add_task);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mLayoutManager = new LinearLayoutManager(this);

        ListModel = new ArrayList<>();

        monAdapter = new MyAdapter(MainActivity.this, ListModel);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(monAdapter);

        getAllPosts();

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        monAdapter.setOnItemClickListener(new MyAdapter.ClickListener() {
            @Override
            public void onClick(MyAdapter.MyViewHolder holder, int position) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("MAPOSITION", position);
                intent.putExtra(Constantes.ID_KEY, holder.IdDBElement);
                intent.putExtra(Constantes.TITLE_KEY, holder.mTextTitle.getText().toString());
                intent.putExtra(Constantes.CONTENT_KEY, holder.mTextContent.getText().toString());
                intent.putExtra(Constantes.FAVORIS_KEY, holder.mCheckboxFavoris.isChecked());
                startActivityForResult(intent, 2);
            }
        });
    }

    private void getAllPosts(){
        final List<Model> ListModel2 = new ArrayList<>();
        MyApolloClient.getMyApolloClient().query(
                GetAllPostTQuery.builder().build()).enqueue(new ApolloCall.Callback<GetAllPostTQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetAllPostTQuery.Data> response) {
                for (int i = 0; i < response.data().posts().size(); i++){
                    Model model = new Model();
                    model.setId(response.data().posts().get(i).id().toString());
                    model.setTitle(response.data().posts().get(i).title().toString());
                    model.setContent(response.data().posts().get(i).content().toString());
                    model.setSelected(response.data().posts().get(i).favoris());
                    ListModel2.add(model);
                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListModel.addAll(ListModel2);

                        synchronized (monAdapter){
                            monAdapter.notifyDataSetChanged();
                        }

                    }
                });
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
                Log.e("Putain de sarace", "MARCHE PAS");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == 1){
                ListModel.add(new Model(data.getStringExtra(Constantes.ID_KEY), data.getStringExtra(Constantes.TITLE_KEY), data.getStringExtra(Constantes.CONTENT_KEY), "", "", false));
                //monAdapter.notifyItemInserted();
            }
            else if (requestCode == 2) {
                ListModel.set(data.getExtras().getInt("MAPOSITIONRETOUR"), new Model(data.getStringExtra(Constantes.ID_KEY), data.getStringExtra(Constantes.TITLE_KEY), data.getStringExtra(Constantes.CONTENT_KEY), "", "", data.getExtras().getBoolean(Constantes.FAVORIS_KEY)));
                monAdapter.notifyItemChanged(data.getExtras().getInt("MAPOSITIONRETOUR"));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 1 :
                MyApolloClient.getMyApolloClient().mutate(
                        DeletePostTMutation.builder()
                                .id(monAdapter.getIDitemRemove(item.getGroupId())).build())
                        .enqueue(new ApolloCall.Callback<DeletePostTMutation.Data>() {
                            @Override
                            public void onResponse(@NotNull Response<DeletePostTMutation.Data> response) {
                            }

                            @Override
                            public void onFailure(@NotNull ApolloException e) {

                            }
                        });
                //myDB.deleteContact(Integer.valueOf(monAdapter.getIDitemRemove(item.getGroupId())).toString());
                monAdapter.remove(item.getGroupId());
        }
        return super.onContextItemSelected(item);
    }

}
