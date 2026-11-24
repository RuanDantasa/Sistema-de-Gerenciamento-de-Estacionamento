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

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public StatusVaga getStatus() {
        return status;
    }

    public void setStatus(StatusVaga status) {
        this.status = status;
    }
}