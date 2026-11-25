package ProjetoFinal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        Conexao.inicializarBanco();
        inicializarAdminPadrao();

        boolean executando = true;
        while (executando) {
            if (Usuario.getUsuarioLogado() == null) {
                executando = exibirTelaLogin();
            } else {
                exibirMenuPrincipal();
            }
        }
        scanner.close();
    }

    private static void inicializarAdminPadrao() {
        if (Usuario.listarUsuarios().isEmpty()) {
            Usuario.criarUsuario("Administrador", "00000000000", "admin", "admin", true, true);
        }
    }

    private static boolean exibirTelaLogin() {
        System.out.println("\n=== SISTEMA DE ESTACIONAMENTO ===");
        System.out.println("1 - Fazer Login");
        System.out.println("0 - Encerrar Sistema");
        int opcao = lerInteiro("Escolha uma opção: ");

        if (opcao == 0) {
            System.out.println("Sistema encerrado.");
            return false;
        }

        if (opcao == 1) {
            System.out.print("Login: ");
            String login = scanner.nextLine().trim();
            System.out.print("Senha: ");
            String senha = scanner.nextLine().trim();
            Usuario.autenticar(login, senha);
        } else {
            System.out.println("Opção inválida.");
        }
        return true;
    }

    private static void exibirMenuPrincipal() {
        Usuario logado = Usuario.getUsuarioLogado();
        if (logado == null) {
            return;
        }

        System.out.println("\n=== MENU PRINCIPAL (" + logado.getNome() + " - " + (logado.isAdmin() ? "Admin" : "Funcionário") + ") ===");
        System.out.println("1 - Operações de Tickets (Entrada / Saída / Caixa)");
        System.out.println("2 - Gestão de Veículos");
        System.out.println("3 - Gestão de Vagas");
        if (Usuario.isAdminLogado()) {
            System.out.println("4 - Relatório Financeiro");
            System.out.println("5 - Gestão de Usuários");
        }
        System.out.println("0 - Logout");

        int opcao = lerInteiro("Escolha uma opção: ");

        switch (opcao) {
            case 1:
                menuTickets();
                break;
            case 2:
                menuVeiculos();
                break;
            case 3:
                menuVagas();
                break;
            case 4:
                if (Usuario.isAdminLogado()) {
                    exibirRelatorioFinanceiro();
                } else {
                    System.out.println("Acesso negado. Apenas administradores podem acessar relatórios financeiros.");
                }
                break;
            case 5:
                if (Usuario.isAdminLogado()) {
                    menuUsuarios();
                } else {
                    System.out.println("Acesso negado. Apenas administradores podem gerenciar usuários.");
                }
                break;
            case 0:
                Usuario.logout();
                break;
            default:
                System.out.println("Opção inválida.");
        }
    }

    private static void menuTickets() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n--- MENU DE TICKETS E CAIXA ---");
            System.out.println("1 - Registrar Entrada (Abrir Ticket)");
            System.out.println("2 - Registrar Saída / Pagamento (Fechar Ticket)");
            System.out.println("3 - Consultar Valor Atual da Estadia");
            System.out.println("4 - Listar Tickets Ativos (ABERTO)");
            System.out.println("5 - Listar Todos os Tickets");
            System.out.println("6 - Buscar Tickets por Data");
            System.out.println("7 - Cancelar Ticket");
            System.out.println("8 - Editar Ticket");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    registrarEntrada();
                    break;
                case 2:
                    registrarSaida();
                    break;
                case 3:
                    consultarValorEstadia();
                    break;
                case 4:
                    listarTickets(Ticket.listarTicketsAtivos());
                    break;
                case 5:
                    listarTickets(Ticket.listarTickets());
                    break;
                case 6:
                    buscarTicketsPorData();
                    break;
                case 7:
                    cancelarTicket();
                    break;
                case 8:
                    editarTicket();
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private static void registrarEntrada() {
        List<Vaga> vagasDisponiveis = Vaga.listarVagasDisponiveis();
        if (vagasDisponiveis.isEmpty()) {
            System.out.println("Entrada não autorizada: não há vagas livres disponíveis.");
            return;
        }

        System.out.print("Placa do veículo: ");
        String placa = scanner.nextLine().trim();
        Veiculo veiculo = Veiculo.buscarVeiculoPorPlaca(placa);

        if (veiculo == null) {
            System.out.println("Veículo não encontrado. Iniciando cadastro rápido...");
            veiculo = cadastrarVeiculoComPlaca(placa);
            if (veiculo == null) {
                return;
            }
        }

        for (Ticket ativo : Ticket.listarTicketsAtivos()) {
            if (ativo.getVeiculo() != null && ativo.getVeiculo().getPlaca().equalsIgnoreCase(veiculo.getPlaca())) {
                System.out.println("Este veículo já possui um ticket ABERTO (ID: " + ativo.getId() + ").");
                return;
            }
        }

        System.out.println("Vagas livres disponíveis:");
        for (int i = 0; i < vagasDisponiveis.size(); i++) {
            Vaga vaga = vagasDisponiveis.get(i);
            System.out.println((i + 1) + " - Vaga Nº " + vaga.getNumero() + " (" + vaga.getStatus() + ")");
        }
        int indice = lerInteiro("Selecione o número da opção da vaga: ") - 1;
        if (indice < 0 || indice >= vagasDisponiveis.size()) {
            System.out.println("Seleção de vaga inválida.");
            return;
        }

        Vaga vagaSelecionada = vagasDisponiveis.get(indice);
        Ticket ticket = Ticket.criarTicket(veiculo, vagaSelecionada);

        if (ticket != null) {
            System.out.println("Ticket aberto com sucesso!");
            ticket.verTicket();
        }
    }

    private static void registrarSaida() {
        Ticket ticket = selecionarTicketNaLista(Ticket.listarTicketsAtivos());
        if (ticket == null) {
            return;
        }

        ticket.registrarPagamento();
        ticket.verTicket();
    }

    private static void consultarValorEstadia() {
        Ticket ticket = selecionarTicketNaLista(Ticket.listarTicketsAtivos());
        if (ticket == null) {
            return;
        }

        double valorAtual = ticket.calcularValor();
        System.out.println("Valor atual da estadia do Ticket #" + ticket.getId() + ": R$ " + String.format("%.2f", valorAtual));
    }

    private static void listarTickets(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("Nenhum ticket encontrado.");
            return;
        }
        for (Ticket ticket : tickets) {
            System.out.println("-----------------------------");
            ticket.verTicket();
        }
        System.out.println("-----------------------------");
    }

    private static void buscarTicketsPorData() {
        LocalDate data = lerData("Informe a data (dd/MM/yyyy): ");
        if (data == null) {
            return;
        }
        listarTickets(Ticket.buscarTicketsPorData(data.atStartOfDay()));
    }

    private static void cancelarTicket() {
        if (!Usuario.isAdminLogado()) {
            System.out.println("Apenas administradores podem cancelar tickets.");
            return;
        }

        Ticket ticket = selecionarTicketNaLista(Ticket.listarTicketsAtivos());
        if (ticket == null) {
            return;
        }

        ticket.cancelarTicket();
    }

    private static void editarTicket() {
        Ticket ticket = selecionarTicketNaLista(Ticket.listarTickets());
        if (ticket == null) {
            return;
        }

        LocalDateTime novaEntrada = lerDataHoraOpcional("Nova data/hora de entrada (dd/MM/yyyy HH:mm) ou Enter para manter: ");
        if (novaEntrada == null) {
            novaEntrada = ticket.getDataHoraEntrada();
        }

        System.out.print("Deseja informar/alterar data/hora de saída? (S/N): ");
        String respSaida = scanner.nextLine().trim();
        LocalDateTime novaSaida = ticket.getDataHoraSaida();
        if (respSaida.equalsIgnoreCase("S")) {
            LocalDateTime saidaInformada = lerDataHoraOpcional("Nova data/hora de saída (dd/MM/yyyy HH:mm): ");
            if (saidaInformada != null) {
                novaSaida = saidaInformada;
            }
        }

        double novoValor = lerDouble("Novo valor total: ");

        StatusTicket[] statuses = StatusTicket.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + " - " + statuses[i]);
        }
        int indiceStatus = lerInteiro("Escolha o novo status: ") - 1;
        if (indiceStatus < 0 || indiceStatus >= statuses.length) {
            System.out.println("Status inválido.");
            return;
        }

        ticket.editarTicket(novaEntrada, novaSaida, novoValor, statuses[indiceStatus]);
        System.out.println("Ticket atualizado com sucesso.");
    }

    private static void exibirRelatorioFinanceiro() {
        System.out.println("\n--- RELATÓRIO FINANCEIRO ---");
        LocalDate dataInicio = lerData("Data inicial (dd/MM/yyyy): ");
        LocalDate dataFim = lerData("Data final (dd/MM/yyyy): ");

        if (dataInicio == null || dataFim == null) {
            return;
        }

        if (dataFim.isBefore(dataInicio)) {
            System.out.println("A data final não pode ser anterior à data inicial.");
            return;
        }

        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);

        double totalArrecadado = Ticket.gerarRelatorioFinanceiro(inicio, fim);

        System.out.println("Período: " + dataInicio.format(FORMATO_DATA) + " até " + dataFim.format(FORMATO_DATA));
        System.out.println("Total arrecadado (tickets pagos): R$ " + String.format("%.2f", totalArrecadado));
    }

    private static void menuVeiculos() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n--- GESTÃO DE VEÍCULOS ---");
            System.out.println("1 - Cadastrar Veículo");
            System.out.println("2 - Listar Veículos");
            System.out.println("3 - Buscar Veículo por Placa");
            System.out.println("4 - Editar Veículo");
            System.out.println("5 - Excluir Veículo");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    System.out.print("Placa: ");
                    String placa = scanner.nextLine().trim();
                    cadastrarVeiculoComPlaca(placa);
                    break;
                case 2:
                    List<Veiculo> veiculos = Veiculo.listarVeiculos();
                    if (veiculos.isEmpty()) {
                        System.out.println("Nenhum veículo cadastrado.");
                    } else {
                        for (Veiculo v : veiculos) {
                            v.verVeiculo();
                        }
                    }
                    break;
                case 3:
                    System.out.print("Placa para busca: ");
                    Veiculo encontrado = Veiculo.buscarVeiculoPorPlaca(scanner.nextLine());
                    if (encontrado != null) {
                        encontrado.verVeiculo();
                    } else {
                        System.out.println("Veículo não encontrado.");
                    }
                    break;
                case 4:
                    System.out.print("Placa do veículo a editar: ");
                    Veiculo vEditar = Veiculo.buscarVeiculoPorPlaca(scanner.nextLine());
                    if (vEditar == null) {
                        System.out.println("Veículo não encontrado.");
                    } else {
                        System.out.print("Novo modelo: ");
                        String modelo = scanner.nextLine();
                        System.out.print("Nova cor: ");
                        String cor = scanner.nextLine();
                        TipoVeiculo novoTipo = selecionarTipoVeiculo();
                        if (novoTipo == null) {
                            break;
                        }
                        try {
                            vEditar.editarVeiculo(vEditar.getPlaca(), modelo, cor, novoTipo);
                            System.out.println("Veículo atualizado.");
                        } catch (IllegalArgumentException e) {
                            System.out.println("Erro: " + e.getMessage());
                        }
                    }
                    break;
                case 5:
                    System.out.print("Placa do veículo a excluir: ");
                    Veiculo vExcluir = Veiculo.buscarVeiculoPorPlaca(scanner.nextLine());
                    if (vExcluir == null) {
                        System.out.println("Veículo não encontrado.");
                    } else if (possuiTicketAtivo(vExcluir.getPlaca())) {
                        System.out.println("Não é possível excluir um veículo com ticket ABERTO.");
                    } else {
                        Veiculo.excluirVeiculo(vExcluir);
                        System.out.println("Veículo excluído.");
                    }
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private static boolean possuiTicketAtivo(String placa) {
        for (Ticket ativo : Ticket.listarTicketsAtivos()) {
            if (ativo.getVeiculo() != null && ativo.getVeiculo().getPlaca().equalsIgnoreCase(placa)) {
                return true;
            }
        }
        return false;
    }

    private static Veiculo cadastrarVeiculoComPlaca(String placa) {
        System.out.print("Modelo: ");
        String modelo = scanner.nextLine();
        System.out.print("Cor: ");
        String cor = scanner.nextLine();

        TipoVeiculo tipo = selecionarTipoVeiculo();
        if (tipo == null) {
            return null;
        }

        try {
            Veiculo novo = Veiculo.criarVeiculo(placa, modelo, cor, tipo);
            if (novo != null) {
                System.out.println("Veículo cadastrado com sucesso:");
                novo.verVeiculo();
            }
            return novo;
        } catch (IllegalArgumentException e) {
            System.out.println("Erro ao cadastrar veículo: " + e.getMessage());
            return null;
        }
    }

    private static TipoVeiculo selecionarTipoVeiculo() {
        TipoVeiculo[] tipos = TipoVeiculo.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println((i + 1) + " - " + tipos[i]);
        }
        int indiceTipo = lerInteiro("Selecione o tipo do veículo: ") - 1;
        if (indiceTipo < 0 || indiceTipo >= tipos.length) {
            System.out.println("Tipo de veículo inválido.");
            return null;
        }
        return tipos[indiceTipo];
    }

    private static void menuVagas() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n--- GESTÃO DE VAGAS ---");
            System.out.println("1 - Cadastrar Vaga");
            System.out.println("2 - Listar Vagas Disponíveis");
            System.out.println("3 - Listar Todas as Vagas");
            System.out.println("4 - Alterar Status de Vaga");
            System.out.println("5 - Excluir Vaga");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    int numero = lerInteiro("Número da vaga: ");
                    StatusVaga statusInicial = selecionarStatusVaga();
                    if (statusInicial == null) {
                        break;
                    }
                    Vaga criada = Vaga.criarVaga(numero, statusInicial);
                    if (criada != null) {
                        System.out.println("Vaga cadastrada com sucesso:");
                        criada.verVaga();
                    }
                    break;
                case 2:
                    List<Vaga> livres = Vaga.listarVagasDisponiveis();
                    if (livres.isEmpty()) {
                        System.out.println("Nenhuma vaga livre.");
                    } else {
                        for (Vaga vaga : livres) {
                            vaga.verVaga();
                        }
                    }
                    break;
                case 3:
                    List<Vaga> todas = Vaga.listarVagas();
                    if (todas.isEmpty()) {
                        System.out.println("Nenhuma vaga cadastrada.");
                    } else {
                        for (Vaga vaga : todas) {
                            vaga.verVaga();
                        }
                    }
                    break;
                case 4:
                    Vaga vagaEditar = selecionarVagaNaLista(Vaga.listarVagas());
                    if (vagaEditar == null) {
                        break;
                    }
                    StatusVaga novoStatus = selecionarStatusVaga();
                    if (novoStatus == null) {
                        break;
                    }
                    vagaEditar.editarVaga(vagaEditar.getNumero(), novoStatus);
                    System.out.println("Status da vaga atualizado.");
                    break;
                case 5:
                    Vaga vagaExcluir = selecionarVagaNaLista(Vaga.listarVagas());
                    if (vagaExcluir == null) {
                        break;
                    }
                    if (vagaExcluir.getStatus() == StatusVaga.OCUPADA) {
                        System.out.println("Não é possível excluir uma vaga que está OCUPADA.");
                        break;
                    }
                    Vaga.excluirVaga(vagaExcluir);
                    System.out.println("Vaga excluída.");
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private static Vaga selecionarVagaNaLista(List<Vaga> vagas) {
        if (vagas.isEmpty()) {
            System.out.println("Nenhuma vaga cadastrada.");
            return null;
        }
        for (int i = 0; i < vagas.size(); i++) {
            Vaga vaga = vagas.get(i);
            System.out.println((i + 1) + " - Vaga Nº " + vaga.getNumero() + " (" + vaga.getStatus() + ")");
        }
        int indice = lerInteiro("Selecione a vaga: ") - 1;
        if (indice < 0 || indice >= vagas.size()) {
            System.out.println("Seleção inválida.");
            return null;
        }
        return vagas.get(indice);
    }

    private static StatusVaga selecionarStatusVaga() {
        StatusVaga[] statuses = StatusVaga.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + " - " + statuses[i]);
        }
        int indiceStatus = lerInteiro("Selecione o status da vaga: ") - 1;
        if (indiceStatus < 0 || indiceStatus >= statuses.length) {
            System.out.println("Status inválido.");
            return null;
        }
        return statuses[indiceStatus];
    }

    private static void menuUsuarios() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n--- GESTÃO DE USUÁRIOS ---");
            System.out.println("1 - Cadastrar Usuário");
            System.out.println("2 - Listar Usuários");
            System.out.println("3 - Editar Usuário");
            System.out.println("4 - Desativar Usuário");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    System.out.print("Nome: ");
                    String nome = scanner.nextLine();
                    System.out.print("CPF: ");
                    String cpf = scanner.nextLine();
                    System.out.print("Login: ");
                    String login = scanner.nextLine();
                    System.out.print("Senha: ");
                    String senha = scanner.nextLine();
                    System.out.print("É administrador? (S/N): ");
                    boolean admin = scanner.nextLine().trim().equalsIgnoreCase("S");
                    Usuario criado = Usuario.criarUsuario(nome, cpf, login, senha, true, admin);
                    if (criado != null) {
                        System.out.println("Usuário cadastrado com sucesso.");
                    }
                    break;
                case 2:
                    List<Usuario> usuarios = Usuario.listarUsuarios();
                    if (usuarios.isEmpty()) {
                        System.out.println("Nenhum usuário cadastrado.");
                    } else {
                        for (Usuario u : usuarios) {
                            System.out.println("-----------------------------");
                            u.verUsuario();
                        }
                        System.out.println("-----------------------------");
                    }
                    break;
                case 3:
                    System.out.print("Login do usuário a editar: ");
                    Usuario uEditar = buscarUsuarioPorLogin(scanner.nextLine().trim());
                    if (uEditar == null) {
                        System.out.println("Usuário não encontrado.");
                    } else {
                        System.out.print("Novo nome: ");
                        String novoNome = scanner.nextLine();
                        System.out.print("Novo CPF: ");
                        String novoCpf = scanner.nextLine();
                        System.out.print("Novo login: ");
                        String novoLogin = scanner.nextLine();
                        System.out.print("Nova senha: ");
                        String novaSenha = scanner.nextLine();
                        System.out.print("Ativo? (S/N): ");
                        boolean novoAtivo = scanner.nextLine().trim().equalsIgnoreCase("S");
                        System.out.print("Administrador? (S/N): ");
                        boolean novoAdmin = scanner.nextLine().trim().equalsIgnoreCase("S");
                        uEditar.editarUsuario(novoNome, novoCpf, novoLogin, novaSenha, novoAtivo, novoAdmin);
                        System.out.println("Usuário atualizado com sucesso.");
                    }
                    break;
                case 4:
                    System.out.print("Login do usuário a desativar: ");
                    Usuario uDesativar = buscarUsuarioPorLogin(scanner.nextLine().trim());
                    if (uDesativar == null) {
                        System.out.println("Usuário não encontrado.");
                    } else {
                        uDesativar.desativarUsuario();
                        System.out.println("Usuário desativado.");
                    }
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
    }

    private static Usuario buscarUsuarioPorLogin(String login) {
        for (Usuario u : Usuario.listarUsuarios()) {
            if (u.getLogin().equals(login)) {
                return u;
            }
        }
        return null;
    }

    private static Ticket selecionarTicketNaLista(List<Ticket> tickets) {
        if (tickets.isEmpty()) {
            System.out.println("Nenhum ticket disponível.");
            return null;
        }
        listarTickets(tickets);
        int id = lerInteiro("Informe o ID do ticket: ");
        for (Ticket ticket : tickets) {
            if (ticket.getId() == id) {
                return ticket;
            }
        }
        System.out.println("Ticket não encontrado.");
        return null;
    }

    private static int lerInteiro(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Digite um número inteiro válido.");
            }
        }
    }

    private static double lerDouble(String mensagem) {
        while (true) {
            System.out.print(mensagem);
            try {
                return Double.parseDouble(scanner.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                System.out.println("Digite um valor numérico válido.");
            }
        }
    }

    private static LocalDate lerData(String mensagem) {
        System.out.print(mensagem);
        try {
            return LocalDate.parse(scanner.nextLine().trim(), FORMATO_DATA);
        } catch (DateTimeParseException e) {
            System.out.println("Data inválida. Use o formato dd/MM/yyyy.");
            return null;
        }
    }

    private static LocalDateTime lerDataHoraOpcional(String mensagem) {
        System.out.print(mensagem);
        String entrada = scanner.nextLine().trim();
        if (entrada.isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(entrada, FORMATO_DATA_HORA);
        } catch (DateTimeParseException e) {
            System.out.println("Data/hora inválida. Use o formato dd/MM/yyyy HH:mm.");
            return null;
        }
    }
}