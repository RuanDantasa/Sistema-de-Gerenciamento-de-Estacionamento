package ProjetoFinal;

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

    private static int proximoId = 1;
    private static List<Ticket> listaTickets = new ArrayList<>();

    public Ticket(Veiculo veiculo, Vaga vaga) {
        this.id = proximoId++;
        this.veiculo = veiculo;
        this.vaga = vaga;
        this.dataHoraEntrada = LocalDateTime.now();
        this.dataHoraSaida = null;
        this.valorTotal = 0.0;
        this.status = StatusTicket.ABERTO;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Veiculo getVeiculo() {
        return veiculo;
    }

    public void setVeiculo(Veiculo veiculo) {
        this.veiculo = veiculo;
    }

    public Vaga getVaga() {
        return vaga;
    }

    public void setVaga(Vaga vaga) {
        this.vaga = vaga;
    }

    public LocalDateTime getDataHoraEntrada() {
        return dataHoraEntrada;
    }

    public void setDataHoraEntrada(LocalDateTime dataHoraEntrada) {
        this.dataHoraEntrada = dataHoraEntrada;
    }

    public LocalDateTime getDataHoraSaida() {
        return dataHoraSaida;
    }

    public void setDataHoraSaida(LocalDateTime dataHoraSaida) {
        this.dataHoraSaida = dataHoraSaida;
    }

    public double getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public StatusTicket getStatus() {
        return status;
    }

    public void setStatus(StatusTicket status) {
        this.status = status;
    }

    public static Ticket criarTicket(Veiculo veiculo, Vaga vaga) {
        Ticket novoTicket = new Ticket(veiculo, vaga);
        listaTickets.add(novoTicket);
        return novoTicket;
    }

    public void verTicket() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        System.out.println("ID do Ticket: " + id);
        System.out.println("Veículo: " + veiculo);
        System.out.println("Vaga: " + vaga);
        System.out.println("Entrada: " + dataHoraEntrada.format(formato));
        System.out.println("Saída: " + (dataHoraSaida != null ? dataHoraSaida.format(formato) : "Em aberto"));
        System.out.println("Valor Total: R$ " + String.format("%.2f", valorTotal));
        System.out.println("Status: " + status);
    }

    public static List<Ticket> listarTickets() {
        return listaTickets;
    }

    public static Ticket buscarTicketPorId(int id) {
        for (Ticket ticket : listaTickets) {
            if (ticket.getId() == id) {
                return ticket;
            }
        }
        return null;
    }

    public void editarTicket(Veiculo veiculo, Vaga vaga, LocalDateTime dataHoraSaida, double valorTotal, StatusTicket status) {
        this.veiculo = veiculo;
        this.vaga = vaga;
        this.dataHoraSaida = dataHoraSaida;
        this.valorTotal = valorTotal;
        this.status = status;
    }

    public static boolean excluirTicket(int id) {
        Ticket ticket = buscarTicketPorId(id);
        if (ticket != null) {
            return listaTickets.remove(ticket);
        }
        return false;
    }
}