package jp.bragnikita.manan.backend.controllers.v1.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class Series {

    @Getter @Setter @NoArgsConstructor
    public static class Brief {
        String id;
        String title;
        String description;
        String publishingStatus;
        String translationStatus;
        String ownerUsername;
        String creatorUsername;
        String updatedAt;
        String createdAt;
        String coverUrl;
        String coverId;
    }

    @Getter @Setter @NoArgsConstructor
    public static class Full {
        Brief info;
        @JsonProperty("pages")
        Page.Preview[] pages;
    }

    @Getter @NoArgsConstructor
    public static class CreateUpdateInfo {
        String title;
        String description;
        String publishingStatus;
        String translationStatus;
        String coverId;
    }

    @Getter @NoArgsConstructor
    public static class CreateUpdate {
        CreateUpdateInfo info;
        String[] pages;
    }
}
