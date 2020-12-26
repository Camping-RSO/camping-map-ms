package si.camping.images.ms.campingimages.lib;

import java.time.Instant;

public class Image {
    private Integer slikaId;
    private Instant created;
    private String url;
    private Integer avtokamp;

    public Integer getSlikaId() {
        return slikaId;
    }

    public void setSlikaId(Integer slikaId) {
        this.slikaId = slikaId;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getAvtokamp() {
        return avtokamp;
    }

    public void setAvtokamp(Integer avtokamp) {
        this.avtokamp = avtokamp;
    }
}
