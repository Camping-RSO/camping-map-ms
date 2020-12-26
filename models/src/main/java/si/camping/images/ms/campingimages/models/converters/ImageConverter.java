package si.camping.images.ms.campingimages.models.converters;

import si.camping.images.ms.campingimages.lib.Image;
import si.camping.images.ms.campingimages.models.entities.ImageEntity;

public class ImageConverter {

    public static Image toDto(ImageEntity entity) {
        Image dto = new Image();
        dto.setSlikaId(entity.getSlikaId());
        dto.setCreated(entity.getCreated());
        dto.setUrl(entity.getUrl());
        dto.setAvtokamp(entity.getAvtokamp());

        return dto;
    }

    public static ImageEntity toEntity(Image dto) {
        ImageEntity entity = new ImageEntity();
        entity.setSlikaId(dto.getSlikaId());
        entity.setCreated(dto.getCreated());
        entity.setUrl(dto.getUrl());
        entity.setAvtokamp(dto.getAvtokamp());

        return entity;
    }

}
