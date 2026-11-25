package ProjetoFinal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Ticket {
    private int id;
    private Veiculo veiculo;
    private Vaga vaga;
    private LocalDateTime dataHoraEntrada;
    private LocalDateTime dataHoraSaida;
    private double valorTotal;
    private StatusTicket status;

    private static final DateTimeFormatter FORMATADOR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public Ticket(int id, Veiculo veiculo, Vaga vaga, LocalDateTime dataHoraEntrada, LocalDateTime dataHoraSaida, double valorTotal, StatusTicket status) {
        this.id = id;
        this.veiculo = veiculo;
        this.vaga = vaga;
        this.dataHoraEntrada = dataHoraEntrada;
        this.dataHoraSaida = dataHoraSaida;
        this.valorTotal = valorTotal;
        this.status = status;
    }

    public Ticket(Veiculo veiculo, Vaga vaga) {
        this(0, veiculo, vaga, LocalDateTime.now(), null, 0.0, StatusTicket.ABERTO);
    }
    
    public void setId(int id) {this.id = id;}
    public void setVeiculo(Veiculo veiculo) {this.veiculo = veiculo;}
    public void setVaga(Vaga vaga) {this.vaga = vaga;}
    public void setDataHoraEntrada(LocalDateTime dataHoraEntrada) {this.dataHoraEntrada = dataHoraEntrada;}
    public void setDataHoraSaida(LocalDateTime dataHoraSaida) {this.dataHoraSaida = dataHoraSaida;}
    public void setValorTotal(double valorTotal) {this.valorTotal = valorTotal;}
    public void setStatus(StatusTicket status) {this.status = status;}
    
    public int getId() {return id;}
    public Veiculo getVeiculo() {return veiculo;}
    public Vaga getVaga() {return vaga;}
    public LocalDateTime getDataHoraEntrada() {return dataHoraEntrada;}
    public LocalDateTime getDataHoraSaida() {return dataHoraSaida;}
    public double getValorTotal() {return valorTotal;}
    public StatusTicket getStatus() {return status;}
             
    public void verTicket() {
        System.out.println("ID Ticket: " + this.id);
        System.out.println("Veiculo: " + (this.veiculo != null ? this.veiculo.getPlaca() + " (" + this.veiculo.getModelo() + ")" : "N/A"));
        System.out.println("Vaga Nº: " + (this.vaga != null ? this.vaga.getNumero() : "N/A"));
        System.out.println("Entrada: " + (this.dataHoraEntrada != null ? this.dataHoraEntrada.format(FORMATADOR) : "N/A"));
        System.out.println("Saida: " + (this.dataHoraSaida != null ? this.dataHoraSaida.format(FORMATADOR) : "Em aberto"));
        System.out.println("Valor Total: R$ " + String.format("%.2f", this.valorTotal));
        System.out.println("Status: " + this.status);
    }

    public static Ticket criarTicket(Veiculo veiculo, Vaga vaga) {
        if (veiculo == null || vaga == null) {
            System.out.println("Erro: Veiculo e Vaga sao obrigatorios para emissao do ticket.");
            return null;
        }

        if (vaga.getStatus() != StatusVaga.LIVRE) {
            System.out.println("Erro: A vaga informada nao esta livre.");
            return null;
        }

        LocalDateTime entrada = LocalDateTime.now();
        String sql = "INSERT INTO ticket (placaVeiculo, numeroVaga, dataHoraEntrada, dataHoraSaida, valorTotal, status) VALUES (?, ?, ?, NULL, 0.0, ?)";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, veiculo.getPlaca());
            stmt.setInt(2, vaga.getNumero());
            stmt.setString(3, entrada.toString());
            stmt.setString(4, StatusTicket.ABERTO.name());

            stmt.executeUpdate();

            int idGerado = 0;
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    idGerado = rs.getInt(1);
                }
            }

            vaga.editarVaga(vaga.getNumero(), StatusVaga.OCUPADA);

            return new Ticket(idGerado, veiculo, vaga, entrada, null, 0.0, StatusTicket.ABERTO);

        } catch (SQLException e) {
            System.out.println("Erro ao salvar ticket no banco: " + e.getMessage());
            return null;
        }
    }

    public void editarTicket(LocalDateTime dataHoraEntrada, LocalDateTime dataHoraSaida, double valorTotal, StatusTicket status) {
        String sql = "UPDATE ticket SET dataHoraEntrada = ?, dataHoraSaida = ?, valorTotal = ?, status = ? WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dataHoraEntrada != null ? dataHoraEntrada.toString() : null);
            stmt.setString(2, dataHoraSaida != null ? dataHoraSaida.toString() : null);
            stmt.setDouble(3, valorTotal);
            stmt.setString(4, status != null ? status.name() : this.status.name());
            stmt.setInt(5, this.id);

            stmt.executeUpdate();

            this.dataHoraEntrada = dataHoraEntrada;
            this.dataHoraSaida = dataHoraSaida;
            this.valorTotal = valorTotal;
            if(status != null){
                this.status = status;
            }

        } catch (SQLException e) {
            System.out.println("Erro ao editar ticket no banco: " + e.getMessage());
        }
    }

    public double calcularValor() {
        LocalDateTime saida = (this.dataHoraSaida != null) ? this.dataHoraSaida : LocalDateTime.now();
        Duration duracao = Duration.between(this.dataHoraEntrada, saida);
        long horas = duracao.toHours();

        if (duracao.toMinutesPart() > 0 || horas == 0) {
            horas++;
        }

        this.valorTotal = horas * 5.0;
        return this.valorTotal;
    }

    public void registrarPagamento() {
        if (this.status != StatusTicket.ABERTO) {
            System.out.println("Este ticket ja foi encerrado ou cancelado.");
            return;
        }

        this.dataHoraSaida = LocalDateTime.now();
        calcularValor();
        this.status = StatusTicket.PAGO;

        String sql = "UPDATE ticket SET dataHoraSaida = ?, valorTotal = ?, status = ? WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, this.dataHoraSaida.toString());
            stmt.setDouble(2, this.valorTotal);
            stmt.setString(3, this.status.name());
            stmt.setInt(4, this.id);

            stmt.executeUpdate();

            if (this.vaga != null) {
                this.vaga.editarVaga(this.vaga.getNumero(), StatusVaga.LIVRE);
            }

            System.out.println("Pagamento registrado com sucesso! Valor total: R$ " + String.format("%.2f", this.valorTotal));

        } catch (SQLException e) {
            System.out.println("Erro ao registrar pagamento do ticket: " + e.getMessage());
        }
    }

    public void cancelarTicket() {
        if (!Usuario.isAdminLogado()) {
            System.out.println("Apenas administradores podem cancelar tickets.");
            return;
        }

        this.status = StatusTicket.CANCELADO;
        if (this.dataHoraSaida == null) {
            this.dataHoraSaida = LocalDateTime.now();
        }

        String sql = "UPDATE ticket SET dataHoraSaida = ?, status = ? WHERE id = ?";

        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, this.dataHoraSaida.toString());
            stmt.setString(2, this.status.name());
            stmt.setInt(3, this.id);

            stmt.executeUpdate();

            if (this.vaga != null) {
                this.vaga.editarVaga(this.vaga.getNumero(), StatusVaga.LIVRE);
            }

            System.out.println("Ticket cancelado com sucesso.");

        } catch (SQLException e) {
            System.out.println("Erro ao cancelar ticket: " + e.getMessage());
        }
    }

    public static List<Ticket> listarTickets() {
        return buscarTicketsComFiltro("SELECT id, placaVeiculo, numeroVaga, dataHoraEntrada, dataHoraSaida, valorTotal, status FROM ticket", null);
    }

    public static List<Ticket> listarTicketsAtivos() {
        return buscarTicketsComFiltro("SELECT id, placaVeiculo, numeroVaga, dataHoraEntrada, dataHoraSaida, valorTotal, status FROM ticket WHERE status = 'ABERTO'", null);
    }

    public static double gerarRelatorioFinanceiro(LocalDateTime dataInicio, LocalDateTime dataFim) {
        String sql = "SELECT SUM(valorTotal) AS total FROM ticket WHERE status = 'PAGO' AND dataHoraSaida BETWEEN ? AND ?";
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dataInicio.toString());
            stmt.setString(2, dataFim.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao gerar relatorio financeiro: " + e.getMessage());
        }
        return 0.0;
    }

    public static List<Ticket> buscarTicketsPorData(LocalDateTime data) {
        String dataStr = data.toLocalDate().toString();
        String sql = "SELECT id, placaVeiculo, numeroVaga, dataHoraEntrada, dataHoraSaida, valorTotal, status FROM ticket WHERE dataHoraEntrada LIKE ?";
        return buscarTicketsComFiltro(sql, dataStr + "%");
    }

    private static List<Ticket> buscarTicketsComFiltro(String sql, String parametro) {
        List<Ticket> lista = new ArrayList<>();
        
        try (Connection conn = Conexao.conectar();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            if (parametro != null) {
                stmt.setString(1, parametro);
            }
        
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String placa = rs.getString("placaVeiculo");
                    int numeroVaga = rs.getInt("numeroVaga");
                    LocalDateTime entrada = LocalDateTime.parse(rs.getString("dataHoraEntrada"));
                    String saidaStr = rs.getString("dataHoraSaida");
                    LocalDateTime saida = (saidaStr != null && !saidaStr.isEmpty()) ? LocalDateTime.parse(saidaStr) : null;
                    double valor = rs.getDouble("valorTotal");
                    StatusTicket status = StatusTicket.valueOf(rs.getString("status"));
                
                    Veiculo v = Veiculo.buscarVeiculoPorPlaca(placa);
                    Vaga vaga = buscarStatusVaga(conn, numeroVaga);
                
                    lista.add(new Ticket(id, v, vaga, entrada, saida, valor, status));
                }
            }
        
        } catch (SQLException e) {
            System.out.println("Erro ao buscar tickets no banco: " + e.getMessage());
        }
    
        return lista;
    }
    
    private static Vaga buscarStatusVaga(Connection conn, int numeroVaga) {
        String sql = "SELECT numero, status FROM vaga WHERE numero = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, numeroVaga);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Vaga(rs.getInt("numero"), StatusVaga.valueOf(rs.getString("status")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao buscar status da vaga: " + e.getMessage());
        }
        return new Vaga(numeroVaga, StatusVaga.LIVRE);
    }
}
