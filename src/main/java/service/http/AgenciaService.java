package service.http;

import domain.Agencia;
import domain.http.AgenciaHttp;
import domain.http.SituacaoCadastral;
import exception.AgenciaNaoAtivaOuNaoEncontradaException;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class AgenciaService {

    @RestClient
    private SituacaoCadastralHttpService situacaoCadastralHttpService;

    private List<Agencia> agencias = new ArrayList<>();


    public void cadastrar(Agencia agencia) {
        AgenciaHttp agenciaHttp = situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj());

        if (agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.INATIVO)) throw new AgenciaNaoAtivaOuNaoEncontradaException("Agencia não encontrada");

        agencias.add(agencia);


    }

}
