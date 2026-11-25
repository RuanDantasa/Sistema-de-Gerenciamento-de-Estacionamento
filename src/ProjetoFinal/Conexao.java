package ProjetoFinal;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {
    private static final String URL = "jdbc:sqlite:estacionamento.db";

    public static Connection conectar() {
        try {
            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Erro ao conectar ao banco SQLite: " + e.getMessage());
            return null;
        }
    }

    public static void inicializarBanco() {
        String sqlUsuario = "CREATE TABLE IF NOT EXISTS usuario ("
                + "cpf TEXT PRIMARY KEY, "
                + "nome TEXT NOT NULL, "
                + "login TEXT UNIQUE NOT NULL, "
                + "senha TEXT NOT NULL, "
                + "ativo INTEGER NOT NULL, "
                + "admin INTEGER NOT NULL"
                + ");";

        String sqlVeiculo = "CREATE TABLE IF NOT EXISTS veiculo ("
                + "placa TEXT PRIMARY KEY, "
                + "modelo TEXT NOT NULL, "
                + "cor TEXT NOT NULL, "
                + "tipo TEXT NOT NULL"
                + ");";

        String sqlVaga = "CREATE TABLE IF NOT EXISTS vaga ("
                + "numero INTEGER PRIMARY KEY, "
                + "status TEXT NOT NULL"
                + ");";

        String sqlTicket = "CREATE TABLE IF NOT EXISTS ticket ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "placaVeiculo TEXT NOT NULL, "
                + "numeroVaga INTEGER NOT NULL, "
                + "dataHoraEntrada TEXT NOT NULL, "
                + "dataHoraSaida TEXT, "
                + "valorTotal REAL NOT NULL, "
                + "status TEXT NOT NULL, "
                + "FOREIGN KEY (placaVeiculo) REFERENCES veiculo (placa), "
                + "FOREIGN KEY (numeroVaga) REFERENCES vaga (numero)"
                + ");";

        try (Connection conn = conectar(); Statement stmt = conn.createStatement()) {
            if (conn != null) {
                stmt.execute(sqlUsuario);
                stmt.execute(sqlVeiculo);
                stmt.execute(sqlVaga);
                stmt.execute(sqlTicket);
            }
        } catch (SQLException e) {
            System.out.println("Erro ao inicializar tabelas: " + e.getMessage());
        }
    }
}
