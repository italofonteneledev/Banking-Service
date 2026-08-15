package service;

import domain.Agencia;
import domain.http.AgenciaHttp;
import domain.http.SituacaoCadastral;
import exception.AgenciaNaoAtivaOuNaoEncontradaException;
import exception.AgencyNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
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

    @WithTransaction
    public Uni<Void> cadastrar(Agencia agencia) {
        Uni<AgenciaHttp> agenciaHttp = situacaoCadastralHttpService.buscarPorCnpj(agencia.getCnpj());
        return agenciaHttp
                .onItem().ifNull().failWith(new AgenciaNaoAtivaOuNaoEncontradaException("Agencia não encontrada ou inativa"))
                .onItem().transformToUni(item -> persistirSeAtiva(agencia, item));
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

    private Uni<Void> persistirSeAtiva(Agencia agencia, AgenciaHttp agenciaHttp) {
        if (agenciaHttp == null || agenciaHttp.getSituacaoCadastral().equals(SituacaoCadastral.INATIVO)) {
            meterRegistry.counter("agencia_nao_adicionada_counter").increment();
            return Uni.createFrom().failure(new AgenciaNaoAtivaOuNaoEncontradaException("Agencia não encontrada ou inativa"));
        }

        meterRegistry.counter("agencia_adicionada_counter").increment();

        return agenciaRepository.persist(agencia).replaceWithVoid();
    }

}
