package developer.roy.animesh.popularmovies.viewmodels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;



import java.util.List;

import developer.roy.animesh.popularmovies.database.AppDatabase;
import developer.roy.animesh.popularmovies.models.Movie;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<Movie>> movies;

    public MainViewModel(Application application){
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        movies = database.favoriteMovieDao().loadAllFavoriteMovies();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }
}
