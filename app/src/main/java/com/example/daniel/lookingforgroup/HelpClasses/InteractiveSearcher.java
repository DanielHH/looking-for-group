package com.example.daniel.lookingforgroup.HelpClasses;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.daniel.lookingforgroup.AsyncResponse;
import com.example.daniel.lookingforgroup.GetData;
import com.example.daniel.lookingforgroup.R;
import com.example.daniel.lookingforgroup.UserPageActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class InteractiveSearcher extends LinearLayout implements AsyncResponse {

    private String baseUrl;
    private Context ctx;
    private EditText searchBar;
    private PopUpList popUpList;
    private PopupWindow popupWindow;
    private int requestId;
    final ArrayList<String> emails = new ArrayList<>();
    private RequestQueue queue;

    public InteractiveSearcher(Context context) {
        super(context);
        this.ctx = context;
        init();
    }

    public InteractiveSearcher(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.ctx = context;
        init();
    }

    private void init() {

        baseUrl = getResources().getString(R.string.url);

        setOrientation(VERTICAL);
        queue = Volley.newRequestQueue(ctx);
        searchBar = new EditText(ctx);

        popUpList = new PopUpList(ctx);
        popUpList.setParent(this);

        ScrollView scrollView = new ScrollView(ctx);
        scrollView.addView(popUpList);

        popupWindow = new PopupWindow(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setContentView(scrollView);
        popupWindow.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

        this.addView(searchBar);

        textWatcher();
    }

    public void showPopupList() {
        popupWindow.showAsDropDown(searchBar);
    }

    private void textWatcher() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                getMatchingNames(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    public void getMatchingNames(String name) {
        popUpList.clearNames();
        requestId++;

        final String url = baseUrl + "getusers/" + requestId + '/' + name;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("id") == requestId) {
                                JSONArray users = response.getJSONArray("users");
                                for (int i = 0; i < users.length(); i++) {
                                    emails.add(users.get(i).toString());
                                }
                                popUpList.setNames(emails);
                                showPopupList();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("dee: ", error.toString());
                    }
                });
        queue.add(jsonObjectRequest);
    }


    public void goToUserPage(String userEmail) {
        GetData getData = new GetData();
        getData.delegate = this;
        String url = baseUrl + "getidwithemail/" + userEmail;
        try {//execute the async task
            getData.execute(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void processFinish(String response) {
        //Handle the response.
        System.out.println(response);
        JSONObject userData;
        String userId;
        try {
            userData = new JSONObject(response);
            userId = userData.getString("id");
            Intent intent = new Intent(ctx, UserPageActivity.class);
            intent.putExtra("EXTRA_USER_ID", userId);
            ctx.startActivity(intent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}