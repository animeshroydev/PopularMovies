package developer.roy.animesh.popularmovies.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import developer.roy.animesh.popularmovies.database.AppDatabase;


public class AddToFavViewModel extends ViewModel {

    private LiveData<Integer> movieId;

    public AddToFavViewModel(AppDatabase database, Integer movieId) {
        this.movieId = database.favoriteMovieDao().getMovieId(movieId);
    }

    public LiveData<Integer> getMovieId(){
        return movieId;
    }
}
