package br.com.cinereserve.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Representa um cliente titular no sistema CineReserve.
 */
public class Cliente {

    private final String id;
    private String nome;
    private String email;
    private String cpf;

    public Cliente(String id, String nome, String email, String cpf) {
        this.id = (id != null && !id.isBlank()) ? id : UUID.randomUUID().toString();
        this.nome = validarNaoVazio(nome, "O nome do cliente não pode ser vazio.");
        this.email = validarNaoVazio(email, "O e-mail do cliente não pode ser vazio.");
        this.cpf = validarNaoVazio(cpf, "O CPF do cliente não pode ser vazio.");
    }

    public Cliente(String nome, String email, String cpf) {
        this(UUID.randomUUID().toString(), nome, email, cpf);
    }

    private static String validarNaoVazio(String valor, String mensagemErro) {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException(mensagemErro);
        }
        return valor.trim();
    }

    public String getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = validarNaoVazio(nome, "O nome do cliente não pode ser vazio.");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = validarNaoVazio(email, "O e-mail do cliente não pode ser vazio.");
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = validarNaoVazio(cpf, "O CPF do cliente não pode ser vazio.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return Objects.equals(id, cliente.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", email='" + email + '\'' +
                ", cpf='" + cpf + '\'' +
                '}';
    }
}
