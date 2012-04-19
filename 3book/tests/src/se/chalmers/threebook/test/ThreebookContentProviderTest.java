package se.chalmers.threebook.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import se.chalmers.threebook.contentprovider.ThreeBookContentProvider;
import se.chalmers.threebook.db.AuthorTable;
import se.chalmers.threebook.db.BookAuthorsTable;
import se.chalmers.threebook.db.BookTable;
import se.chalmers.threebook.model.Author;
import se.chalmers.threebook.model.Book;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.test.ProviderTestCase2;
import android.test.mock.MockContentResolver;

public class ThreebookContentProviderTest extends ProviderTestCase2<ThreeBookContentProvider> {

    private MockContentResolver mMockResolver;
    private SQLiteDatabase mDb;
    
    List<Author> mAuthors;
    List<Book> mBooks;
    
    public ThreebookContentProviderTest() {
	super(ThreeBookContentProvider.class, "se.chalmers.threebook.contentprovider");
	
    }
    
    @Override
    protected void setUp() throws Exception {
	super.setUp();
	
	mMockResolver = getMockContentResolver();
	
	mDb = getProvider().getOpenHelperForTest().getWritableDatabase();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    private void insertData() {
	mAuthors = new ArrayList<Author>();
	
	//create a couple of authors
	for(int i = 0; i<10; i++) {
	    Author author = new Author();
	    author.setFirstName("fName" + i);
	    author.setLastName("lName" + i);
	    
	    mAuthors.add(author);
	}
	
	mBooks = new ArrayList<Book>();
	
	//create some books
	for(int i = 0; i<10; i++) {
	    Book book = new Book();
	    book.setTitle("book" + i);
	    List<Author> bookAuthors = book.getAuthors();
	    
	    List<Author> availableAuthors = new ArrayList<Author>();
	    availableAuthors.addAll(mAuthors);
		
	    //associate between 1 and 3 random authors for each book.
	    for(int j = new Random().nextInt(2); j<3; j++) {
		Author author = availableAuthors.get(new Random().nextInt(availableAuthors.size()));
		availableAuthors.remove(author); // make sure we don't select the same author again.
		bookAuthors.add(author);
	    }
	    
	    mBooks.add(book);
	}
	
	for(Book book : mBooks) {
	    ContentValues bookValues = new ContentValues();
	    bookValues.put(BookTable.COLUMN_TITLE, book.getTitle());
	    
//	    Long bookId = mDb.insertOrThrow(BookTable.TABLE_BOOKS, null, bookValues);
	    Uri uri = mMockResolver.insert(ThreeBookContentProvider.BOOK_URI, bookValues);
	    String bookId = uri.getLastPathSegment();
	    
	    
	    for(Author author : book.getAuthors()) {
		ContentValues authorValues = new ContentValues();
		authorValues.put(AuthorTable.COLUMN_FIRSTNAME, author.getFirstName());
		authorValues.put(AuthorTable.COLUMN_LASTNAME, author.getLastName());
		
		mMockResolver.insert(Uri.withAppendedPath(ThreeBookContentProvider.BOOK_AUTHORS_URI, bookId), authorValues);
//		mDb.insertOrThrow(BookAuthorsTable.TABLE_BOOK_AUTHORS, null, authorValues);
	    }
	}
    }
    
    public void testQueriesOnBookUri() {
	Cursor cursor = mMockResolver.query(ThreeBookContentProvider.BOOK_URI, null, null, null, null);
	
	assertEquals(mBooks.size(), cursor.getCount());
    }
    
    
    
}
