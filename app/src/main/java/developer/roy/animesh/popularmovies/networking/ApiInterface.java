package developer.roy.animesh.popularmovies.networking;


import developer.roy.animesh.popularmovies.models.MovieDbResponse;
import developer.roy.animesh.popularmovies.models.MovieDetailsResponse;
import developer.roy.animesh.popularmovies.models.MovieReviewResponse;
import developer.roy.animesh.popularmovies.models.MovieVideoResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("movie/popular")
    Call<MovieDbResponse> getPopularMovies(@Query("api_key") String apiKey);

    @GET("movie/top_rated")
    Call<MovieDbResponse> getTopRatedMovies(@Query("api_key") String apiKey);

    @GET("movie/{id}")
    Call<MovieDetailsResponse> getMovieDetails(@Path("id") int id, @Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Call<MovieVideoResponse> getMovieVideos(@Path("id") int movieId, @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Call<MovieReviewResponse> getMovieReviews(@Path("id") int movieId, @Query("api_key") String apiKey);
}