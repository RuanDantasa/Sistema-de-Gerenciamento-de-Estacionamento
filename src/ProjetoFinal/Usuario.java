package ProjetoFinal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class Usuario {
    private String nome;
    private String cpf;
    private String login;
    private String senha;
    private boolean ativo;
    private boolean admin;

    private static Usuario usuarioLogado = null; 

    public static Usuario getUsuarioLogado(){ return usuarioLogado; }
    public void setNome(String nome) {this.nome = nome;}
    public void setCpf(String cpf) {this.cpf = cpf;}
    public void setLogin(String login) {this.login = login;}
    public void setSenha(String senha) {this.senha = senha;}
    public void setAtivo(boolean ativo) {this.ativo = ativo;}
    public void setAdmin(boolean admin) {this.admin = admin;}
    
    public String getNome() {return nome;}
    public String getCpf() {return cpf;}
    public String getLogin() {return login;}
    public String getSenha() {return senha;}
    public boolean isAtivo() {return ativo;}
    public boolean isAdmin() {return admin;}
    public static boolean isAdminLogado() { return usuarioLogado != null && usuarioLogado.isAdmin(); }
    
    public Usuario(String nome, String cpf, String login, String senha, boolean ativo, boolean admin) {
        this.nome = nome;
        this.cpf = cpf;
        this.login = login;
        this.senha = senha;
        this.ativo = ativo;
        this.admin = admin;
    }
    
    public static Usuario criarUsuario(String nome, String cpf, String login, String senha, boolean ativo, boolean admin){
        String sql = "INSERT INTO usuario (cpf, nome, login, senha, ativo, admin) VALUES (?, ?, ?, ?, ?, ?)";

        try(Connection conn = Conexao.conectar(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cpf);
            stmt.setString(2, nome);
            stmt.setString(3, login);
            stmt.setString(4, senha);
            stmt.setInt(5, 1);
            stmt.setInt(6, admin ? 1 : 0);

            stmt.executeUpdate();
            return new Usuario(nome, cpf, login, senha, true, admin);
        } catch(SQLException e){
            System.out.println("Erro ao criar usuario no banco: " + e.getMessage());
            return null;
        }
    }
    

    public void verUsuario(){
        System.out.println("Nome do Usuário: " + nome); 
        System.out.println("CPF " + cpf); 
        System.out.println("Login: " + login); 
        System.out.println("Status: " + (this.ativo ? "Ativo" : "Inativo")); 
        System.out.println("Perfil: " + (this.admin ? "Administrador" : "Padrão")); 
    }
    
    public void editarUsuario(String nome, String cpf, String login, String senha, boolean ativo, boolean admin) {
        String sql = "UPDATE usuario SET nome = ?, cpf = ?, login = ?, senha = ?, ativo = ?, admin = ? WHERE cpf = ?";
    
        try (Connection conn = Conexao.conectar(); 
            PreparedStatement stmt = conn.prepareStatement(sql)) {
        
            stmt.setString(1, nome);
            stmt.setString(2, cpf); 
            stmt.setString(3, login);
            stmt.setString(4, senha);
            stmt.setInt(5, ativo ? 1 : 0);
            stmt.setInt(6, admin ? 1 : 0);
            stmt.setString(7, this.cpf); 
                
            stmt.executeUpdate();

            this.nome = nome;
            this.login = login;
            this.senha = senha;
            this.ativo = ativo;
            this.admin = admin;
            this.cpf = cpf;
        }catch (SQLException e) {
            System.out.println("Erro ao editar usuario: " + e.getMessage());
        }
    }

    public void desativarUsuario() {
    String sql = "UPDATE usuario SET ativo = 0 WHERE cpf = ?";
    
    try (Connection conn = Conexao.conectar(); 
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, this.cpf);
        stmt.executeUpdate();
        this.ativo = false;
        
    } catch (SQLException e) {
        System.out.println("Erro ao desativar usuario: " + e.getMessage());
    }
}

    
    public static List<Usuario> listarUsuarios() {
    List<Usuario> lista = new ArrayList<>();
    String sql = "SELECT nome, cpf, login, senha, ativo, admin FROM usuario";

    try (Connection conn = Conexao.conectar(); 
        PreparedStatement stmt = conn.prepareStatement(sql); 
        ResultSet rs = stmt.executeQuery()) {

        while (rs.next()) {
            Usuario u = new Usuario(
                rs.getString("nome"),
                rs.getString("cpf"),
                rs.getString("login"),
                rs.getString("senha"),
                rs.getInt("ativo") == 1,
                rs.getInt("admin") == 1
            );
            lista.add(u);
        }
    } catch (SQLException e) {
        System.out.println("Erro ao listar usuarios: " + e.getMessage());
    }
    return lista;
}


    public static boolean autenticar(String login, String senha) {
        String sql = "SELECT nome, cpf, login, senha, ativo, admin FROM usuario WHERE login = ? AND senha = ?";

        try (Connection conn = Conexao.conectar();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, login);
            stmt.setString(2, senha);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    boolean ativo = rs.getInt("ativo") == 1;
                    if (!ativo) {
                        System.out.println("Conta desativada. Acesso negado.");
                        return false;
                    }
                    usuarioLogado = new Usuario(
                        rs.getString("nome"),
                        rs.getString("cpf"),
                        rs.getString("login"),
                        rs.getString("senha"),
                        true,
                        rs.getInt("admin") == 1
                    );
                    System.out.println("Bem-vindo, " + usuarioLogado.getNome() + "!");
                    return true;
                }
                System.out.println("Login ou senha inválidos.");
                return false;
            }

        } catch (SQLException e) {
            System.out.println("Erro na autenticacao: " + e.getMessage());
            return false;
        }
    }


    public static void logout() {
    if (usuarioLogado == null) {
        System.out.println("Nenhum usuário logado no momento.");
        return;
    }
    System.out.println("Sessão de " + usuarioLogado.getNome() + " finalizada.");
    usuarioLogado = null;
}
    

}