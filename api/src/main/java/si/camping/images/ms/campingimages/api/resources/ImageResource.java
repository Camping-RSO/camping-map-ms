package si.camping.images.ms.campingimages.api.resources;

import com.kumuluz.ee.cors.annotations.CrossOrigin;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.logs.cdi.Log;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.headers.Header;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import si.camping.images.ms.campingimages.api.dtos.UploadImageResponse;
import si.camping.images.ms.campingimages.dtos.ImageProcessRequest;
import si.camping.images.ms.campingimages.lib.Image;
import si.camping.images.ms.campingimages.services.beans.ImageBean;
import si.camping.images.ms.campingimages.services.clients.AmazonRekognitionClient;
import si.camping.images.ms.campingimages.services.clients.ImageProcessingApi;
import si.camping.images.ms.campingimages.services.streaming.EventProducerImpl;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.logging.Logger;

@Log
@ApplicationScoped
@Path("/slike")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@CrossOrigin(supportedMethods = "GET, POST, HEAD, DELETE, OPTIONS")
public class ImageResource {

    private Logger log = Logger.getLogger(ImageResource.class.getName());

    @Inject
    private ImageBean imageBean;

    @Inject
    private EventProducerImpl eventProducer;

    @Context
    protected UriInfo uriInfo;

    @Inject
    private AmazonRekognitionClient amazonRekognitionClient;

    //    @Inject
//    @RestClient
    private ImageProcessingApi imageProcessingApi;

    @DiscoverService("image-processing-service")
    private URI imageProcessingServiceUrl;

    @PostConstruct
    private void init() {
        imageProcessingApi = RestClientBuilder
                .newBuilder()
                .baseUri(imageProcessingServiceUrl)
                .build(ImageProcessingApi.class);
    }

