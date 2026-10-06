package br.com.cinereserve;

import br.com.cinereserve.cli.ConsoleApp;
import br.com.cinereserve.model.Assento;
import br.com.cinereserve.model.Sessao;
import br.com.cinereserve.model.TipoAssento;
import br.com.cinereserve.repository.ClienteRepository;
import br.com.cinereserve.repository.ReservaRepository;
import br.com.cinereserve.repository.SessaoRepository;
import br.com.cinereserve.repository.json.ClienteJsonRepository;
import br.com.cinereserve.repository.json.ReservaJsonRepository;
import br.com.cinereserve.repository.json.SessaoJsonRepository;
import br.com.cinereserve.service.ReservaService;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Ponto de entrada (Bootstrap) da aplicação CineReserve.
 */
public class Main {

    public static void main(String[] args) {
        Path pastaDados = Paths.get("data");

        ClienteRepository clienteRepo = new ClienteJsonRepository(pastaDados.resolve("clientes.json"));
        SessaoRepository sessaoRepo = new SessaoJsonRepository(pastaDados.resolve("sessoes.json"));
        ReservaRepository reservaRepo = new ReservaJsonRepository(pastaDados.resolve("reservas.json"), clienteRepo, sessaoRepo);

        ReservaService reservaService = new ReservaService(clienteRepo, sessaoRepo, reservaRepo);

        // Se for a primeira execução e não houver sessões cadastradas, cria sessões de demonstração
        inicializarDadosSeNecessario(sessaoRepo);

        ConsoleApp app = new ConsoleApp(clienteRepo, sessaoRepo, reservaService);
        app.iniciar();
    }

    private static void inicializarDadosSeNecessario(SessaoRepository sessaoRepo) {
        if (sessaoRepo.buscarTodas().isEmpty()) {
            List<Assento> assentosSala = new ArrayList<>();

            // Gera sala com 4 fileiras (A, B, C, D) e 6 assentos por fileira
            // Fileira A: Comum
            // Fileira B: Comum + PNE
            // Fileira C e D: VIP
            for (char fila : List.of('A', 'B', 'C', 'D')) {
                for (int num = 1; num <= 6; num++) {
                    TipoAssento tipo;
                    if (fila == 'B' && (num == 1 || num == 6)) {
                        tipo = TipoAssento.PNE;
                    } else if (fila == 'C' || fila == 'D') {
                        tipo = TipoAssento.VIP;
                    } else {
                        tipo = TipoAssento.COMUM;
                    }
                    assentosSala.add(new Assento(fila, num, tipo));
                }
            }

            LocalDateTime amanha = LocalDateTime.now().plusDays(1).withHour(19).withMinute(30).withSecond(0).withNano(0);
            LocalDateTime depoisDeAmanha = LocalDateTime.now().plusDays(2).withHour(21).withMinute(0).withSecond(0).withNano(0);

            Sessao s1 = new Sessao("Interestelar (IMAX)", amanha, new BigDecimal("32.00"), assentosSala);
            Sessao s2 = new Sessao("Oppenheimer", depoisDeAmanha, new BigDecimal("35.00"), assentosSala);

            sessaoRepo.salvar(s1);
            sessaoRepo.salvar(s2);
        }
    }
}
