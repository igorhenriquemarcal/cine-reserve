# CineReserve

Sistema em Java para gerenciamento e reserva de assentos de cinema via terminal, com persistência em arquivos JSON e controle de concorrência para reservas simultâneas.

O objetivo do projeto é aplicar conceitos avançados de Programação Orientada a Objetos (POO), arquitetura em camadas sem dependência de frameworks (como Spring), tratamento seguro de multithreading e boas práticas de testes automatizados.

---

## 🎬 Funcionalidades

- **Gestão de Sessões e Filmes**: Visualização de sessões programadas, horários, preços base e capacidade da sala.
- **Mapa Visual de Assentos (ASCII)**: Exibição da sala em matriz no console com categorização dos assentos (`Comum`, `VIP` e `PNE - Acessível`) e indicação de assentos livres e ocupados em tempo real.
- **Cadastro e Consulta de Clientes**: Registro por CPF, e-mail e nome com validações de dados.
- **Reserva Atômica**: Reserva de múltiplos assentos em uma única operação ("tudo ou nada"). Se um dos assentos escolhidos estiver ocupado, a operação é revertida para evitar estados inconsistentes.
- **Cálculo Dinâmico de Valores**: Aplicação de multiplicadores sobre o preço base de acordo com o tipo de assento (ex.: assento VIP possui acréscimo de 50%).
- **Cancelamento e Liberação**: Cancelamento de reservas com devolução imediata dos assentos para a sessão.
- **Persistência em JSON**: Armazenamento do estado em arquivos JSON sem corrupção de referências entre objetos.
- **Controle de Concorrência**: Prevenção de *race condition* (dupla reserva do mesmo assento) quando múltiplas threads/usuários tentam reservar simultaneamente.

---

## 🧱 Arquitetura e Decisões de Design

O projeto foi dividido em camadas desacopladas seguindo os princípios de responsabilidade única (SRP) e inversão de dependência (DIP):

```
br.com.cinereserve
├── cli            # Interface de terminal (menus, leitura de entrada e renderização do mapa)
├── exception      # Exceções de negócio (AssentoIndisponivelException, etc.)
├── model          # Entidades do domínio (Cliente, Assento, TipoAssento, Sessao, Reserva)
├── repository     # Contratos e implementações de persistência JSON com DTOs dedicados
├── service        # Regras de negócio, transações e controle de travas por sessão
└── Main.java      # Bootstrap e ponto de entrada da aplicação
```

### Pontos Chave de Implementação:
1. **Domínio Rico vs. Anêmico**: A classe `Sessao` é quem controla quais assentos estão livres ou ocupados nela. A entidade física `Assento` permanece imutável e reutilizável.
2. **DTOs na Persistência JSON**: Para evitar referências circulares e arquivos redundantes, a camada de persistência utiliza DTOs (`ReservaJsonDto`, `SessaoJsonDto`, etc.). A `Reserva` em disco grava apenas os identificadores do cliente, da sessão e os códigos dos assentos, reconstruindo o grafo de objetos durante a leitura.
3. **Travas Granulares por Sessão (`ReentrantLock`)**: Em vez de bloquear o sistema inteiro ao realizar uma reserva, o `ReservaService` utiliza locks gerenciados em um `ConcurrentHashMap` por ID de sessão. Assim, reservas para filmes ou salas diferentes acontecem em paralelo sem gargalos, enquanto disputas na mesma sessão são serializadas com segurança.
4. **Precisão Monetária**: Utilização de `BigDecimal` com arredondamento explícito (`RoundingMode.HALF_UP`) em todos os cálculos financeiros para evitar erros de ponto flutuante.

---

## 🛠️ Tecnologias Utilizadas

- **Java 21**
- **Apache Maven**
- **JUnit 5 (Jupiter)**: Testes unitários de regras de negócio, testes de persistência com `@TempDir` e teste de carga concorrente com `CountDownLatch` e `ExecutorService`.
- **Jackson (databind + datatype-jsr310)**: Serialização e desserialização JSON com suporte a datas modernas (`LocalDateTime`).

---

## 🚀 Como Executar o Projeto

### Pré-requisitos
- JDK 21 instalado e configurado no PATH
- Maven 3.8+ (ou utilize o Maven integrado na sua IDE, como IntelliJ IDEA)

### Clonando o repositório
```bash
git clone https://github.com/igorhenriquemarcal/cine-reserve.git
cd cine-reserve
```

### Compilar e Rodar os Testes
```bash
mvn clean test
```

### Executar a Aplicação pelo Terminal
```bash
mvn compile exec:java -Dexec.mainClass="br.com.cinereserve.Main"
```

*Ou simplesmente abra o projeto na sua IDE e execute a classe `src/main/java/br/com/cinereserve/Main.java`.*

---

## 🧪 Testes Automatizados

O projeto conta com cobertura de testes unitários e de integração local:
- `DominioTest`: valida criação de assentos, cálculo proporcional de tarifas e transições de status da reserva.
- `RepositoryJsonTest`: testa persistência, escrita e recarregamento dos arquivos sem perda de integridade.
- `ReservaServiceTest`: testa o fluxo completo de compra e cancelamento, além de uma simulação real de 10 threads concorrentes disputando o mesmo assento físico no mesmo milissegundo.
