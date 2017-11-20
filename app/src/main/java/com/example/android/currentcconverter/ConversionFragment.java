package com.example.android.currentcconverter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Toast;
import org.json.JSONObject;
import java.math.RoundingMode;
import java.text.DecimalFormat;


/**
 * Created by ngtrnhan1205 on 10/21/17.
 */

public class ConversionFragment extends Fragment implements OnClickListener{

    TextView inputTextView;
    TextView outputTextView;
    ImageView mainCountryImageView;
    TextView abbrMainTextView;
    ImageView subCountryImageView;
    TextView abbrSubTextView;
    FloatingActionButton switchFab;

    private void setInfo() {
        //Render after choosing a currency
        if (MainActivity.positionArr[0] != -1) {
            CurrentC myCurrency1 = MainActivity.currenciesList.get(MainActivity.positionArr[0]);
            mainCountryImageView.setImageResource(myCurrency1.getFlagResourcesId());
            abbrMainTextView.setText(myCurrency1.getCurrentCAbbreviations());
        }
        if (MainActivity.positionArr[2] != -1) {
            CurrentC myCurrency2 = MainActivity.currenciesList.get(MainActivity.positionArr[2]);
            subCountryImageView.setImageResource(myCurrency2.getFlagResourcesId());
            abbrSubTextView.setText(myCurrency2.getCurrentCAbbreviations());
        }
        new ConversionGetUrl().execute(MainActivity.BASE_URL +
                MainActivity.ENDPOINT + "?access_key=" +MainActivity.ACCESS_KEY);
    }

    private void findView (View view) {
        inputTextView = view.findViewById(R.id.inputTextView);
        outputTextView = view.findViewById(R.id.outputTextView);
        mainCountryImageView = view.findViewById(R.id.mainCoutnryImageView);
        abbrMainTextView =view.findViewById(R.id.abbrMainTextView);
        subCountryImageView = view.findViewById(R.id.subCountryImageView);
        abbrSubTextView = view.findViewById(R.id.abbrSubTextView);
        switchFab = view.findViewById(R.id.switchFab);
    }

    private void setViewOnClickListener() {
        //Set view to OnClickListener
        mainCountryImageView.setOnClickListener(this);
        subCountryImageView.setOnClickListener(this);
        switchFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int tmp = MainActivity.positionArr[0];
                MainActivity.positionArr[0] = MainActivity.positionArr[2];
                MainActivity.positionArr[2] = tmp;
                SharedPreferences sharedPreferences =
                        getActivity().getSharedPreferences("com.example.android.currentcconverter", Context.MODE_PRIVATE);
                sharedPreferences.edit().putInt("main",MainActivity.positionArr[0]).apply();
                sharedPreferences.edit().putInt("sub",MainActivity.positionArr[2]).apply();
                setInfo();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conversion, container, false);
        findView(view);
        setViewOnClickListener();
        setInfo();
        return view;
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(getActivity(), CountrySelection.class);
        if (subCountryImageView == v){
            intent.putExtra("fragment", 2);
        } else intent.putExtra("fragment", 0);
        startActivity(intent);
    }

    private class ConversionGetUrl extends GetUrl{
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {
                if (s != null) {
                    // Find ratio of main and sub to USD
                    String subCurrency = "USD" + abbrSubTextView.getText().toString();
                    String mainCurrency = "USD" + abbrMainTextView.getText().toString();
                    JSONObject jsonObject = new JSONObject(s);
                    JSONObject currency = jsonObject.getJSONObject("quotes");
                    // Calculate ratio sub to main and set to View
                    double answer = currency.getDouble(subCurrency) / currency.getDouble(mainCurrency);
                    answer *= Double.parseDouble(inputTextView.getText().toString());
                    DecimalFormat decimalFormat = new DecimalFormat("#.##");
                    decimalFormat.setRoundingMode(RoundingMode.FLOOR);
                    String finalAnswer = decimalFormat.format(answer);
                    outputTextView.setText(finalAnswer);
                } else {
                    Toast.makeText(getContext(), MainActivity.ERROR_MESSAGE, Toast.LENGTH_LONG);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
