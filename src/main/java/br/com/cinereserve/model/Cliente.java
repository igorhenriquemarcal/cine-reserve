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

    private static final java.util.regex.Pattern EMAIL_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Cliente(String id, String nome, String email, String cpf) {
        this.id = (id != null && !id.isBlank()) ? id : UUID.randomUUID().toString();
        this.nome = validarNaoVazio(nome, "O nome do cliente não pode ser vazio.");
        this.email = validarEmail(email);
        this.cpf = normalizarEValidarCpf(cpf);
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

    private static String validarEmail(String email) {
        String emailLimpo = validarNaoVazio(email, "O e-mail do cliente não pode ser vazio.");
        if (!EMAIL_PATTERN.matcher(emailLimpo).matches()) {
            throw new IllegalArgumentException("Formato de e-mail inválido: " + emailLimpo);
        }
        return emailLimpo.toLowerCase();
    }

    private static String normalizarEValidarCpf(String cpf) {
        String cpfLimpo = validarNaoVazio(cpf, "O CPF do cliente não pode ser vazio.");
        String digitos = cpfLimpo.replaceAll("\\D", "");
        if (digitos.length() != 11) {
            throw new IllegalArgumentException("O CPF deve conter exatamente 11 dígitos numéricos.");
        }
        return String.format("%s.%s.%s-%s",
                digitos.substring(0, 3),
                digitos.substring(3, 6),
                digitos.substring(6, 9),
                digitos.substring(9, 11));
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
        this.email = validarEmail(email);
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = normalizarEValidarCpf(cpf);
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
