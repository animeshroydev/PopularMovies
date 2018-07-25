package developer.roy.animesh.popularmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.github.ybq.android.spinkit.style.Circle;
import com.github.ybq.android.spinkit.style.Wave;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import developer.roy.animesh.popularmovies.adapters.MoviesAdapter;
import developer.roy.animesh.popularmovies.adapters.interfaces.ItemClickListener;
import developer.roy.animesh.popularmovies.constants.Constants;
import developer.roy.animesh.popularmovies.database.AppDatabase;
import developer.roy.animesh.popularmovies.models.Movie;
import developer.roy.animesh.popularmovies.models.MovieDbResponse;
import developer.roy.animesh.popularmovies.networking.ApiClient;
import developer.roy.animesh.popularmovies.networking.ApiInterface;
import developer.roy.animesh.popularmovies.utils.ConnectivityUtils;
import developer.roy.animesh.popularmovies.viewmodels.MainViewModel;
import retrofit2.Call;
import retrofit2.Callback;

public class MainActivity extends AppCompatActivity implements ItemClickListener, MoviesAdapter.ItemClickListener {


     // Used butterknife library initialization of views


    @BindView(R.id.tv_selected_filter)
    TextView tvSelectedFilter;
    @BindView(R.id.btn_filter)
    Button btnFilter;
    @BindView(R.id.radio_filter_group)
    RadioGroup radioFilterGroup;
    @BindView(R.id.btn_popular)
    RadioButton btnPopular;
    @BindView(R.id.btn_top_rated)
    RadioButton btnTopRated;

    @BindView(R.id.btn_favorites)
    RadioButton btnFavourites;

    @BindView(R.id.pbHeaderProgress)
    ProgressBar progressBar;




    @BindView(R.id.my_recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.rl_internet_failure)
    RelativeLayout rlInternetFailure;
    @BindView(R.id.btn_retry)
    Button btnRetry;
    @BindView(R.id.bottom_sheet_filter)
    View bottomSheetFilter;




    private int mFilterId = Constants.POPULAR_MOVIES_ID;

    private BottomSheetBehavior mBottomSheetBehavior;


    private View mDimBackground;
    boolean mIsFilterMenuOpen = false;

    private ApiInterface apiInterface;

    private MoviesAdapter adapter;

    private List<Movie> movies;
    private AppDatabase mDb;

    MainViewModel viewModel;

    private int currentItemPosition = 0;

    private static final String SAVED_FILTER_ID = "filterId";
    private static final String SAVED_VISIBLE_POSITION = "position";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDb = AppDatabase.getInstance(getApplicationContext());

      ButterKnife.bind(this);
        apiInterface = ApiClient.getRetrofitClient().create(ApiInterface.class);
        Circle circle = new Circle();
        mRecyclerView.setLayoutManager(new GridLayoutManager(this,2));


      progressBar.setIndeterminateDrawable(circle);
      progressBar.setVisibility(View.VISIBLE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
        }

        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        mRecyclerView.setHasFixedSize(true);
        movies = new ArrayList<>();
        adapter = new MoviesAdapter(movies);
        GridLayoutManager mGridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setAdapter(adapter);
        adapter.setOnItemClickListener(this);




        /*
        * Initializing bottom sheet and hiding it on first loading(with or without internet)
        */
        mBottomSheetBehavior = BottomSheetBehavior.from(bottomSheetFilter);
        mBottomSheetBehavior.setHideable(true);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        setupListeners();

