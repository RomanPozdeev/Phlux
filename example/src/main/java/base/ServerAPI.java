package base;

import android.text.Html;

import com.google.gson.annotations.SerializedName;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ServerAPI {

    String ENDPOINT = "http://api.icndb.com";

    @GET("/jokes/random/10")
    Single<Response> getItems(@Query("firstName") String firstName, @Query("lastName") String lastName);

    class Item {
        @SerializedName("joke")
        public String text;

        @Override
        public String toString() {
            return Html.fromHtml(text).toString();
        }
    }

    class Response {
        @SerializedName("value")
        public Item[] items;
    }
}