    @Operation(description = "Vrni vse slike avtokampov.", summary = "Podatki o vseh slikah")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Seznam vseh slik",
                    content = @Content(schema = @Schema(implementation = Image.class, type = SchemaType.ARRAY)),
                    headers = {@Header(name = "X-Total-Count", description = "Število vseh slik")}
            )})
    @GET
    public Response getImageMetadata() {
        log.info("Pridobivam vse slike...");
        List<Image> imageMetadata = imageBean.getImageMetadataFilter(uriInfo);
        log.info("Vse slike so bile pridobljene.");
        return Response.status(Response.Status.OK).entity(imageMetadata).build();
    }

    @Operation(description = "Pridobi podatke za posamezno sliko.", summary = "Podatki o sliki")
    @APIResponses({
            @APIResponse(responseCode = "200",
                    description = "Image metadata",
                    content = @Content(
                            schema = @Schema(implementation = Image.class))
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Slika ni bila najdena."
            )})
    @GET
    @Path("{slika_id}")
    public Response getImageMetadata(@Parameter(description = "Id slike", required = true)
                                     @PathParam("slika_id") Integer imageMetadataId) {
        log.info("Pridobivam sliko...");
        Image image = imageBean.getImageMetadata(imageMetadataId);

        if (image == null) {
            log.warning("Napaka pri pridobivanju slike.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.info("Slika je bila pridobljena.");
        return Response.status(Response.Status.OK).entity(image).build();
    }

    @Operation(description = "Dodaj sliko.", summary = "Dodaj novo sliko")
    @APIResponses({
            @APIResponse(responseCode = "201",
                    description = "Slika je bila uspešno dodana."
            ),
            @APIResponse(responseCode = "400", description = "Napaka.")
    })
    @POST
    public Response createImageMetadata(@RequestBody(description = "Slika", required = true, content = @Content(
            schema = @Schema(implementation = Image.class))) Image image) {
        log.info("Dodajam sliko...");
        if ((image.getAvtokamp() == null || image.getUrl() == null)) {
            log.warning("Napaka pri dodajanju slike.");
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            image = imageBean.createImageMetadata(image);
        }
        log.info("Slika je bila dodana.");
        return Response.status(Response.Status.CREATED).entity(image).build();

    }

    @Operation(description = "Posodobi podatke o sliki.", summary = "Posodobi sliko")
    @APIResponses({
            @APIResponse(
                    responseCode = "200",
                    description = "Slika je bila uspešno posodobljena."
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Slika ni bila najdena."
            )
    })
    @PUT
    @Path("{slika_id}")
    public Response putImageMetadata(@Parameter(description = "Id slike", required = true)
                                     @PathParam("slika_id") Integer imageMetadataId,
                                     @RequestBody(
                                             description = "Slika",
                                             required = true, content = @Content(
                                             schema = @Schema(implementation = Image.class)))
                                             Image image) {
        log.info("Posodabljam sliko...");
        image = imageBean.putImageMetadata(imageMetadataId, image);

        if (image == null) {
            log.warning("Napaka pri posodabljanju slike.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        log.info("Slika je bila posodobljena.");
        return Response.status(Response.Status.OK).build();

    }

    @Operation(description = "Izbriši sliko.", summary = "Brisanje slike")
    @APIResponses({
            @APIResponse(
                    responseCode = "204",
                    description = "Slika je bila uspešno izbrisana."
            ),
            @APIResponse(
                    responseCode = "404",
                    description = "Slika ni bila najdena."
            )
    })
    @DELETE
    @Path("{slika_id}")
    public Response deleteImageMetadata(@Parameter(description = "Id slike", required = true)
                                        @PathParam("slika_id") Integer imageMetadataId) {
        log.info("Brišem sliko...");
        boolean deleted = imageBean.deleteImageMetadata(imageMetadataId);

        if (deleted) {
            log.warning("Slika je bila izbrisana.");
            return Response.status(Response.Status.NO_CONTENT).build();
        } else {
            log.info("Napaka pri brisanju slike.");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

//    @Operation(description = "Naloži sliko.", summary = "Nalaganje slike")
//    @APIResponses({
//            @APIResponse(
//                    responseCode = "201",
//                    description = "Slika je bila uspešno naložena."
//            ),
//            @APIResponse(
//                    responseCode = "404",
//                    description = "Not found."
//            )
//    })
//    @POST
//    @Path("/nalaganje")
//    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
//    public Response uploadImage(InputStream uploadedInputStream) {
//        log.info("Nalagam sliko...");
//        String imageId = UUID.randomUUID().toString();
//        String imageLocation = UUID.randomUUID().toString();
//
//        byte[] bytes = new byte[0];
//        try (uploadedInputStream) {
//            bytes = uploadedInputStream.readAllBytes();
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        UploadImageResponse uploadImageResponse = new UploadImageResponse();
//
//        Integer numberOfFaces = amazonRekognitionClient.countFaces(bytes);
//        uploadImageResponse.setNumberOfFaces(numberOfFaces);
//
//        if (numberOfFaces != 1) {
//            uploadImageResponse.setMessage("Image must contain one face.");
//            return Response.status(Response.Status.BAD_REQUEST).entity(uploadImageResponse).build();
//
//        }
//
//        List<String> detectedCelebrities = amazonRekognitionClient.checkForCelebrities(bytes);
//
//        if (!detectedCelebrities.isEmpty()) {
//            uploadImageResponse.setMessage("Image must not contain celebrities. Detected celebrities: "
//                    + detectedCelebrities.stream().collect(Collectors.joining(", ")));
//            return Response.status(Response.Status.BAD_REQUEST).entity(uploadImageResponse).build();
//        }
//
//        uploadImageResponse.setMessage("Slika uspešno naložena.");
//
//        // Upload image to storage
//
//        // Generate event for image processing
//        eventProducer.produceMessage(imageId, imageLocation);
//
//        // start image processing over async API
//        CompletionStage<String> stringCompletionStage =
//                imageProcessingApi.processImageAsynch(new ImageProcessRequest(imageId, imageLocation));
//
//        stringCompletionStage.whenComplete((s, throwable) -> System.out.println(s));
//        stringCompletionStage.exceptionally(throwable -> {
//            log.severe(throwable.getMessage());
//            return throwable.getMessage();
//        });
//
//        log.info("Slika je bila naložena.");
//        return Response.status(Response.Status.CREATED).entity(uploadImageResponse).build();
//    }
}