        if (ConnectivityUtils.isConnectedToInternet(this)) {



            loadMovies();
        } else {



            progressBar.setVisibility(View.GONE);
            rlInternetFailure.setVisibility(View.VISIBLE);
            btnRetry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ConnectivityUtils.isConnectedToInternet(MainActivity.this)){
                        loadMovies();

                        rlInternetFailure.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
            });
        }
          /*
        * restore state on orientation change
        */
        updateUIFromSavedState(savedInstanceState);
    }

    private void updateUIFromSavedState(Bundle savedInstanceState) {
        if (savedInstanceState != null){

            mFilterId = savedInstanceState.getInt(SAVED_FILTER_ID, Constants.POPULAR_MOVIES_ID);
            currentItemPosition = savedInstanceState.getInt(SAVED_VISIBLE_POSITION, 0);
            if (mFilterId == Constants.TOP_RATED_MOVIES_ID){
                tvSelectedFilter.setText(getString(R.string.text_top_rated_movies_label));
                btnTopRated.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip));
                btnPopular.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip_unselected));
            } else if (mFilterId == Constants.FAVORITES_MOVIES_ID){
                tvSelectedFilter.setText(getString(R.string.text_favorites_movies_label));
                btnFavourites.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip));
                btnPopular.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip_unselected));
            }
        }
    }

    private void setupListeners() {

        btnFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ConnectivityUtils.isConnectedToInternet(MainActivity.this)) {
                    if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

                    } else {
                        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                }
            }
        });


        radioFilterGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                if (ConnectivityUtils.isConnectedToInternet(MainActivity.this)) {
                    switch (checkedId) {
                        case R.id.btn_popular:
                            tvSelectedFilter.setText(getString(R.string.text_popular_movies));

                            mFilterId = Constants.POPULAR_MOVIES_ID;
                            if (ConnectivityUtils.isConnectedToInternet(MainActivity.this)) {
                                loadMovies();
                            } else {

                            }
                            btnPopular.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip));
                            btnFavourites.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip_unselected));
                            btnTopRated.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip_unselected));
                            break;
                        case R.id.btn_top_rated:
                            tvSelectedFilter.setText(getString(R.string.text_top_rated_movies));
                            if (ConnectivityUtils.isConnectedToInternet(MainActivity.this)) {
                                loadMovies();
                            } else {

                            }
                            mFilterId = Constants.TOP_RATED_MOVIES_ID;
                            btnTopRated.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip));
                            btnFavourites.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip_unselected));
                            btnPopular.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip_unselected));
                            break;
                        case R.id.btn_favorites:
                            tvSelectedFilter.setText(getString(R.string.text_fav_movies));
                            mFilterId = Constants.FAVORITES_MOVIES_ID;

                            progressBar.setVisibility(View.GONE);
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                            btnTopRated.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip_unselected));
                            btnFavourites.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip));
                            btnPopular.setBackground(ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_chip_unselected));

                            break;
                    }
                    loadMovies();
                } else {
                    Toast.makeText(MainActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMovies(){

        if (mFilterId == Constants.FAVORITES_MOVIES_ID) {

            viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
                @Override
                public void onChanged(@Nullable List<Movie> movies) {
                    MainActivity.this.movies = movies;
                    adapter.setMovies(MainActivity.this.movies);
                    adapter.notifyDataSetChanged();
                    mRecyclerView.scrollToPosition(currentItemPosition);
                    currentItemPosition = 0;
                }
            });
        } else {

            Call<MovieDbResponse> responseCall;
            if (mFilterId == Constants.POPULAR_MOVIES_ID) {
                responseCall = apiInterface.getPopularMovies(Constants.API_KEY);
            } else {
                responseCall = apiInterface.getTopRatedMovies(Constants.API_KEY);
            }
            responseCall.enqueue(new Callback<MovieDbResponse>() {
                @Override
                public void onResponse(@Nullable Call<MovieDbResponse> call, @Nullable retrofit2.Response<MovieDbResponse> response) {
                    try {
                        movies = response.body().getMovies();
                        adapter.setMovies(movies);
                        runAnimation(mRecyclerView,2);
                        progressBar.setVisibility(View.GONE);
                        adapter.notifyDataSetChanged();
                        mRecyclerView.scrollToPosition(currentItemPosition);
                        currentItemPosition = 0;
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(@Nullable Call<MovieDbResponse> call, @Nullable Throwable t) {
                }
            });
        }
    }


    private void runAnimation(RecyclerView recyclerView, int type) {
        Context context = recyclerView.getContext();
        LayoutAnimationController controller = null;


        if (type == 2) //slide from right
            controller = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_slide_from_right);

        //set anim
        recyclerView.setLayoutAnimation(controller);
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
    }



    /*
  * Called when a movie poster is clicked
  */
    @Override
    public void onItemClicked(Class adapterClass, int position) {


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_FILTER_ID, mFilterId);
        currentItemPosition = ((GridLayoutManager)mRecyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        outState.putInt(SAVED_VISIBLE_POSITION, currentItemPosition);
    }

    @Override
    public void onItemClicked(int position) {
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        Intent movieDetailsIntent = new Intent(this, MovieDetailsActivity.class);
        movieDetailsIntent.putExtra(Constants.PASSING_MOVIE, movies.get(position));
        startActivity(movieDetailsIntent);
    }
}
