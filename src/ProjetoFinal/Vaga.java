package ProjetoFinal;

public class Vaga {
    private int numero;
    private StatusVaga status;

    public Vaga(int numero) {
        this.numero = numero;
        this.status = StatusVaga.LIVRE;
    }

    public boolean estaLivre() {
        return this.status == StatusVaga.LIVRE;
    }

    public void atualizar(StatusVaga status) {
        this.status = status;
    }

    public void exibir() {
        System.out.println("Vaga Nº: " + numero + " | Status: " + status);
    }

    public int getNumero() { return numero; }
    public StatusVaga getStatus() { return status; }
    public void setStatus(StatusVaga status) { this.status = status; }
}