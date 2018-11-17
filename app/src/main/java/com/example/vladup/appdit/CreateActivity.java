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

public class CreateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        final EditText titleCreate = (EditText)findViewById(R.id.titleCreate);
        final EditText contentCreate = (EditText)findViewById(R.id.contentCreate);
        Button createButton = (Button) findViewById(R.id.createButton);

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyApolloClient.getMyApolloClient().mutate(
                        CreatePostTMutation.builder()
                                .title(titleCreate.getText().toString())
                                .content(contentCreate.getText().toString())
                                .date("")
                                .color("")
                                .favoris(false).build())
                        .enqueue(new ApolloCall.Callback<CreatePostTMutation.Data>() {
                            @Override
                            public void onResponse(@NotNull Response<CreatePostTMutation.Data> response) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);

                                intent.putExtra(Constantes.ID_KEY, response.data().createDraft().id);
                                intent.putExtra(Constantes.TITLE_KEY, titleCreate.getText().toString());
                                intent.putExtra(Constantes.CONTENT_KEY, contentCreate.getText().toString());
                                Log.e("SAMER", "POSITION : "+ response.data().createDraft().id);
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
