package ProjetoFinal;

import java.util.ArrayList;
import java.util.List;


public class Usuario {
    private String nome;
    private String cpf;
    private String login;
    private String senha;
    private boolean ativo;
    private boolean admin;
    
    private static List<Usuario> listaUsuarios = new ArrayList<>();
    
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }
    
    public Usuario(String nome, String cpf, String login, String senha, boolean ativo, boolean admin) {
        this.nome = nome;
        this.cpf = cpf;
        this.login = login;
        this.senha = senha;
        this.ativo = ativo;
        this.admin = admin;
    }
    
    public static Usuario criarUsuario(String nome, String cpf, String login, String senha, boolean ativo, boolean admin){
        Usuario novo = new Usuario(nome, cpf, login, senha, ativo, admin);
        listaUsuarios.add(novo);
        return novo;
    }

    public void verUsuario(){
        System.out.println("Nome do Usuário: " + nome); 
        System.out.println("CPF " + cpf); 
        System.out.println("Login: " + login); 
        System.out.println("Status: " + (this.ativo ? "Ativo" : "Inativo")); 
        System.out.println("Perfil: " + (this.admin ? "Administrador" : "Padrão")); 
    }
    
    public void editarUsuario(String nome, String cpf, String login, String senha, boolean ativo, boolean admin){
        this.nome = nome;
        this.cpf = cpf;
        this.login = login;
        this.senha = senha;
        this.ativo = ativo;
        this.admin = admin;
    }

    public void desativarUsuario(){
        ativo = false;
    }
    
    public static List<Usuario> listarUsuarios(){
        return listaUsuarios;
    }

    public static boolean autenticar(String login, String senha){
        for(Usuario u : listaUsuarios){
            if(u.login.equals(login) && u.senha.equals(senha)){
                return u.ativo;
            }
        } return false;
    }

    public static void logout(){
        System.out.println("Sessão finalizada.");
    }

}