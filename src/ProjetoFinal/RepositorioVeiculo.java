package ProjetoFinal;

import java.util.ArrayList;
import java.util.List;

public class RepositorioVeiculo {
    private List<Veiculo> listaVeiculos = new ArrayList<>();

    public void criarVeiculo(String placa, String modelo, String cor, TipoVeiculo tipo) {
        Veiculo v = new Veiculo(placa, modelo, cor, tipo);
        listaVeiculos.add(v);
    }

    public Veiculo verVeiculo(String placa) {
        for (Veiculo v : listaVeiculos) {
            if (v.getPlaca().equalsIgnoreCase(placa)) {
                return v;
            }
        }
        return null;
    }

    public boolean editarVeiculo(String placa, String novoModelo, String novaCor, TipoVeiculo novoTipo) {
        Veiculo v = verVeiculo(placa);
        if (v != null) {
            v.atualizar(novoModelo, novaCor, novoTipo); // Chama o método da classe Veiculo
            return true;
        }
        return false;
    }

    public boolean deletarVeiculo(String placa) {
        Veiculo v = verVeiculo(placa);
        if (v != null) {
            listaVeiculos.remove(v);
            return true;
        }
        return false;
    }

    public void listarVeiculos() {
        if (listaVeiculos.isEmpty()) {
            System.out.println("Nenhum veículo cadastrado.");
            return;
        }
        for (Veiculo v : listaVeiculos) {
            v.exibirDetalhes();
        }
    }
}
