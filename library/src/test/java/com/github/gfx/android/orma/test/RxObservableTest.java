package com.github.gfx.android.orma.test;

import com.github.gfx.android.orma.BuildConfig;
import com.github.gfx.android.orma.ModelBuilder;
import com.github.gfx.android.orma.SingleRelation;
import com.github.gfx.android.orma.test.model.Book;
import com.github.gfx.android.orma.test.model.OrmaDatabase;
import com.github.gfx.android.orma.test.model.Publisher;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import android.content.Context;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, manifest = Config.NONE)
public class RxObservableTest {

    OrmaDatabase db;

    Context getContext() {
        return RuntimeEnvironment.application;
    }

    @Before
    public void setUp() throws Exception {
        db = new OrmaDatabase(getContext(), null);

        final Publisher publisher = db.createPublisher(new ModelBuilder<Publisher>() {
            @Override
            public Publisher build() {
                Publisher publisher = new Publisher();
                publisher.name = "foo bar";
                publisher.startedYear = 2015;
                publisher.startedMonth = 12;

                return publisher;
            }
        });

        db.createBook(new ModelBuilder<Book>() {
            @Override
            public Book build() {
                Book book = new Book();
                book.title = "today";
                book.content = "milk, banana";
                book.inPrint = true;
                book.publisher = SingleRelation.id(publisher.id);
                return book;
            }
        });

        db.createBook(new ModelBuilder<Book>() {
            @Override
            public Book build() {
                Book book = new Book();
                book.title = "friday";
                book.content = "apple";
                book.inPrint = false;
                book.publisher = SingleRelation.id(publisher.id);
                return book;
            }
        });
    }

    @Test
    public void testObservable() throws Exception {
        List<Book> list = db.selectFromBook()
                .where("title = ?", "today")
                .observable()
                .toList()
                .toBlocking()
                .single();

        assertThat(list.size(), is(1));
        assertThat(list.get(0).title, is("today"));
    }
}
