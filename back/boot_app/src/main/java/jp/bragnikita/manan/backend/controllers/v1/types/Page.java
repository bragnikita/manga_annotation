package jp.bragnikita.manan.backend.controllers.v1.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class Page {

    @Getter @Setter @NoArgsConstructor
    public static class Preview {
        String id;
        String previewUrl;
        String originalFileName;
    }

    @Getter @Setter @NoArgsConstructor
    public static class Full {
        String id;
        String imageUrl;
        JsonNode meta;
    }

    @Getter
    public static class UpdateRequest {
        JsonNode meta;

        public UpdateRequest() {
            ObjectMapper mapper = new ObjectMapper();
            this.meta = mapper.createArrayNode();
        }
    }

}
