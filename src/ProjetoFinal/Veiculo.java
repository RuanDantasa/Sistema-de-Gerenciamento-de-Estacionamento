package ProjetoFinal;

import java.util.ArrayList;
import java.util.List;

public class Veiculo {

    private String placa;
    private String modelo;
    private String cor;
    private TipoVeiculo tipo;
    private static final List<Veiculo> listaVeiculos = new ArrayList<>();

    public Veiculo(String placa, String modelo, String cor, TipoVeiculo tipo) {
        this.placa = placa;
        this.modelo = modelo;
        this.cor = cor;
        this.tipo = tipo;
    }

    public static Veiculo criarVeiculo(String placa, String modelo, String cor, TipoVeiculo tipo) {
        String placaNormalizada = normalizarPlaca(placa);
        validarTexto(modelo, "Modelo");
        validarTexto(cor, "Cor");
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do veículo é obrigatório.");
        }
        if (buscarVeiculoPorPlaca(placaNormalizada) != null) {
            throw new IllegalArgumentException("Já existe veículo cadastrado com a placa " + placaNormalizada + ".");
        }
        Veiculo veiculo = new Veiculo(placaNormalizada, modelo.trim(), cor.trim(), tipo);
        listaVeiculos.add(veiculo);
        return veiculo;
    }

    public String verVeiculo() {
        return "Placa: " + placa + " | Modelo: " + modelo + " | Cor: " + cor + " | Tipo: " + tipo;
    }

    public void editarVeiculo(String modelo, String cor) {
        validarTexto(modelo, "Modelo");
        validarTexto(cor, "Cor");
        this.modelo = modelo.trim();
        this.cor = cor.trim();
    }

    public void excluirVeiculo() {
        listaVeiculos.remove(this);
    }

    public static List<Veiculo> listarVeiculos() {
        return new ArrayList<>(listaVeiculos);
    }

    public static Veiculo buscarVeiculoPorPlaca(String placa) {
        if (placa == null) {
            return null;
        }
        String placaNormalizada = placa.trim().toUpperCase().replace("-", "");
        for (Veiculo veiculo : listaVeiculos) {
            if (veiculo.placa.equals(placaNormalizada)) {
                return veiculo;
            }
        }
        return null;
    }

    private static String normalizarPlaca(String placa) {
        if (placa == null) {
            throw new IllegalArgumentException("Placa é obrigatória.");
        }
        String normalizada = placa.trim().toUpperCase().replace("-", "");
        if (!normalizada.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}")) {
            throw new IllegalArgumentException("Placa inválida: " + placa);
        }
        return normalizada;
    }

    private static void validarTexto(String valor, String campo) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(campo + " é obrigatório.");
        }
    }

    public String getPlaca() {
        return placa;
    }

    public String getModelo() {
        return modelo;
    }

    public String getCor() {
        return cor;
    }

    public TipoVeiculo getTipo() {
        return tipo;
    }
}