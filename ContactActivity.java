package com.example.chattingdemo.contacts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.chattingdemo.AppController;
import com.example.chattingdemo.ChatRoomMessage;
import com.example.chattingdemo.R;
import com.example.chattingdemo.RequestCache;
import com.example.chattingdemo.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.chattingdemo.contacts.PaginationListenerBottom.PAGE_START;

public class ContactActivity extends AppCompatActivity {

    private static final String TAG = "ContactActivity";

    private ArrayList<ContactHandler> contactsList = new ArrayList<>();

    private ProgressBar progressbar;
    private LinearLayout noMessage;

    private BottomSheetDialog mBottomDialogNotificationAction;

    private ContactsAdapter adapter;

    private int currentPage = PAGE_START;
    private boolean isLastPage = false;
    private int totalPage = 2000;
    private int perPageContact = 20;
    private boolean isLoading = false;

    private int LoadTotal = 0;
    private int LoadStart = 0;
    private boolean flagLoading = false;
    private static final int LoadEnd = 20;
    private String selectedOptionMessageId;
    private String selectedOptionCustomerId;

    int callTime = 1;
    boolean timerRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        init();
    }

    private void init() {

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        setTitle("Messages");

        noMessage = findViewById(R.id.EmptyMessage);
        RecyclerView rcContacts = findViewById(R.id.rcContacts);
        progressbar = findViewById(R.id.progressBar);

        progressbar.getIndeterminateDrawable().setColorFilter(this.getResources().getColor(R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rcContacts.setHasFixedSize(true);
        rcContacts.setLayoutManager(layoutManager);

        adapter = new ContactsAdapter(ContactActivity.this, new ArrayList<ContactHandler>(),
                new ContactsAdapter.ContactsListener() {
                    @Override
                    public void onClick(int position) {
                        ContactHandler contact = contactsList.get(position);

                        Intent launchIntent = new Intent(ContactActivity.this,
                                ChatRoomMessage.class);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        launchIntent.putExtra("business_name", contact.getName());
                        launchIntent.putExtra("message_id", contact.getBucketId());
                        launchIntent.putExtra("message_state", "LOAD");
                        getApplicationContext().startActivity(launchIntent);

                    }

                    @Override
                    public void onClickContact(int position) {
                        ContactHandler contact = contactsList.get(position);
                        selectedOptionMessageId = contact.getBucketId();
                        selectedOptionCustomerId = contact.getSellerId();
                        ShowContactOptions();
                    }
                }
        );

        rcContacts.setAdapter(adapter);


        rcContacts.addOnScrollListener(new PaginationListenerBottom(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage++;
                Log.e("response", callTime++ + "");

                Timer t = new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        timerRunning = false;
                    }
                }, 3000);

                if (!timerRunning) {
                    timerRunning = true;
                    loadContacts(LoadStart, LoadEnd);
                }
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        loadContacts(LoadStart, LoadEnd);
    }

    public void loadContacts(int start, int end) {

        RequestCache stringLoad = new RequestCache(Request.Method.GET, Utils.API_FETCH_CONTACTS
                + "?customer_key=SLApO64gAktgmuLl&start=" + start + "&end=" + end,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressbar.setVisibility(View.GONE);

                        try {
                            Log.e("response", response);
                            JSONObject res = new JSONObject(response);
                            if (res.getInt("status") == 200) {
                                LoadTotal = res.getInt("count");
                                LoadStart = (LoadStart + LoadTotal);
                                Log.e(TAG, "LoadTotal = " + LoadTotal + "New LoadStart = " + LoadStart + " New LoadEnd = " + LoadEnd);
                                JSONArray thread = res.getJSONArray("messages");

                                List<ContactHandler> list = new ArrayList<>();
                                if (thread.length() > 0) {
                                    for (int i = 0; i < thread.length(); i++) {
                                        JSONObject obj = thread.getJSONObject(i);
                                        ContactHandler cObject = new ContactHandler(
                                                obj.getString("name"),
                                                obj.getString("photo"),
                                                obj.getString("message"),
                                                obj.getInt("online_status"),
                                                obj.getInt("read_status"),
                                                obj.getString("row_id"),
                                                obj.getString("bucket_id"),
                                                obj.getString("seller_key")
                                        );
                                        list.add(cObject);
                                        contactsList.add(cObject);
                                    }
                                    if (currentPage != PAGE_START)
                                        adapter.removeLoading();

                                    adapter.addItems(list);

                                    if (LoadTotal < perPageContact) {
                                        isLastPage = true;
                                    }else {
                                        adapter.addLoading();

                                        Environment.getExternalStorageDirectory()
                                    }
                                        /*// check weather is last page or not
                                        if (currentPage < totalPage) {
                                            adapter.addLoading();
                                        } else {
                                            isLastPage = true;
                                        }*/
                                    isLoading = false;
                                } else {
                                    Log.e("response", "list zero");
                                    noData();
                                }
                            } else {
                                noData();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            noData();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressbar.setVisibility(View.GONE);
                        noData();
                    }
                });
        AppController.getInstance(this).addToRequestQueue(stringLoad);
    }

    private void noData() {
        isLoading = false;
        if (contactsList.size() < 1)
            noMessage.setVisibility(View.VISIBLE);
    }

//    public void loadContacts(int start, int end, final Contacts.dataFinishChanging myCallback) {
//
//        RequestCache stringLoad = new RequestCache(Request.Method.GET, Utils.API_FETCH_CONTACTS
//                + "?customer_key=SLApO64gAktgmuLl&start=" + start + "&end=" + end,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//
//                        progressbar.setVisibility(View.GONE);
//
//                        try {
//                            Log.e("response", response);
//                            JSONObject res = new JSONObject(response);
//                            if (res.getInt("status") == 200) {
//                                LoadTotal = res.getInt("count");
//                                LoadStart = (LoadStart + LoadTotal);
//                                Log.e(TAG, "LoadTotal = " + LoadTotal + "New LoadStart = " + LoadStart + " New LoadEnd = " + LoadEnd);
//                                JSONArray thread = res.getJSONArray("messages");
//                                if (thread.length() > 0) {
//                                    for (int i = 0; i < thread.length(); i++) {
//                                        JSONObject obj = thread.getJSONObject(i);
//                                        ContactHandler cObject = new ContactHandler(
//                                                obj.getString("name"),
//                                                obj.getString("photo"),
//                                                obj.getString("message"),
//                                                obj.getInt("online_status"),
//                                                obj.getInt("read_status"),
//                                                obj.getString("row_id"),
//                                                obj.getString("bucket_id"),
//                                                obj.getString("seller_key")
//                                        );
//                                        contactsList.add(cObject);
//                                    }
//                                    //Collections.reverse(contactsList);
//                                    myCallback.onCallback(contactsList);
//                                } else {
//                                    myCallback.onEmpty(null);
//                                }
//                            } else {
//                                myCallback.onEmpty(null);
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                            myCallback.onEmpty(null);
//                        }
//                    }
//                },
//                new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        progressbar.setVisibility(View.GONE);
//                    }
//                });
//        AppController.getInstance(this).addToRequestQueue(stringLoad);
//    }

    public void ShowContactOptions() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View ac = AlertSheetDialog(ContactActivity.this);
                if (ac != null) {
                    ac.findViewById(R.id.contactBlock).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mBottomDialogNotificationAction.cancel();
                            if (selectedOptionMessageId != null && selectedOptionCustomerId != null) {
                                /**
                                 * @ selectedOptionMessageId Message Id
                                 * @ selectedOptionCustomerId Message customer Id
                                 */
                            }
                        }
                    });

                    ac.findViewById(R.id.contactArchive).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mBottomDialogNotificationAction.cancel();
                            if (selectedOptionMessageId != null && selectedOptionCustomerId != null) {
                                /**
                                 * @ selectedOptionMessageId Message Id
                                 * @ selectedOptionCustomerId Message customer Id
                                 */
                            }
                        }
                    });

                    ac.findViewById(R.id.contactOptionClose).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mBottomDialogNotificationAction.cancel();
                            selectedOptionMessageId = null;
                            selectedOptionCustomerId = null;
                        }
                    });
                }
            }
        });
    }

    private View AlertSheetDialog(Activity mActivity) {
        try {
            View sheetView = mActivity.getLayoutInflater().inflate(R.layout.contact_options, null);
            mBottomDialogNotificationAction = new BottomSheetDialog(mActivity);
            mBottomDialogNotificationAction.setContentView(sheetView);
            mBottomDialogNotificationAction.setCancelable(true);
            mBottomDialogNotificationAction.show();
            FrameLayout bottomSheet = mBottomDialogNotificationAction.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackground(null);
            }
            return sheetView;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.e(TAG, String.valueOf(item.getItemId()));
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
