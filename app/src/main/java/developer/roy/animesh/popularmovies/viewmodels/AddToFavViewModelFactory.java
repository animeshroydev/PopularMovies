package developer.roy.animesh.popularmovies.viewmodels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import developer.roy.animesh.popularmovies.database.AppDatabase;


public class AddToFavViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private final AppDatabase mDb;
    private final Integer mMovieId;

    public AddToFavViewModelFactory(AppDatabase mDb, Integer mMovieId) {
        this.mDb = mDb;
        this.mMovieId = mMovieId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        return (T) new AddToFavViewModel(mDb, mMovieId);
    }
}
