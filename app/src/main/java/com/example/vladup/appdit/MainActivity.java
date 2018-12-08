package com.example.vladup.appdit;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import com.apollographql.apollo.ApolloClient;
import okhttp3.OkHttpClient;
import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CallbackItemTouch {

    private static final String TAG = "MainActivity";
    public RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private MyAdapter monAdapter;
    public List<Model> ListModel;
    FloatingActionButton buttonAdd;
    Boolean isModifiable;
    MenuItem itemValide;
    MenuItem itemCancel;
    Menu myMenu;
    DBHandler myDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonAdd = (FloatingActionButton) findViewById(R.id.add_task);

        myDB = new DBHandler(this);
        View inflatedView = getLayoutInflater().inflate(R.layout.item_list, null);
        CheckBox favoris = (CheckBox) inflatedView.findViewById(R.id.favorisItem);
        favoris.setVisibility(View.INVISIBLE);

        isModifiable = true;
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
        mLayoutManager = new LinearLayoutManager(this);

        ListModel = new ArrayList<>();

        monAdapter = new MyAdapter(MainActivity.this, ListModel);


        ItemTouchHelper.Callback callback = new ItemMoveCallback(this);// create MyItemTouchHelperCallback
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback); // Create ItemTouchHelper and pass with parameter the MyItemTouchHelperCallback
        touchHelper.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(monAdapter);

        getAllPosts();

        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isModifiable){
                    Toast.makeText(MainActivity.this, "You need to confirm action.", Toast.LENGTH_LONG).show();
                }
                else {
                    Intent intent = new Intent(MainActivity.this, CreateActivity.class);
                    intent.putExtra("NEWPOS", ListModel.size());
                    startActivityForResult(intent, 1);
                }
            }
        });

        monAdapter.setOnItemClickListener(new MyAdapter.ClickListener() {
            @Override
            public void onClick(MyAdapter.MyViewHolder holder, int position) {
                if (!isModifiable){
                    Toast.makeText(MainActivity.this, "You need to confirm action.", Toast.LENGTH_LONG).show();
                }
                else{
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra("MAPOSITION", holder.maPosition);
                    intent.putExtra(Constantes.ID_KEY, holder.IdDBElement);
                    intent.putExtra(Constantes.TITLE_KEY, holder.mTextTitle.getText().toString());
                    intent.putExtra(Constantes.CONTENT_KEY, holder.mTextContent.getText().toString());
                    intent.putExtra(Constantes.DATE_KEY, holder.mTextDate.getText().toString());
                    intent.putExtra(Constantes.COLOR_KEY, holder.colorBackground);
                    intent.putExtra(Constantes.FAVORIS_KEY, holder.mCheckboxFavoris.isChecked());
                    startActivityForResult(intent, 2);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        itemValide = menu.findItem(R.id.validateList);
        itemCancel = menu.findItem(R.id.cancelList);

        itemValide.setVisible(false);
        itemCancel.setVisible(false);

        this.myMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sort_by_done:
                Collections.sort(ListModel, new Comparator<Model>() {
                    @Override
                    public int compare(Model o1, Model o2) {
                        return Boolean.compare(o2.isSelected(),o1.isSelected());
                    }
                });
                updatePos();
                monAdapter.notifyDataSetChanged();
                break;
            case R.id.validateList:
                isModifiable = true;
                item.setVisible(false);
                itemCancel.setVisible(false);
                updatePos();
                monAdapter.notifyDataSetChanged();
                break;
            case R.id.cancelList:
                isModifiable = true;
                item.setVisible(false);
                itemValide.setVisible(false);
                ListModel.clear();
                getAllPosts();
                monAdapter.notifyDataSetChanged();
            default:
                return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAllPosts(){
        //final List<Model> ListModel2 = new ArrayList<>();
        MyApolloClient.getMyApolloClient().query(
                GetAllPostTQuery.builder().build()).enqueue(new ApolloCall.Callback<GetAllPostTQuery.Data>() {
            @Override
            public void onResponse(@NotNull Response<GetAllPostTQuery.Data> response) {
                Model[] array = new Model[response.data().posts().size()];
                for (int i = 0; i < response.data().posts().size(); i++){
                    Model model = new Model();
                    model.setId(response.data().posts().get(i).id().toString());
                    model.setTitle(response.data().posts().get(i).title().toString());
                    model.setContent(response.data().posts().get(i).content().toString());
                    model.setDate(response.data().posts().get(i).date());
                    model.setColor(response.data().posts().get(i).color());
                    model.setSelected(response.data().posts().get(i).favoris());
                    model.setPosition(response.data().posts().get(i).position());

                    array[response.data().posts().get(i).position()]=model;
                    //ListModel2.add(response.data().posts().get(i).position().intValue(), model);
                }
                final List<Model> petiteList = Arrays.asList(array);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // ListModel.addAll(ListModel2);
                         ListModel.addAll(petiteList);
                        synchronized (monAdapter){
                            monAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }

            @Override
            public void onFailure(@NotNull ApolloException e) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == 1){
                ListModel.add(new Model(data.getStringExtra(Constantes.ID_KEY), data.getStringExtra(Constantes.TITLE_KEY), data.getStringExtra(Constantes.CONTENT_KEY), data.getStringExtra(Constantes.DATE_KEY), data.getStringExtra(Constantes.COLOR_KEY), false, data.getExtras().getInt("NEWPOSRETOUR")));
                monAdapter.notifyDataSetChanged();
            }
            else if (requestCode == 2) {
                ListModel.set(data.getExtras().getInt("MAPOSITIONRETOUR"), new Model(data.getStringExtra(Constantes.ID_KEY), data.getStringExtra(Constantes.TITLE_KEY), data.getStringExtra(Constantes.CONTENT_KEY), data.getStringExtra(Constantes.DATE_KEY), data.getStringExtra(Constantes.COLOR_KEY), data.getExtras().getBoolean(Constantes.FAVORIS_KEY), data.getExtras().getInt("MAPOSITIONRETOUR")));
                monAdapter.notifyItemChanged(data.getExtras().getInt("MAPOSITIONRETOUR"));
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case 1 :
                if (!isModifiable) {
                    Toast.makeText(MainActivity.this, "You need to confirm action.", Toast.LENGTH_LONG).show();
                }
                else {
                    MyApolloClient.getMyApolloClient().mutate(
                            DeletePostTMutation.builder()
                                    .id(monAdapter.getIDitemRemove(item.getGroupId())).build())
                            .enqueue(new ApolloCall.Callback<DeletePostTMutation.Data>() {
                                @Override
                                public void onResponse(@NotNull Response<DeletePostTMutation.Data> response) {
                                    myDB.deleteNotif(response.data().deletePost().id);
                                }

                                @Override
                                public void onFailure(@NotNull ApolloException e) {
                                }
                            });
                    monAdapter.remove(item.getGroupId());
                    updatePos();
                }
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void itemTouchOnMove(int oldPosition, int newPosition) {
        ListModel.add(newPosition,ListModel.remove(oldPosition));// change position
        ListModel.get(oldPosition).setPosition(newPosition);
        monAdapter.notifyItemMoved(oldPosition, newPosition);

        itemValide = myMenu.findItem(R.id.validateList);
        itemCancel = myMenu.findItem(R.id.cancelList);
        isModifiable = false;

        itemCancel.setVisible(true);
        itemValide.setVisible(true);
    }

    @Override
    public void itemRemoveOnSwipe(int position) {
        monAdapter.remove(position);
    }

    public void updatePos(){
        for (int i = 0; i < ListModel.size(); i++){
            ListModel.get(i).setPosition(i);
        }
        for (int i = 0; i < ListModel.size(); i++){
            MyApolloClient.getMyApolloClient().mutate(
                    UpdatePostTMutation.builder()
                            .id(ListModel.get(i).getId())
                            .position(ListModel.get(i).getPosition()).build())
                    .enqueue(new ApolloCall.Callback<UpdatePostTMutation.Data>() {
                        @Override
                        public void onResponse(@NotNull Response<UpdatePostTMutation.Data> response) {
                        }

                        @Override
                        public void onFailure(@NotNull ApolloException e) {
                        }
                    });
        }
    }
}
