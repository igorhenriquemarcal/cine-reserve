package br.com.cinereserve.repository.json;

import br.com.cinereserve.model.Cliente;

/**
 * DTO para serialização JSON de Cliente.
 */
public class ClienteJsonDto {

    private String id;
    private String nome;
    private String email;
    private String cpf;

    public ClienteJsonDto() {
    }

    public ClienteJsonDto(Cliente cliente) {
        this.id = cliente.getId();
        this.nome = cliente.getNome();
        this.email = cliente.getEmail();
        this.cpf = cliente.getCpf();
    }

    public Cliente paraDominio() {
        return new Cliente(this.id, this.nome, this.email, this.cpf);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
