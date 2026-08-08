package service;

import domain.Agencia;
import domain.http.AgenciaHttp;
import domain.http.SituacaoCadastral;
import exception.AgenciaNaoAtivaOuNaoEncontradaException;
import exception.AgencyNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import repository.AgenciaRepository;
import service.http.SituacaoCadastralHttpService;

@ApplicationScoped
public class AgenciaService {

    @RestClient
    private SituacaoCadastralHttpService situacaoCadastralHttpService;

    private final AgenciaRepository agenciaRepository;

    private final MeterRegistry meterRegistry;

    public AgenciaService(AgenciaRepository agenciaRepository, MeterRegistry meterRegistry) {
        this.agenciaRepository = agenciaRepository;
        this.meterRegistry = meterRegistry;
    }

    @Transactional
    public void cadastrar(Agencia agencia) {
        AgenciaHttp agenciaHttp = situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj());
        if (agenciaHttp != null && agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.INATIVO)) {
            meterRegistry.counter("agencia_nao_adicionada_counter").increment();
            throw new AgenciaNaoAtivaOuNaoEncontradaException("Agencia não encontrada ou ativa");
        }

        meterRegistry.counter("agencia_adicionada_counter").increment();

        agenciaRepository.persist(agencia);
    }

    public Agencia buscarPorId(Long id) {
        return agenciaRepository.findByIdOptional(id).orElseThrow(() -> new AgencyNotFoundException("Agency not found"));
    }

    public void deletar(Long id) {
        agenciaRepository.deleteById(id);
    }

    public void alterar(Agencia agencia) {
        agenciaRepository.update("nome = ?1, razaoSocial = ?2, cnpj = ?3 = where id = ?4", agencia.getNome(), agencia.getRazaoSocial(), agencia.getCnpj(), agencia.getId());
    }

}
