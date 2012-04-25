package se.chalmers.threebook.ui.fragments;

import se.chalmers.threebook.R;
import se.chalmers.threebook.adapters.CollectionBookAdapter;
import se.chalmers.threebook.db.BookDataHelper;
import se.chalmers.threebook.model.Book;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

public class BooksFragment extends Fragment {
	
	private CollectionBookAdapter adapter;
	private ListView list;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
		
		View view = inflater.inflate(R.layout.fragment_books, container, false);
		
		list = (ListView) view.findViewById(R.id.lst_collection_books);
		
		adapter = new CollectionBookAdapter(view.getContext(), BookDataHelper.getBooks(view.getContext()));

		
		list.setAdapter(adapter);
		
		
		return view;
	}

	public String getName() {
		return "asfas";
	}
}
