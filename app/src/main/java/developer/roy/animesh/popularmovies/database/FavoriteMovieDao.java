package developer.roy.animesh.popularmovies.database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;



import java.util.List;

import developer.roy.animesh.popularmovies.models.Movie;

@Dao
public interface FavoriteMovieDao {

    @Query("SELECT * FROM favorite_movie")
    LiveData<List<Movie>> loadAllFavoriteMovies();

    @Insert
    void insertFavoriteMovie(Movie movie);

    @Delete
    void deleteFavoriteMovie(Movie movie);

    @Query("SELECT id FROM favorite_movie WHERE id = :movieId")
    LiveData<Integer> getMovieId(Integer movieId);
}
