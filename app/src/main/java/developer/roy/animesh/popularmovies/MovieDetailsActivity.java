package developer.roy.animesh.popularmovies;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialimageloading.MaterialImageLoading;
import com.like.LikeButton;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import developer.roy.animesh.popularmovies.adapters.MovieVideoAdapter;
import developer.roy.animesh.popularmovies.adapters.MoviewReviewAdapter;
import developer.roy.animesh.popularmovies.adapters.interfaces.ItemClickListener;
import developer.roy.animesh.popularmovies.constants.Constants;
import developer.roy.animesh.popularmovies.database.AppDatabase;
import developer.roy.animesh.popularmovies.models.Movie;
import developer.roy.animesh.popularmovies.models.MovieReviewResponse;
import developer.roy.animesh.popularmovies.models.MovieVideo;
import developer.roy.animesh.popularmovies.models.MovieVideoResponse;
import developer.roy.animesh.popularmovies.models.Review;
import developer.roy.animesh.popularmovies.networking.ApiClient;
import developer.roy.animesh.popularmovies.networking.ApiInterface;
import developer.roy.animesh.popularmovies.utils.AppExecutors;
import developer.roy.animesh.popularmovies.utils.CommonUtils;
import developer.roy.animesh.popularmovies.viewmodels.AddToFavViewModel;
import developer.roy.animesh.popularmovies.viewmodels.AddToFavViewModelFactory;
import retrofit2.Call;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class MovieDetailsActivity extends AppCompatActivity implements ItemClickListener {


    final static int DURATION = 10000;

    @BindView(R.id.iv_toolbar_movie_poster)
    ImageView ivToolbarMoviePoster;
    @BindView(R.id.iv_movie_poster)
    ImageView ivMoviePoster;
    @BindView(R.id.tv_movie_release_date)
    TextView tvMovieReleaseDate;
    @BindView(R.id.tv_movie_vote)
    TextView tvMovieVote;
    @BindView(R.id.tv_movie_description)
    TextView tvMovieDescription;
    @BindView(R.id.toolbar_layout_movie_details)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_movie_details)
    Toolbar toolbarMovieDetails;
    @BindView(R.id.tv_movie_title)
    TextView tvMovieTitle;
    @BindView(R.id.trailerRv)
    RecyclerView rvMovieVideos;
    @BindView(R.id.tv_video_not_available)
    TextView tvVideoNotAvailable;
    @BindView(R.id.tv_review_not_available)
    TextView tvReviewNotAvailable;
    @BindView(R.id.rv_movie_reviews)
    RecyclerView rvMovieReviews;
    private Movie currentMovie;
    private List<MovieVideo> videos;
    private MovieVideoAdapter videoAdapter;
    private List<Review> reviews;
    private MoviewReviewAdapter reviewAdapter;
    @BindView(R.id.movieFavBtn)
    LikeButton likeButton;
    private AppDatabase mDb;
    private boolean isFavorite = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()    );
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        Intent detailsIntent = getIntent();
        if (detailsIntent != null){
            currentMovie = (Movie) detailsIntent.getSerializableExtra(Constants.PASSING_MOVIE);
        }
        else {
            return;
        }

        videos = new ArrayList<>();
        rvMovieVideos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvMovieVideos.setHasFixedSize(true);
        videoAdapter = new MovieVideoAdapter();
        videoAdapter.setVideos(videos);
        videoAdapter.setOnItemClickListener(this);
        rvMovieVideos.setAdapter(videoAdapter);

        reviews = new ArrayList<>();
        rvMovieReviews.setLayoutManager(new LinearLayoutManager(this));
        rvMovieReviews.setHasFixedSize(true);
        rvMovieReviews.setNestedScrollingEnabled(false);
        reviewAdapter = new MoviewReviewAdapter(this);
        reviewAdapter.setReviews(reviews);
        rvMovieReviews.setAdapter(reviewAdapter);

        collapsingToolbarLayout.setTitle(currentMovie.getTitle());

        setSupportActionBar(toolbarMovieDetails);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mDb = AppDatabase.getInstance(getApplicationContext());


        populateMovieVideos();

        populateMovieDetails();
        RunAnimationForOverview();
        RunAnimationForAll();
    }

    private void populateMovieVideos() {
        ApiInterface apiInterface = ApiClient.getRetrofitClient().create(ApiInterface.class);
        Call<MovieVideoResponse> videoResponseCall = apiInterface.getMovieVideos(currentMovie.getId(), Constants.API_KEY);
        videoResponseCall.enqueue(new retrofit2.Callback<MovieVideoResponse>() {
            @Override
            public void onResponse(Call<MovieVideoResponse> call, Response<MovieVideoResponse> response) {
                videos = response.body().getVideos();
                if (!videos.isEmpty()){
                    tvVideoNotAvailable.setVisibility(View.GONE);
                }
                videoAdapter.setVideos(videos);
                rvMovieVideos.setAdapter(videoAdapter);
                videoAdapter.notifyDataSetChanged();
               populateMovieReviews();
            }

            @Override
            public void onFailure(Call<MovieVideoResponse> call, Throwable t) {

            }
        });
    }

    private void populateMovieReviews() {

        ApiInterface apiInterface = ApiClient.getRetrofitClient().create(ApiInterface.class);
        Call<MovieReviewResponse> reviewResponseCall = apiInterface.getMovieReviews(currentMovie.getId(), Constants.API_KEY);
        reviewResponseCall.enqueue(new retrofit2.Callback<MovieReviewResponse>() {
            @Override
            public void onResponse(Call<MovieReviewResponse> call, Response<MovieReviewResponse> response) {
                reviews = response.body().getReviews();
                if (!reviews.isEmpty()){
                    tvReviewNotAvailable.setVisibility(View.GONE);
                }
                reviewAdapter.setReviews(reviews);
                rvMovieReviews.setAdapter(reviewAdapter);
                reviewAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Call<MovieReviewResponse> call, Throwable t) {

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateMovieDetails() {

        Picasso.get()
                .load(Constants.IMAGE_BASE_URL + currentMovie.getPosterPath())
                .placeholder(R.drawable.holderimg)
                .error(R.drawable.holderimg)
                .into(ivToolbarMoviePoster);

        Picasso.get()
                .load(Constants.IMAGE_BASE_URL + currentMovie.getPosterPath())
                .placeholder(R.drawable.holderimg)
                .error(R.drawable.holderimg)
                .into(ivMoviePoster);
        MaterialImageLoading.animate(ivMoviePoster).setDuration(DURATION).start();



        tvMovieTitle.setText(currentMovie.getTitle());
        tvMovieReleaseDate.setText(currentMovie.getReleaseDate());
        tvMovieVote.setText(String.valueOf(currentMovie.getVoteAverage()));
        tvMovieDescription.setText(currentMovie.getOverview());

        String releaseDateToDisplay = "N/A";
        try {
            Date responseDate = CommonUtils.getResponseFormatter().parse(currentMovie.getReleaseDate());
            releaseDateToDisplay = CommonUtils.getFormatterToDisplay().format(responseDate);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        tvMovieReleaseDate.setText(releaseDateToDisplay);
        tvMovieVote.setText(String.valueOf(currentMovie.getVoteAverage()));


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                CommonUtils.makeTextViewResizable(tvMovieDescription, 3, currentMovie.getOverview(), Constants.VIEW_MORE, true);
            }
        }
        tvMovieDescription.setText(currentMovie.getOverview());
        AddToFavViewModelFactory viewModelFactory = new AddToFavViewModelFactory(mDb, currentMovie.getId());
        AddToFavViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(AddToFavViewModel.class);

        viewModel.getMovieId().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(@Nullable Integer integer) {

                if (currentMovie.getId().equals(integer)){
                    likeButton.setLiked(true);
                    isFavorite = true;
                } else {
                    likeButton.setLiked(false);
                    isFavorite = false;
                }
            }
        });

        likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (isFavorite){
                            mDb.favoriteMovieDao().deleteFavoriteMovie(currentMovie);
                        } else {
                            mDb.favoriteMovieDao().insertFavoriteMovie(currentMovie);
                        }
                    }
                });
            }
        });
    }


    private void RunAnimationForOverview()
    {
        Animation a = AnimationUtils.loadAnimation(this, R.anim.item_animation_slide_from_right);
        a.reset();
        tvMovieDescription.clearAnimation();
        tvMovieDescription.startAnimation(a);
    }
    private void RunAnimationForAll()
    {
        Animation a = AnimationUtils.loadAnimation(this, R.anim.item_animation_slide_from_left);
        a.reset();
        tvMovieTitle.clearAnimation();
        tvMovieTitle.startAnimation(a);

        tvMovieVote.clearAnimation();
        tvMovieVote.startAnimation(a);

        tvMovieReleaseDate.clearAnimation();
        tvMovieReleaseDate.startAnimation(a);
    }

    @Override
    public void onItemClicked(Class<?> adapterClass, int position) {
        if (adapterClass == MovieVideoAdapter.class){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.VIDEO_BASE_URL + videos.get(position).getKey())));
        }
    }
}
