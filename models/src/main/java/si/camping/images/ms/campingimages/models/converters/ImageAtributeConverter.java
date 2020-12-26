package si.camping.images.ms.campingimages.models.converters;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Base64;

@Converter(autoApply = true)
public class ImageAtributeConverter implements AttributeConverter<String, byte[]> {

    @Override
    public byte[] convertToDatabaseColumn(String image) {
        return (image == null ? null : Base64.getEncoder().encodeToString(image.getBytes()).getBytes());
    }

    @Override
    public String convertToEntityAttribute(byte[] image) {
        return (image == null ? null : new String(Base64.getDecoder().decode(image)));
    }
}
