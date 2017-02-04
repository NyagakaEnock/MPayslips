package model;

/**
 * Created by Enock on 8/17/2016.
 */
public class ItemsData {


    private String title;
    private int imageUrl;

    public ItemsData(String title,int imageUrl){

        this.title = title;
        this.imageUrl = imageUrl;
    }

    public String gettitle() {
        return title;
    }

    public void settitle(String title) {
        this.title = title;
    }
    public int getimageUrl() {
        return imageUrl;
    }

    public void setimageUrl(int imageUrl) {
        this.imageUrl = imageUrl;
    }
}
