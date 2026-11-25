package ProjetoFinal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Veiculo {
    private String placa;
    private String modelo;
    private String cor;
    private TipoVeiculo tipo;
    
    public String getPlaca() { return placa;}
    public String getModelo() {return modelo;}
    public String getCor() {return cor;}
    public TipoVeiculo getTipo() {return tipo;}

    public void setPlaca(String placa) {this.placa = placa;}
    public void setModelo(String modelo) {this.modelo = modelo;}
    public void setCor(String cor) {this.cor = cor;}
    public void setTipo(TipoVeiculo tipo) {this.tipo = tipo;}

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

        String sql = "INSERT INTO veiculo (placa, modelo, cor, tipo) VALUES (?, ?, ?, ?)";
        try (Connection conn = Conexao.conectar(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, placaNormalizada);
            stmt.setString(2, modelo.trim());
            stmt.setString(3, cor.trim());
            stmt.setString(4, tipo.name());

            stmt.executeUpdate();
            return new Veiculo(placaNormalizada, modelo.trim(), cor.trim(), tipo);

        } catch (SQLException e) {
            System.out.println("Erro ao salvar veículo no banco: " + e.getMessage());
            return null;
        }
    }

    public void verVeiculo() {
        System.out.println("Placa: " + placa + " | Modelo: " + modelo + " | Cor: " + cor + " | Tipo: " + tipo);
    }

    public void editarVeiculo(String placa, String modelo, String cor, TipoVeiculo tipo) {
        validarTexto(modelo, "Modelo");
        validarTexto(cor, "Cor");
        if (tipo == null) {
            throw new IllegalArgumentException("Tipo do veículo é obrigatório.");
        }

        String sql = "UPDATE veiculo SET modelo = ?, cor = ?, tipo = ? WHERE placa = ?";
        try (Connection conn = Conexao.conectar(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, modelo.trim());
            stmt.setString(2, cor.trim());
            stmt.setString(3, tipo.name());
            stmt.setString(4, this.placa);

            stmt.executeUpdate();

            this.modelo = modelo.trim();
            this.cor = cor.trim();
            this.tipo = tipo;

        } catch (SQLException e) {
            System.out.println("Erro ao editar veículo no banco: " + e.getMessage());
        }
    }

    public static void excluirVeiculo(Veiculo veiculo) {
        if (veiculo == null) {
            return;
        }

        String sql = "DELETE FROM veiculo WHERE placa = ?";
        try (Connection conn = Conexao.conectar(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, veiculo.getPlaca());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("Erro ao excluir veículo do banco: " + e.getMessage());
        }
    }

    public static List<Veiculo> listarVeiculos() {
        List<Veiculo> lista = new ArrayList<>();
        String sql = "SELECT placa, modelo, cor, tipo FROM veiculo";

        try (Connection conn = Conexao.conectar(); 
             PreparedStatement stmt = conn.prepareStatement(sql); 
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Veiculo v = new Veiculo(
                    rs.getString("placa"),
                    rs.getString("modelo"),
                    rs.getString("cor"),
                    TipoVeiculo.valueOf(rs.getString("tipo"))
                );
                lista.add(v);
            }

        } catch (SQLException e) {
            System.out.println("Erro ao listar veículos do banco: " + e.getMessage());
        }
        return lista;
    }

    public static Veiculo buscarVeiculoPorPlaca(String placa) {
        if (placa == null) {
            return null;
        }
        String placaNormalizada = placa.trim().toUpperCase().replace("-", "");
        String sql = "SELECT placa, modelo, cor, tipo FROM veiculo WHERE placa = ?";

        try (Connection conn = Conexao.conectar(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, placaNormalizada);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Veiculo(
                        rs.getString("placa"),
                        rs.getString("modelo"),
                        rs.getString("cor"),
                        TipoVeiculo.valueOf(rs.getString("tipo"))
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println("Erro ao buscar veículo por placa: " + e.getMessage());
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

}