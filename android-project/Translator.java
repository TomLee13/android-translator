package edu.cmu.mingyan2.project4android;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This class is the main activity of the Android app required for Project4 Task1 and Task2.
 * It is responsible for the UI content of the app.
 * It is very similar to the InterestingPicture Class in the Android lab.
 */
public class Translator extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("onCreate hit");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        // Add the name of the name to the tool bar
        toolbar.setTitle("Fr. & Es. Translator");

        final Translator t = this;

        Button translateButton1 = (Button)findViewById(R.id.translateToFrench);
        // Add a listener to the translate to French button
        translateButton1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewParam) {
                String text = ((EditText)findViewById(R.id.text)).getText().toString().toLowerCase();
                String language = "french";
                // send message to the GetTranslation class to interact with the web-service
                GetTranslation gt = new GetTranslation();
                gt.translate(text, language, t); // Done asynchronously in another thread.
            }
        });

        Button translateButton2 = (Button)findViewById(R.id.translateToSpanish);
        // Add a listener to the translate to Spanish button
        translateButton2.setOnClickListener(new View.OnClickListener(){
            public void onClick(View viewParam) {
                String text = ((EditText)findViewById(R.id.text)).getText().toString().toLowerCase();
                String language = "spanish";
                // send message to the GetTranslation class to interact with the web-service
                GetTranslation gt = new GetTranslation();
                gt.translate(text, language, t); // Done asynchronously in another thread.
            }
        });


    }

    public void translationReady(String translation) {
        System.out.println("translation ready");
        TextView text = (TextView)findViewById(R.id.text);
        TextView translationText = (TextView)findViewById(R.id.translation);
        if (translation.equals("N")) {
            translationText.setText("No translation for " + text.getText().toString());
        } else {
            translationText.setText("The translation for " + text.getText().toString() + " is " + translation);
        }
    }


}
