package si.camping.images.ms.campingimages.services.beans;

import com.kumuluz.ee.rest.beans.QueryParameters;
import com.kumuluz.ee.rest.utils.JPAUtils;
import org.eclipse.microprofile.metrics.annotation.Timed;
import si.camping.images.ms.campingimages.lib.Image;
import si.camping.images.ms.campingimages.models.converters.ImageConverter;
import si.camping.images.ms.campingimages.models.entities.ImageEntity;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;


@RequestScoped
public class ImageBean {

    private Logger log = Logger.getLogger(ImageBean.class.getName());

    @Inject
    private ImageBean imageBeanProxy;

    @Inject
    private EntityManager em;

    private Client httpClient;
    private String baseUrl;

    @PostConstruct
    private void init() {
        httpClient = ClientBuilder.newClient();
        baseUrl = "http://localhost:8081"; // only for demonstration
    }

    public List<Image> getImageMetadata() {

        TypedQuery<ImageEntity> query = em.createNamedQuery(
                "ImageMetadataEntity.getAll", ImageEntity.class);

        List<ImageEntity> resultList = query.getResultList();

        return resultList.stream().map(ImageConverter::toDto).collect(Collectors.toList());

    }

    @Timed
    public List<Image> getImageMetadataFilter(UriInfo uriInfo) {
        QueryParameters queryParameters = QueryParameters.query(uriInfo.getRequestUri().getQuery()).defaultOffset(0)
                .build();

        return JPAUtils.queryEntities(em, ImageEntity.class, queryParameters).stream()
                .map(ImageConverter::toDto).collect(Collectors.toList());
    }

    public Image getImageMetadata(Integer id) {
        ImageEntity imageEntity = em.find(ImageEntity.class, id);
        if (imageEntity == null) {
            throw new NotFoundException();
        }
        Image image = ImageConverter.toDto(imageEntity);

        return image;
    }

    public List<Image> getCampingImagesMetadata(Integer kampId) {
        List<Image> images = getImageMetadata();
        List<Image> result = new ArrayList<>();

        for (Image i: images) {
            if (i.getAvtokamp().equals(kampId)) {
                result.add(i);
            }
        }

        return result;
    }

    public Image getCampingImageMetadata(Integer kampId) {
        List<Image> images = getImageMetadata();
        Image result = null;

        for (Image i: images) {
            if (i.getAvtokamp().equals(kampId)) {
                result = i;
                break;
            }
        }

        if (result == null) {
            throw new NotFoundException();
        }

        return result;
    }

    public Image createImageMetadata(Image image) {
        ImageEntity imageEntity = ImageConverter.toEntity(image);

        try {
            beginTx();
            em.persist(imageEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        if (imageEntity.getSlikaId() == null) {
            throw new RuntimeException("Entity was not persisted");
        }

        return ImageConverter.toDto(imageEntity);
    }

    public Image putImageMetadata(Integer id, Image image) {
        ImageEntity c = em.find(ImageEntity.class, id);

        if (c == null) {
            return null;
        }

        ImageEntity updatedImageEntity = ImageConverter.toEntity(image);

        try {
            beginTx();
            updatedImageEntity.setSlikaId(c.getSlikaId());
            updatedImageEntity = em.merge(updatedImageEntity);
            commitTx();
        } catch (Exception e) {
            rollbackTx();
        }

        return ImageConverter.toDto(updatedImageEntity);
    }

    public boolean deleteImageMetadata(Integer id) {
        ImageEntity imageMetadata = em.find(ImageEntity.class, id);

        if (imageMetadata != null) {
            try {
                beginTx();
                em.remove(imageMetadata);
                commitTx();
            } catch (Exception e) {
                rollbackTx();
            }
        } else {
            return false;
        }

        return true;
    }

    private void beginTx() {
        if (!em.getTransaction().isActive()) {
            em.getTransaction().begin();
        }
    }

    private void commitTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
    }

    private void rollbackTx() {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
