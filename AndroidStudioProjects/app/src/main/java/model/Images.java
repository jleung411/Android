package model;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;

public class Images {

    @Expose
    private List<String> images = new ArrayList<String>();
    @Expose
    private String error;

    /**
     *
     * @return
     * The images
     */
    public List<String> getImages() {
        return images;
    }

    /**
     *
     * @param images
     * The images
     */
    public void setImages(List<String> images) {
        this.images = images;
    }

    /**
     *
     * @return
     * The error
     */
    public String getError() {
        return error;
    }

    /**
     *
     * @param error
     * The error
     */
    public void setError(String error) {
        this.error = error;
    }

}