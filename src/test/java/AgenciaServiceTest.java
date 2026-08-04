import domain.Agencia;
import domain.Endereco;
import exception.AgenciaNaoAtivaOuNaoEncontradaException;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import repository.AgenciaRepository;
import service.AgenciaService;
import service.http.SituacaoCadastralHttpService;

@QuarkusTest
public class AgenciaServiceTest {

    @InjectMock
    private AgenciaRepository agenciaRepository;

    @InjectMock
    @RestClient
    private SituacaoCadastralHttpService situacaoCadastralHttpService;

    @Inject
    private AgenciaService agenciaService;

    @Test
    public void shouldNotCreateWhenClientReturnNull() {
        Agencia agencia = criarAgencia("200");
        Mockito.when(situacaoCadastralHttpService.buscarPorCnpj("200")).thenReturn(null);

        Assertions.assertThrows(AgenciaNaoAtivaOuNaoEncontradaException.class, () -> agenciaService.cadastrar(agencia));

        Mockito.verify(agenciaRepository, Mockito.never()).persist(agencia);
    }


    private Agencia criarAgencia(String cnpj) {
        Endereco endereco = new Endereco(1, "", "", "", 1);
        return new Agencia(1, "", "", cnpj, endereco);
    }

}
