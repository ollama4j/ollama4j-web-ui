package io.github.ollama4j.webui.data;

import io.github.ollama4j.models.response.LibraryModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class LibraryModelItem extends LibraryModel {
    private String popularTagsString;
    private String link;

    public String getLink() {
        return String.format("https://ollama.com/library/%s", getName());
    }

    public String getPopularTagsString() {
        StringBuilder tagsString = new StringBuilder();
        getPopularTags().forEach(tag -> {
            if (!tagsString.isEmpty()) {
                tagsString.append(", ");
            }
            tagsString.append(tag);
        });
        this.popularTagsString = tagsString.toString();
        return this.popularTagsString;
    }
}
