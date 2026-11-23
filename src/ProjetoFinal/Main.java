package ProjetoFinal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static Usuario usuarioLogado = null;

    public static void main(String[] args) {
        inicializarAdminPadrao();

        boolean executando = true;
        while (executando) {
            if (usuarioLogado == null) {
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

            if (Usuario.autenticar(login, senha)) {
                usuarioLogado = buscarUsuarioPorLogin(login);
                System.out.println("Bem-vindo(a), " + usuarioLogado.getNome() + "!");
            } else {
                System.out.println("Login, senha inválidos ou usuário desativado.");
            }
        } else {
            System.out.println("Opção inválida.");
        }
        return true;
    }

    private static void exibirMenuPrincipal() {
        System.out.println("\n=== MENU PRINCIPAL (" + usuarioLogado.getNome() + " - " + (usuarioLogado.isAdmin() ? "Admin" : "Funcionário") + ") ===");
        System.out.println("1 - Operações de Tickets (Entrada / Saída / Caixa)");
        System.out.println("2 - Gestão de Veículos");
        System.out.println("3 - Gestão de Vagas");
        if (usuarioLogado.isAdmin()) {
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
                if (usuarioLogado.isAdmin()) {
                    exibirRelatorioFinanceiro();
                } else {
                    System.out.println("Acesso negado. Apenas administradores podem acessar relatórios financeiros.");
                }
                break;
            case 5:
                if (usuarioLogado.isAdmin()) {
                    menuUsuarios();
                } else {
                    System.out.println("Acesso negado. Apenas administradores podem gerenciar usuários.");
                }
                break;
            case 0:
                Usuario.logout();
                usuarioLogado = null;
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
        List<Vaga> vagasLivres = Vaga.listarVagasLivres();
        if (vagasLivres == null || vagasLivres.isEmpty()) {
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
        for (int i = 0; i < vagasLivres.size(); i++) {
            System.out.println((i + 1) + " - " + vagasLivres.get(i));
        }
        int indice = lerInteiro("Selecione o número da opção da vaga: ") - 1;
        if (indice < 0 || indice >= vagasLivres.size()) {
            System.out.println("Seleção de vaga inválida.");
            return;
        }

        Vaga vagaSelecionada = vagasLivres.get(indice);
        vagaSelecionada.setStatus(StatusVaga.OCUPADA);
        Ticket ticket = Ticket.criarTicket(veiculo, vagaSelecionada);

        System.out.println("Ticket aberto com sucesso!");
        ticket.verTicket();
    }

    private static void registrarSaida() {
        Ticket ticket = selecionarTicketNaLista(Ticket.listarTicketsAtivos());
        if (ticket == null) {
            return;
        }

        ticket.registrarPagamento();
        if (ticket.getVaga() != null) {
            ticket.getVaga().setStatus(StatusVaga.LIVRE);
        }

        System.out.println("Pagamento registrado e saída liberada!");
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
        Date data = lerData("Informe a data (dd/MM/yyyy): ");
        if (data == null) {
            return;
        }
        listarTickets(Ticket.buscarTicketsPorData(data));
    }

    private static void cancelarTicket() {
        Ticket ticket = selecionarTicketNaLista(Ticket.listarTicketsAtivos());
        if (ticket == null) {
            return;
        }

        ticket.cancelarTicket();
        if (ticket.getVaga() != null) {
            ticket.getVaga().setStatus(StatusVaga.LIVRE);
        }
        System.out.println("Ticket #" + ticket.getId() + " cancelado com sucesso.");
    }

    private static void editarTicket() {
        Ticket ticket = selecionarTicketNaLista(Ticket.listarTickets());
        if (ticket == null) {
            return;
        }

        Date novaEntrada = lerDataHora("Nova data/hora de entrada (dd/MM/yyyy HH:mm): ");
        if (novaEntrada == null) {
            novaEntrada = ticket.getDataHoraEntrada();
        }

        System.out.print("Deseja informar data/hora de saída? (S/N): ");
        String respSaida = scanner.nextLine().trim();
        Date novaSaida = ticket.getDataHoraSaida();
        if (respSaida.equalsIgnoreCase("S")) {
            novaSaida = lerDataHora("Nova data/hora de saída (dd/MM/yyyy HH:mm): ");
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
        Date dataInicio = lerData("Data inicial (dd/MM/yyyy): ");
        Date dataFim = lerData("Data final (dd/MM/yyyy): ");

        if (dataInicio == null || dataFim == null) {
            return;
        }

        Calendar calFim = Calendar.getInstance();
        calFim.setTime(dataFim);
        calFim.set(Calendar.HOUR_OF_DAY, 23);
        calFim.set(Calendar.MINUTE, 59);
        calFim.set(Calendar.SECOND, 59);
        calFim.set(Calendar.MILLISECOND, 999);
        dataFim = calFim.getTime();

        List<Ticket> todos = Ticket.listarTickets();
        double totalArrecadado = 0.0;
        if (!todos.isEmpty()) {
            totalArrecadado = todos.get(0).gerarRelatorioFinanceiro(dataInicio, dataFim);
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        System.out.println("Período: " + formato.format(dataInicio) + " até " + formato.format(dataFim));
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
                            System.out.println(v.verVeiculo());
                        }
                    }
                    break;
                case 3:
                    System.out.print("Placa para busca: ");
                    Veiculo encontrado = Veiculo.buscarVeiculoPorPlaca(scanner.nextLine());
                    if (encontrado != null) {
                        System.out.println(encontrado.verVeiculo());
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
                        try {
                            vEditar.editarVeiculo(modelo, cor);
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
                    } else {
                        vExcluir.excluirVeiculo();
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

    private static Veiculo cadastrarVeiculoComPlaca(String placa) {
        System.out.print("Modelo: ");
        String modelo = scanner.nextLine();
        System.out.print("Cor: ");
        String cor = scanner.nextLine();

        TipoVeiculo[] tipos = TipoVeiculo.values();
        for (int i = 0; i < tipos.length; i++) {
            System.out.println((i + 1) + " - " + tipos[i]);
        }
        int indiceTipo = lerInteiro("Selecione o tipo do veículo: ") - 1;
        if (indiceTipo < 0 || indiceTipo >= tipos.length) {
            System.out.println("Tipo de veículo inválido.");
            return null;
        }

        try {
            Veiculo novo = Veiculo.criarVeiculo(placa, modelo, cor, tipos[indiceTipo]);
            System.out.println("Veículo cadastrado: " + novo.verVeiculo());
            return novo;
        } catch (IllegalArgumentException e) {
            System.out.println("Erro ao cadastrar veículo: " + e.getMessage());
            return null;
        }
    }

    private static void menuVagas() {
        int opcao = -1;
        while (opcao != 0) {
            System.out.println("\n--- GESTÃO DE VAGAS ---");
            System.out.println("1 - Listar Vagas Livres");
            System.out.println("2 - Listar Todas as Vagas");
            System.out.println("3 - Alterar Status de Vaga");
            System.out.println("0 - Voltar");

            opcao = lerInteiro("Escolha uma opção: ");

            switch (opcao) {
                case 1:
                    List<Vaga> livres = Vaga.listarVagasLivres();
                    if (livres == null || livres.isEmpty()) {
                        System.out.println("Nenhuma vaga livre.");
                    } else {
                        for (Vaga vaga : livres) {
                            System.out.println(vaga);
                        }
                    }
                    break;
                case 2:
                    List<Vaga> todas = Vaga.listarVagas();
                    if (todas == null || todas.isEmpty()) {
                        System.out.println("Nenhuma vaga cadastrada.");
                    } else {
                        for (Vaga vaga : todas) {
                            System.out.println(vaga);
                        }
                    }
                    break;
                case 3:
                    List<Vaga> lista = Vaga.listarVagas();
                    if (lista == null || lista.isEmpty()) {
                        System.out.println("Nenhuma vaga cadastrada.");
                        break;
                    }
                    for (int i = 0; i < lista.size(); i++) {
                        System.out.println((i + 1) + " - " + lista.get(i));
                    }
                    int indiceVaga = lerInteiro("Selecione a vaga: ") - 1;
                    if (indiceVaga < 0 || indiceVaga >= lista.size()) {
                        System.out.println("Seleção inválida.");
                        break;
                    }
                    StatusVaga[] statuses = StatusVaga.values();
                    for (int i = 0; i < statuses.length; i++) {
                        System.out.println((i + 1) + " - " + statuses[i]);
                    }
                    int indiceStatus = lerInteiro("Novo status: ") - 1;
                    if (indiceStatus < 0 || indiceStatus >= statuses.length) {
                        System.out.println("Status inválido.");
                        break;
                    }
                    lista.get(indiceVaga).setStatus(statuses[indiceStatus]);
                    System.out.println("Status da vaga atualizado.");
                    break;
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida.");
            }
        }
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
                    Usuario.criarUsuario(nome, cpf, login, senha, true, admin);
                    System.out.println("Usuário cadastrado com sucesso.");
                    break;
                case 2:
                    for (Usuario u : Usuario.listarUsuarios()) {
                        System.out.println("-----------------------------");
                        u.verUsuario();
                    }
                    System.out.println("-----------------------------");
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

    private static Date lerData(String mensagem) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
        formato.setLenient(false);
        System.out.print(mensagem);
        try {
            return formato.parse(scanner.nextLine().trim());
        } catch (ParseException e) {
            System.out.println("Data inválida. Use o formato dd/MM/yyyy.");
            return null;
        }
    }

    private static Date lerDataHora(String mensagem) {
        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        formato.setLenient(false);
        System.out.print(mensagem);
        try {
            return formato.parse(scanner.nextLine().trim());
        } catch (ParseException e) {
            System.out.println("Data/hora inválida. Use o formato dd/MM/yyyy HH:mm.");
            return null;
        }
    }
}