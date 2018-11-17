package com.example.vladup.appdit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import org.jetbrains.annotations.NotNull;

public class DetailActivity extends AppCompatActivity {

    private String id_element;
    private int position;
    private boolean isCheckedD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        final EditText titleDetail = (EditText)findViewById(R.id.titleDetail);
        final EditText contentDetail = (EditText)findViewById(R.id.contentDetail);
        Button editButton = (Button) findViewById(R.id.editButton);

        position = getIntent().getExtras().getInt("MAPOSITION");
        id_element = getIntent().getStringExtra(Constantes.ID_KEY);
        titleDetail.setText(getIntent().getStringExtra(Constantes.TITLE_KEY));
        contentDetail.setText(getIntent().getStringExtra(Constantes.CONTENT_KEY));
        isCheckedD = getIntent().getExtras().getBoolean(Constantes.FAVORIS_KEY);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApolloClient.getMyApolloClient().mutate(
                        UpdatePostTMutation.builder()
                        .id(id_element)
                        .title(titleDetail.getText().toString())
                        .content(contentDetail.getText().toString())
                        .date("")
                        .color("").build())
                        .enqueue(new ApolloCall.Callback<UpdatePostTMutation.Data>() {
                            @Override
                            public void onResponse(@NotNull Response<UpdatePostTMutation.Data> response) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.putExtra("MAPOSITIONRETOUR", position);
                                intent.putExtra(Constantes.ID_KEY, id_element);
                                intent.putExtra(Constantes.TITLE_KEY, titleDetail.getText().toString());
                                intent.putExtra(Constantes.CONTENT_KEY, contentDetail.getText().toString());
                                intent.putExtra(Constantes.FAVORIS_KEY, isCheckedD);
                                //Log.e("SAMER", "POSITION : "+position);
                                setResult(RESULT_OK, intent);
                                finish();
                            }

                            @Override
                            public void onFailure(@NotNull ApolloException e) {

                            }
                        });
            }
        });
    }
}
