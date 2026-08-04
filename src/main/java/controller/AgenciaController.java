package controller;

import domain.Agencia;
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
    public RestResponse<Void> cadastrar(Agencia agencia, @Context UriInfo uriInfo) {
        this.agenciaService.cadastrar(agencia);
        return RestResponse.created(uriInfo.getAbsolutePath());
    }

    @GET
    @Path("{id}")
    public RestResponse<Agencia> buscarPorId(Long id) {
        return RestResponse.ok(this.agenciaService.buscarPorId(id));
    }

    @DELETE
    @Path("{id}")
    public RestResponse<Void> deletar(Long id) {
        this.agenciaService.deletar(id);
        return RestResponse.ok();
    }

    @PUT
    public RestResponse<Void> alterar(Agencia agencia) {
        return RestResponse.ok();
    }


}
