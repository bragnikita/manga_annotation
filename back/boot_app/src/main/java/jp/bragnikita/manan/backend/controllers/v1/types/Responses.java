package jp.bragnikita.manan.backend.controllers.v1.types;

import java.util.List;

import lombok.Getter;

public class Responses {

    @Getter
    public static class Listing {
        Object[] items;

        public static <T> Responses.Listing fromList(List<T> list) {
            Responses.Listing listing = new Listing();
            listing.items = list.toArray();
            return listing;
        }
    }
}
