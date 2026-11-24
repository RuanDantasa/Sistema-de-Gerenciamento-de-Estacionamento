package ProjetoFinal;

public class Veiculo {
    private String placa;
    private String modelo;
    private String cor;
    private TipoVeiculo tipo;

    public Veiculo(String placa, String modelo, String cor, TipoVeiculo tipo) {
        this.placa = placa;
        this.modelo = modelo;
        this.cor = cor;
        this.tipo = tipo;
    }

    public void atualizar(String modelo, String cor, TipoVeiculo tipo) {
        this.modelo = modelo;
        this.cor = cor;
        this.tipo = tipo;
    }

    public void exibirDetalhes() {
        System.out.println("Placa: " + placa + " | Modelo: " + modelo + " | Cor: " + cor + " | Tipo: " + tipo);
    }

    public String getPlaca() { return placa; }
    public String getModelo() { return modelo; }
    public String getCor() { return cor; }
    public TipoVeiculo getTipo() { return tipo; }
}