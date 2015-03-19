package eu.europeana.harvester.domain;

import java.io.Serializable;

public class TextMetaInfo implements Serializable {

    /**
     * An Internet media type is a standard identifier used on the
     * Internet to indicate the type of data that a file contains.
     */
    private final String mimeType;

    /**
     * The size of the file in bytes.
     */
    private final Long fileSize;

    /**
     * The number of lines or resolution height e.g. 1080, 720, etc.
     * */
    private final Integer resolution;

    /**
     * Shows if the file contains only images (returns false) or any text (returns true)
     */
    private final Boolean isSearchable;

    public TextMetaInfo(final String mimeType, final Long fileSize,
                        final Integer resolution, final Boolean isSearchable) {
        this.mimeType = mimeType;
        this.fileSize = fileSize;
        this.resolution = resolution;
        this.isSearchable = isSearchable;
    }

    public TextMetaInfo() {
        this.mimeType = null;
        this.fileSize = null;
        this.resolution = null;
        this.isSearchable = null;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public Integer getResolution() {
        return resolution;
    }

    public Boolean getIsSearchable() {
        return isSearchable;
    }
}
