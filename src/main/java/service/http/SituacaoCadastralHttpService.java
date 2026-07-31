package service.http;

import domain.http.AgenciaHttp;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/situacao-cadastral")
@RegisterRestClient(configKey = "situacao-cadastral-api")
interface SituacaoCadastralHttpService {

    @GET
    @Path("{cnpj}")
    AgenciaHttp buscarPorCnpj(String cnpj);

}
