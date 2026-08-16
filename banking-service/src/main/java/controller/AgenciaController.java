package controller;

import domain.Agencia;
import io.smallrye.common.annotation.NonBlocking;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.reactive.RestResponse;
import service.AgenciaService;

@Path("/agencias")
public class AgenciaController {

    private final AgenciaService agenciaService;

    public AgenciaController(AgenciaService agenciaService) {
        this.agenciaService = agenciaService;
    }

    @POST
    @NonBlocking
    public Uni<RestResponse<Void>> cadastrar(Agencia agencia, @Context UriInfo uriInfo) {
        return this.agenciaService.cadastrar(agencia).replaceWith(RestResponse.created(uriInfo.getAbsolutePath()));
    }

    @GET
    @Path("{id}")
    public Uni<RestResponse<Agencia>> buscarPorId(Long id) {
        return this.agenciaService.buscarPorId(id).onItem().transform(agencia -> RestResponse.ok(agencia));
    }

    @DELETE
    @Path("{id}")
    public Uni<RestResponse<Void>> deletar(Long id) {
        return this.agenciaService.deletar(id).replaceWith(RestResponse.ok());
    }

    @PUT
    public Uni<RestResponse<Void>> alterar(Agencia agencia) {
       return this.agenciaService.alterar(agencia).replaceWith(RestResponse.ok());
    }


}
