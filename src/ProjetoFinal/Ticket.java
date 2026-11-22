package ProjetoFinal;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Ticket {
    private int id;
    private Date dataHoraEntrada;
    private Date dataHoraSaida;
    private double valorTotal;
    private StatusTicket status;
    private Veiculo veiculo;
    private Vaga vaga;

    private static int proximoId = 1;
    private static List<Ticket> listaTickets = new ArrayList<>();

    public Ticket(Veiculo veiculo, Vaga vaga) {
        this.id = proximoId++;
        this.veiculo = veiculo;
        this.vaga = vaga;
        this.dataHoraEntrada = new Date();
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

    public Date getDataHoraEntrada() {
        return dataHoraEntrada;
    }

    public void setDataHoraEntrada(Date dataHoraEntrada) {
        this.dataHoraEntrada = dataHoraEntrada;
    }

    public Date getDataHoraSaida() {
        return dataHoraSaida;
    }

    public void setDataHoraSaida(Date dataHoraSaida) {
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

    public void cancelarTicket() {
        this.status = StatusTicket.CANCELADO;
    }

    public void verTicket() {
        SimpleDateFormat formato = new SimpleDateFormat("dia/mes/ano hora:minuto:segundo");
        System.out.println("ID do Ticket: " + id);
        System.out.println("Veículo: " + veiculo);
        System.out.println("Vaga: " + vaga);
        System.out.println("Entrada: " + (dataHoraEntrada != null ? formato.format(dataHoraEntrada) : "Não informada"));
        System.out.println("Saída: " + (dataHoraSaida != null ? formato.format(dataHoraSaida) : "Em aberto"));
        System.out.println("Valor Total: R$ " + String.format("%.2f", valorTotal));
        System.out.println("Status: " + status);
    }

    public static Ticket criarTicket(Veiculo veiculo, Vaga vaga) {
        Ticket novoTicket = new Ticket(veiculo, vaga);
        listaTickets.add(novoTicket);
        return novoTicket;
    }

    public void editarTicket(Date dataHoraEntrada, Date dataHoraSaida, double valorTotal, StatusTicket status) {
        this.dataHoraEntrada = dataHoraEntrada;
        this.dataHoraSaida = dataHoraSaida;
        this.valorTotal = valorTotal;
        this.status = status;
    }

    public double calcularValor() {
        Date fim = (this.dataHoraSaida != null) ? this.dataHoraSaida : new Date();
        long diferencaMilissegundos = fim.getTime() - this.dataHoraEntrada.getTime();
        double horas = diferencaMilissegundos / (1000.0 * 60 * 60);
        long horasCobradas = (long) Math.ceil(horas);
        if (horasCobradas < 1) {
            horasCobradas = 1;
        }
        this.valorTotal = horasCobradas * 5.0;
        return this.valorTotal;
    }

    public void registrarPagamento() {
        if (this.status == StatusTicket.ABERTO) {
            this.dataHoraSaida = new Date();
            this.valorTotal = calcularValor();
            this.status = StatusTicket.PAGO;
        }
    }

    public static List<Ticket> listarTickets() {
        return listaTickets;
    }

    public static List<Ticket> listarTicketsAtivos() {
        List<Ticket> ativos = new ArrayList<>();
        for (Ticket ticket : listaTickets) {
            if (ticket.getStatus() == StatusTicket.ABERTO) {
                ativos.add(ticket);
            }
        }
        return ativos;
    }

    public double gerarRelatorioFinanceiro(Date dataInicio, Date dataFim) {
        double total = 0.0;
        for (Ticket ticket : listaTickets) {
            Date dataReferencia = ticket.getDataHoraSaida() != null ? ticket.getDataHoraSaida() : ticket.getDataHoraEntrada();
            if (ticket.getStatus() == StatusTicket.PAGO && dataReferencia != null
                    && !dataReferencia.before(dataInicio) && !dataReferencia.after(dataFim)) {
                total += ticket.getValorTotal();
            }
        }
        return total;
    }

    public static List<Ticket> buscarTicketsPorData(Date data) {
        List<Ticket> encontrados = new ArrayList<>();
        Calendar calAlvo = Calendar.getInstance();
        calAlvo.setTime(data);

        Calendar calTicket = Calendar.getInstance();
        for (Ticket ticket : listaTickets) {
            if (ticket.getDataHoraEntrada() != null) {
                calTicket.setTime(ticket.getDataHoraEntrada());
                if (calTicket.get(Calendar.YEAR) == calAlvo.get(Calendar.YEAR)
                        && calTicket.get(Calendar.MONTH) == calAlvo.get(Calendar.MONTH)
                        && calTicket.get(Calendar.DAY_OF_MONTH) == calAlvo.get(Calendar.DAY_OF_MONTH)) {
                    encontrados.add(ticket);
                }
            }
        }
        return encontrados;
    }
}