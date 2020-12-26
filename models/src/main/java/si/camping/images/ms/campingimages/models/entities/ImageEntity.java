package si.camping.images.ms.campingimages.models.entities;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "slike")
@NamedQueries(value =
        {
                @NamedQuery(name = "ImageMetadataEntity.getAll", query = "SELECT im FROM ImageEntity im")
        })
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "slika_id")
    private Integer slikaId;

    @Column(name = "url")
    private String url;

    @Column(name = "created")
    private Instant created;

    @Column(name = "avtokamp")
    private Integer avtokamp;

    public Integer getSlikaId() {
        return slikaId;
    }

    public void setSlikaId(Integer slikaId) {
        this.slikaId = slikaId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Instant getCreated() {
        return created;
    }

    public void setCreated(Instant created) {
        this.created = created;
    }

    public Integer getAvtokamp() {
        return avtokamp;
    }

    public void setAvtokamp(Integer avtokamp) {
        this.avtokamp = avtokamp;
    }
}
