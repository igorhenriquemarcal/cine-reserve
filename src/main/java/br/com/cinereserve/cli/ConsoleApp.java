package br.com.cinereserve.cli;

import br.com.cinereserve.exception.RegraDeNegocioException;
import br.com.cinereserve.model.Cliente;
import br.com.cinereserve.model.Reserva;
import br.com.cinereserve.model.Sessao;
import br.com.cinereserve.repository.ClienteRepository;
import br.com.cinereserve.repository.SessaoRepository;
import br.com.cinereserve.service.ReservaService;

import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Interface interativa de linha de comando (CLI) para os usuários do CineReserve.
 */
public class ConsoleApp {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClienteRepository clienteRepository;
    private final SessaoRepository sessaoRepository;
    private final ReservaService reservaService;
    private final Scanner scanner;

    public ConsoleApp(ClienteRepository clienteRepository,
                      SessaoRepository sessaoRepository,
                      ReservaService reservaService) {
        this.clienteRepository = clienteRepository;
        this.sessaoRepository = sessaoRepository;
        this.reservaService = reservaService;
        this.scanner = new Scanner(System.in);
    }

    public void iniciar() {
        boolean rodando = true;
        while (rodando) {
            exibirMenuPrincipal();
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> listarSessoesEVisualizarMapa();
                case "2" -> gerenciarClientes();
                case "3" -> realizarNovaReserva();
                case "4" -> consultarReservasCliente();
                case "5" -> cancelarReserva();
                case "0" -> {
                    System.out.println("\nObrigado por utilizar o CineReserve! Até logo!");
                    rodando = false;
                }
                default -> System.out.println("\n[!] Opção inválida. Escolha uma opção do menu.");
            }
        }
    }

    private void exibirMenuPrincipal() {
        System.out.println("========================================================");
        System.out.println("                   CINERESERVE - MENU                   ");
        System.out.println("========================================================");
        System.out.println("  1. Listar Sessões e Ver Mapa de Assentos");
        System.out.println("  2. Cadastrar ou Buscar Cliente");
        System.out.println("  3. Realizar Reserva de Assento(s)");
        System.out.println("  4. Consultar Histórico de Reservas por Cliente");
        System.out.println("  5. Cancelar Reserva");
        System.out.println("  0. Sair");
        System.out.print("Escolha uma opção: ");
    }

    private void listarSessoesEVisualizarMapa() {
        List<Sessao> sessoes = sessaoRepository.buscarTodas();
        if (sessoes.isEmpty()) {
            System.out.println("\n[!] Nenhuma sessão cadastrada no momento.\n");
            return;
        }

        System.out.println("\n--- SESSÕES DISPONÍVEIS ---");
        for (int i = 0; i < sessoes.size(); i++) {
            Sessao s = sessoes.get(i);
            System.out.printf("[%d] %s | Horário: %s | Preço Base: R$ %.2f | Vagas: %d/%d\n",
                    (i + 1),
                    s.getTituloFilme(),
                    s.getHorarioInicio().format(FORMATO_DATA),
                    s.getPrecoBase(),
                    s.getAssentosDisponiveisCount(),
                    s.getTotalAssentos());
        }

        System.out.print("\nDigite o número da sessão para ver o mapa detalhado (ou 0 para voltar): ");
        try {
            int num = Integer.parseInt(scanner.nextLine().trim());
            if (num > 0 && num <= sessoes.size()) {
                Sessao escolhida = sessoes.get(num - 1);
                MapaAssentosVisualizer.exibirMapa(escolhida);
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Entrada inválida.");
        }
    }

    private void gerenciarClientes() {
        System.out.println("\n--- GERENCIAR CLIENTES ---");
        System.out.println("1. Cadastrar Novo Cliente");
        System.out.println("2. Buscar Cliente por CPF");
        System.out.println("3. Listar Todos os Clientes");
        System.out.print("Escolha: ");
        String sub = scanner.nextLine().trim();

        switch (sub) {
            case "1" -> cadastrarNovoCliente();
            case "2" -> {
                System.out.print("Digite o CPF (ex: 123.456.789-00): ");
                String cpf = scanner.nextLine().trim();
                clienteRepository.buscarPorCpf(cpf).ifPresentOrElse(
                        c -> System.out.printf("\n[✓] Cliente encontrado: %s | E-mail: %s | ID: %s\n\n", c.getNome(), c.getEmail(), c.getId()),
                        () -> System.out.println("\n[!] Nenhum cliente encontrado com esse CPF.\n")
                );
            }
            case "3" -> {
                List<Cliente> clientes = clienteRepository.buscarTodos();
                if (clientes.isEmpty()) {
                    System.out.println("\n[!] Nenhum cliente cadastrado.\n");
                } else {
                    System.out.println("\n--- LISTA DE CLIENTES ---");
                    for (Cliente c : clientes) {
                        System.out.printf("- %s | CPF: %s | E-mail: %s (ID: %s)\n", c.getNome(), c.getCpf(), c.getEmail(), c.getId());
                    }
                    System.out.println();
                }
            }
            default -> System.out.println("[!] Opção inválida.");
        }
    }

    private Cliente cadastrarNovoCliente() {
        System.out.print("Nome completo: ");
        String nome = scanner.nextLine().trim();
        System.out.print("E-mail: ");
        String email = scanner.nextLine().trim();
        System.out.print("CPF: ");
        String cpf = scanner.nextLine().trim();

        try {
            Optional<Cliente> existente = clienteRepository.buscarPorCpf(cpf);
            if (existente.isPresent()) {
                System.out.println("\n[!] Já existe um cliente cadastrado com este CPF.\n");
                return existente.get();
            }

            Cliente novo = new Cliente(nome, email, cpf);
            clienteRepository.salvar(novo);
            System.out.printf("\n[✓] Cliente '%s' cadastrado com sucesso! ID: %s\n\n", novo.getNome(), novo.getId());
            return novo;
        } catch (IllegalArgumentException e) {
            System.out.println("\n[Erro de validação] " + e.getMessage() + "\n");
            return null;
        }
    }

    private void realizarNovaReserva() {
        System.out.println("\n--- REALIZAR RESERVA ---");

        System.out.print("Informe o CPF do cliente: ");
        String cpf = scanner.nextLine().trim();
        Optional<Cliente> clienteOpt = clienteRepository.buscarPorCpf(cpf);

        Cliente cliente;
        if (clienteOpt.isEmpty()) {
            System.out.println("Cliente não encontrado. Vamos cadastrá-lo agora:");
            cliente = cadastrarNovoCliente();
            if (cliente == null) return;
        } else {
            cliente = clienteOpt.get();
            System.out.printf("Cliente identificado: %s\n", cliente.getNome());
        }

        List<Sessao> sessoes = sessaoRepository.buscarTodas();
        if (sessoes.isEmpty()) {
            System.out.println("[!] Não há sessões disponíveis.");
            return;
        }

        System.out.println("\nEscolha a sessão:");
        for (int i = 0; i < sessoes.size(); i++) {
            Sessao s = sessoes.get(i);
            System.out.printf("[%d] %s (%s) - R$ %.2f\n", (i + 1), s.getTituloFilme(), s.getHorarioInicio().format(FORMATO_DATA), s.getPrecoBase());
        }
        System.out.print("Número da sessão: ");
        int numSessao;
        try {
            numSessao = Integer.parseInt(scanner.nextLine().trim());
            if (numSessao < 1 || numSessao > sessoes.size()) {
                System.out.println("[!] Sessão inválida.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Número inválido.");
            return;
        }

        Sessao sessao = sessoes.get(numSessao - 1);
        MapaAssentosVisualizer.exibirMapa(sessao);

        System.out.print("Digite os códigos dos assentos separados por vírgula (ex: A1, A2, B3): ");
        String linhaAssentos = scanner.nextLine().trim();
        if (linhaAssentos.isEmpty()) {
            System.out.println("[!] Nenhum assento selecionado.");
            return;
        }

        List<String> codigos = Arrays.stream(linhaAssentos.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        try {
            Reserva reserva = reservaService.realizarReserva(cliente.getId(), sessao.getId(), codigos);
            System.out.println("\n========================================================");
            System.out.println("             [✓] RESERVA CONFIRMADA COM SUCESSO!         ");
            System.out.println("========================================================");
            System.out.printf("Código Localizador da Reserva : %s\n", reserva.getId());
            System.out.printf("Cliente                       : %s\n", cliente.getNome());
            System.out.printf("Filme                         : %s\n", sessao.getTituloFilme());
            System.out.printf("Horário                       : %s\n", sessao.getHorarioInicio().format(FORMATO_DATA));
            System.out.printf("Assentos                      : %s\n", codigos);
            System.out.printf("Valor Total                   : R$ %.2f\n", reserva.getValorTotal());
            System.out.println("========================================================\n");
        } catch (RegraDeNegocioException e) {
            System.out.println("\n[!] Falha na reserva: " + e.getMessage() + "\n");
        }
    }

    private void consultarReservasCliente() {
        System.out.print("\nDigite o CPF do cliente: ");
        String cpf = scanner.nextLine().trim();

        Optional<Cliente> clienteOpt = clienteRepository.buscarPorCpf(cpf);
        if (clienteOpt.isEmpty()) {
            System.out.println("[!] Cliente não localizado com este CPF.\n");
            return;
        }

        Cliente c = clienteOpt.get();
        List<Reserva> reservas = reservaService.buscarReservasPorCliente(c.getId());
        if (reservas.isEmpty()) {
            System.out.printf("\nNenhuma reserva encontrada para o cliente %s.\n\n", c.getNome());
            return;
        }

        System.out.printf("\n--- HISTÓRICO DE RESERVAS DE %s ---\n", c.getNome().toUpperCase());
        for (Reserva r : reservas) {
            System.out.printf("Localizador: %s | Status: %s | Filme: %s | Assentos: %s | Total: R$ %.2f\n",
                    r.getId(),
                    r.getStatus(),
                    r.getSessao().getTituloFilme(),
                    r.getAssentos().stream().map(a -> a.getCodigo()).toList(),
                    r.getValorTotal());
        }
        System.out.println();
    }

    private void cancelarReserva() {
        System.out.print("\nInforme o Localizador/ID da reserva a cancelar: ");
        String id = scanner.nextLine().trim();

        try {
            reservaService.cancelarReserva(id);
            System.out.println("\n[✓] Reserva cancelada com sucesso! Os assentos foram liberados para a sessão.\n");
        } catch (RegraDeNegocioException e) {
            System.out.println("\n[!] Falha ao cancelar: " + e.getMessage() + "\n");
        }
    }
}
