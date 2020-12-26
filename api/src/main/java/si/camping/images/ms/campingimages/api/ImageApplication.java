package si.camping.images.ms.campingimages.api;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.servers.Server;

@OpenAPIDefinition(info = @Info(title = "Camping images microservice API", version = "v1",
        contact = @Contact(email = "al9838@students.uni-lj.si"), description = "API for managing camping images"),
        servers = @Server(url = "http://localhost:8080/"))
@ApplicationPath("/")
public class ImageApplication extends Application {

}
