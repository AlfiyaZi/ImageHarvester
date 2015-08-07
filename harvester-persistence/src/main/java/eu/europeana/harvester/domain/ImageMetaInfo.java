package eu.europeana.harvester.domain;

import java.io.Serializable;

/**
 * A class which contains information about an IMAGE document
 */
public class ImageMetaInfo implements Serializable {

    /**
     * The width of image in pixels.
     */
    private final Integer width;

    /**
     * The height of image in pixels.
     */
    private final Integer height;

    /**
     * An Internet media type is a standard identifier used on the
     * Internet to indicate the type of data that a file contains.
     */
    private final String mimeType;

    /**
     * A file format is a standard way that information is encoded for storage in a computer file.
     */
    private final String fileFormat;

    /**
     * A color space is a specific organization of colors.
     */
    private final String colorSpace;

    /**
     * The size of the file in bytes
     */
    private final Long fileSize;

    /**
     * An array with up to 6 colors (in the HEX code used for web applications)
     */
    private final String[] colorPalette;

    /**
     * The orientation of the image (LANDSCAPE or PORTRAIT)
     */
    private final ImageOrientation orientation;

    public ImageMetaInfo() {
        this.width = null;
        this.height = null;
        this.mimeType = null;
        this.fileFormat = null;
        this.colorSpace = null;
        this.fileSize = null;
        this.colorPalette = null;
        this.orientation = null;
    }

    public ImageMetaInfo(final Integer width, final Integer height,
                         final String mimeType, final String fileFormat, final String colorSpace,
                         final Long fileSize, final String[] colorPalette, final ImageOrientation orientation) {
        this.width = width;
        this.height = height;
        this.mimeType = mimeType;
        this.fileFormat = fileFormat;
        this.colorSpace = colorSpace;
        this.fileSize = fileSize;
        this.colorPalette = colorPalette;
        this.orientation = orientation;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public String getColorSpace() {
        return colorSpace;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public String[] getColorPalette() {
        return colorPalette;
    }

    public ImageOrientation getOrientation() {
        return orientation;
    }

    public ImageMetaInfo withColorPalette(final String[] newColorPalette) {
        return new ImageMetaInfo(width,height,mimeType,fileFormat,colorSpace,fileSize,newColorPalette,orientation);
    }

    public boolean hasOnlyColorPalette() {
        return (width == null) &&
        (height == null) &&
        (mimeType == null) &&
        (fileFormat == null) &&
        (colorSpace == null) &&
        (fileSize == null) &&
        (colorPalette != null && colorPalette.length > 0) &&
        (orientation == null);
    }

}
