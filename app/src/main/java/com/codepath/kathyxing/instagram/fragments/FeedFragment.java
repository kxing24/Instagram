package com.codepath.kathyxing.instagram.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.codepath.kathyxing.instagram.EndlessRecyclerViewScrollListener;
import com.codepath.kathyxing.instagram.LoginActivity;
import com.codepath.kathyxing.instagram.NDSpinner;
import com.codepath.kathyxing.instagram.Post;
import com.codepath.kathyxing.instagram.PostsAdapter;
import com.codepath.kathyxing.instagram.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class FeedFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String TAG = "FeedFragment";

    private int check = 0;

    private RecyclerView rvPosts;
    private ImageButton ibComposePost;
    private NDSpinner sUserDropdownMenu;
    protected PostsAdapter adapter;
    protected List<Post> allPosts;

    private EndlessRecyclerViewScrollListener scrollListener;

    // Required empty public constructor
    public FeedFragment() {}

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    // This event is triggered soon after onCreateView().
    // onViewCreated() is only called if the view returned from onCreateView() is non-null.
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        rvPosts = view.findViewById(R.id.rvPosts);
        ibComposePost = view.findViewById(R.id.ibComposePost);
        sUserDropdownMenu = view.findViewById(R.id.sUserDropdownMenu);

        // initialize the array that will hold posts and create a PostsAdapter
        allPosts = new ArrayList<>();
        adapter = new PostsAdapter(getContext(), allPosts);

        // set up the spinner
        String[] userMenuOptions = getResources().getStringArray(R.array.user_menu_options);
        ArrayAdapter spinnerAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, userMenuOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sUserDropdownMenu.setAdapter(spinnerAdapter);
        sUserDropdownMenu.setOnItemSelectedListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        // set the adapter on the recycler view
        rvPosts.setAdapter(adapter);
        // set the layout manager on the recycler view
        rvPosts.setLayoutManager(linearLayoutManager);

        // implementation of endless scroll
        scrollListener = new EndlessRecyclerViewScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                loadNextDataFromApi(page);
            }
        };
        // Adds the scroll listener to RecyclerView
        rvPosts.addOnScrollListener(scrollListener);

        // query posts from Instagram
        queryPosts();

        // set up the toolbar
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    public void onDestroyView() {
        // reset check when the fragment is destroyed
        check = 0;
        super.onDestroyView();
    }

    private void queryPosts() {
        // specify what type of data we want to query - Post.class
        ParseQuery<Post> query = ParseQuery.getQuery(Post.class);
        // include data referred by user key
        query.include(Post.KEY_USER);
        // limit query to latest 20 items
        query.setLimit(20);
        // order posts by creation date (newest first)
        query.addDescendingOrder("createdAt");
        // start an asynchronous call for posts
        query.findInBackground(new FindCallback<Post>() {
            @Override
            public void done(List<Post> posts, ParseException e) {
                // check for errors
                if (e != null) {
                    Log.e(TAG, "Issue with getting posts", e);
                    return;
                }

                // save received posts to list and notify adapter of new data
                allPosts.addAll(posts);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void logoutUser() {
        Log.i(TAG, "Logging out");
        ParseUser.logOutInBackground();
        goLoginActivity();
    }

    private void goLoginActivity() {
        Intent i = new Intent(getContext(), LoginActivity.class);
        startActivity(i);
    }

    public void loadNextDataFromApi(int offset) {
        // Send an API request to retrieve appropriate paginated data
        //  --> Send the request including an offset value (i.e `page`) as a query parameter.
        //  --> Deserialize and construct new model objects from the API response
        //  --> Append the new data objects to the existing set of items inside the array of items
        //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i("FeedActivity", "item selected");
        if (++check > 1 && parent.getId() == R.id.sUserDropdownMenu) {
            String valueFromSpinner = parent.getItemAtPosition(position).toString();
            if (valueFromSpinner.equals(getString(R.string.logout))) {
                logoutUser();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.i("FeedActivity", "nothing selected");
    }
}
