package ProjetoFinal;

import java.util.ArrayList;
import java.util.List;

public class Vaga {

    private int id;
    private String codigo;
    private StatusVaga status;
    private static final List<Vaga> listaVagas = new ArrayList<>();

    public Vaga(int id, String codigo, StatusVaga status) {
        this.id = id;
        this.codigo = codigo;
        this.status = status;
    }

    public static Vaga criarVaga(String codigo) {
        validarCodigo(codigo);
        String codigoNormalizado = codigo.trim().toUpperCase();
        if (buscarVagaPorCodigo(codigoNormalizado) != null) {
            throw new IllegalArgumentException("Já existe vaga com o código " + codigoNormalizado + ".");
        }
        Vaga vaga = new Vaga(0, codigoNormalizado, StatusVaga.LIVRE);
        vaga.id = listaVagas.isEmpty() ? 1 : listaVagas.size() + 1;
        listaVagas.add(vaga);
        return vaga;
    }

    public String verVaga() {
        return "Vaga " + id + " | Código: " + codigo + " | Status: " + status;
    }

    public void editarVaga(String codigo, StatusVaga status) {
        validarCodigo(codigo);
        if (status == null) {
            throw new IllegalArgumentException("Status é obrigatório.");
        }
        String codigoNormalizado = codigo.trim().toUpperCase();
        Vaga existente = buscarVagaPorCodigo(codigoNormalizado);
        if (existente != null && existente.id != this.id) {
            throw new IllegalArgumentException("Já existe vaga com o código " + codigoNormalizado + ".");
        }
        if (status == StatusVaga.OCUPADA && this.status != StatusVaga.LIVRE) {
            throw new IllegalStateException("Só é possível ocupar uma vaga livre.");
        }
        if (status == StatusVaga.MANUTENCAO && this.status == StatusVaga.OCUPADA) {
            throw new IllegalStateException("Vaga ocupada não pode ir para manutenção.");
        }
        this.codigo = codigoNormalizado;
        this.status = status;
    }

    public static List<Vaga> listarVagas() {
        return new ArrayList<>(listaVagas);
    }

    public static List<Vaga> listarVagasLivres() {
        List<Vaga> vagasLivres = new ArrayList<>();
        for (Vaga vaga : listaVagas) {
            if (vaga.status == StatusVaga.LIVRE) {
                vagasLivres.add(vaga);
            }
        }
        return vagasLivres;
    }

    public static boolean existeVagaLivre() {
        return !listarVagasLivres().isEmpty();
    }

    public static Vaga buscarVagaPorId(int id) {
        for (Vaga vaga : listaVagas) {
            if (vaga.id == id) {
                return vaga;
            }
        }
        return null;
    }

    public static Vaga buscarVagaPorCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        String codigoNormalizado = codigo.trim().toUpperCase();
        for (Vaga vaga : listaVagas) {
            if (vaga.codigo.equals(codigoNormalizado)) {
                return vaga;
            }
        }
        return null;
    }

    private static void validarCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("Código da vaga é obrigatório.");
        }
    }

    public int getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public StatusVaga getStatus() {
        return status;
    }
}