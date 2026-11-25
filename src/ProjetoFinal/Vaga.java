package ProjetoFinal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Vaga {
    private int numero;
    private StatusVaga status;

    public Vaga(int numero, StatusVaga status) {
        this.numero = numero;
        this.status = status;
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

    public void verVaga() {
        System.out.println("Vaga Nº: " + this.numero + " | Status: " + this.status);
    }

    public static Vaga criarVaga(int numero, StatusVaga status) {
        if (status == null) {
            status = StatusVaga.LIVRE;
        }

        String sql = "INSERT INTO vaga (numero, status) VALUES (?, ?)";
        try (Connection conn = Conexao.conectar(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, numero);
            stmt.setString(2, status.name());
            stmt.executeUpdate();

            return new Vaga(numero, status);

        } catch (SQLException e) {
            System.out.println("Erro ao criar vaga no banco: " + e.getMessage());
            return null;
        }
    }

    public void editarVaga(int numero, StatusVaga status) {
        String sql = "UPDATE vaga SET status = ? WHERE numero = ?";
        try (Connection conn = Conexao.conectar(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, numero);
            stmt.executeUpdate();

            this.status = status;

        } catch (SQLException e) {
            System.out.println("Erro ao editar vaga no banco: " + e.getMessage());
        }
    }

    public static void excluirVaga(Vaga vaga) {
        if (vaga == null) {
            return;
        }

        String sql = "DELETE FROM vaga WHERE numero = ?";
        try (Connection conn = Conexao.conectar(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, vaga.getNumero());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao excluir vaga do banco: " + e.getMessage());
        }
    }

    public static List<Vaga> listarVagas() {
        List<Vaga> lista = new ArrayList<>();
        String sql = "SELECT numero, status FROM vaga";

        try (Connection conn = Conexao.conectar(); 
             PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Vaga v = new Vaga(
                    rs.getInt("numero"),
                    StatusVaga.valueOf(rs.getString("status"))
                );
                lista.add(v);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar vagas do banco: " + e.getMessage());
        }
        return lista;
    }

    public static List<Vaga> listarVagasDisponiveis() {
        List<Vaga> lista = new ArrayList<>();
        String sql = "SELECT numero, status FROM vaga WHERE status = ?";

        try (Connection conn = Conexao.conectar(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, StatusVaga.LIVRE.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Vaga v = new Vaga(
                        rs.getInt("numero"),
                        StatusVaga.valueOf(rs.getString("status"))
                    );
                    lista.add(v);
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar vagas disponiveis: " + e.getMessage());
        }
        return lista;
    }
}
