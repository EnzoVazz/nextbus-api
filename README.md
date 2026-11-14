# 🚌 NextBus — API do ônibus na hora de sair

## 📖 Sobre o Projeto

O **NextBus** é a API **Spring Boot** da Global Solution. Ela acompanha a rotina de quem pega ônibus em São Paulo: o cidadão grava a linha, o ponto e o horário, e a API avisa quantos minutos faltam para o embarque.

A previsão e a posição vêm da **Olho Vivo**. O aviso anda por uma fila **RabbitMQ**. A frase da tela é escrita pelo **Spring AI**, com os dados já calculados. Se o modelo não responder, entra uma frase padrão com os mesmos campos.

O projeto entrega a API REST consumida pelo aplicativo. O banco Oracle já existe: o Hibernate não cria nem altera as tabelas.

---

## 🧠 O que a API faz

### ⏱️ Aviso na janela da rotina

Vinte minutos antes do horário, o agendador publica a rotina na fila. Um consumidor consulta a previsão, escolhe o ônibus que chega mais perto do horário e publica o aviso. O outro grava o aviso do dia.

O `GET /avisos` separa o que o celular mostra:

| Campo | Uso na tela |
|---|---|
| `minutos` | Número grande |
| `textoCurto` | Notificação, sempre na frase fixa |
| `texto` | Frase da tela |
| `comunicado` | Faixa do aviso oficial da linha |

### 💬 Frase da tela

O Spring AI escreve `texto` com letreiro, ponto, minutos, idade do GPS e, se existir, o comunicado oficial. A base fica em `src/main/resources/comunicados.json`.

Sem `OPENAI_API_KEY`, ou se o modelo falhar ou devolver um texto em formato de ficha, `texto` usa a frase padrão. Essa frase não inventa acidente, desvio nem lotação. O comunicado só entra quando está vigente para a linha.

### 🔐 Dois papéis

O login devolve um JWT. O cidadão cuida das próprias rotinas, dos avisos e do planejamento. O gestor vê as linhas cujo GPS está velho. Quem não está logado recebe 401. Quem está no papel errado recebe 403.

### 🔁 Rotina sem duplicar

`POST /rotinas` exige o cabeçalho `Idempotency-Key`. A mesma chave, com os mesmos dados, devolve a rotina já criada. Com outros dados, a API recusa. Ida e volta são duas rotinas.

---

## 🛠️ Tecnologias

- **Java 17**
- **Spring Boot 4.1.2**
  - Spring Web
  - Spring Data JPA
  - Spring Security
  - Bean Validation
  - Spring AMQP
- **Spring AI 2.0.1**, com a OpenAI na frase do aviso
- **Oracle** pela FIAP, driver `ojdbc17`
- **RabbitMQ** local, usuário `guest`
- **JWT** com JJWT 0.12
- **Swagger** com Springdoc OpenAPI

---

## 🏗️ Estrutura

```text
nextbus/
│
├── db/
│   ├── 001-tabelas-e-carga.sql
│   ├── 002-senha-hash.sql
│   └── 003-idempotencia.sql
│
├── src/main/java/br/com/fiap/nextbus/
│   ├── config/          # Segurança, JWT, Swagger, RabbitMQ, relógio
│   ├── controller/      # Endpoints REST
│   ├── dto/             # Entrada e saída da API
│   ├── exception/       # Erros no mesmo formato
│   ├── messaging/       # Mensagens das filas
│   ├── model/           # Entidades JPA
│   ├── repository/      # Spring Data
│   └── service/         # Rotina, previsão, aviso e Olho Vivo
│
├── src/main/resources/
│   ├── catalogo/        # Linhas e paradas locais
│   ├── comunicados.json
│   └── application.properties
│
└── pom.xml
```

| Pasta | Responsabilidade |
|---|---|
| `config/` | Segurança, Swagger, filas e carga da senha de teste |
| `controller/` | Rotas REST, sem prefixo `/api` |
| `dto/` | Contratos de entrada e saída |
| `exception/` | Um único formato de erro |
| `messaging/` | `rotina.na-janela` e `aviso.atualizado` |
| `model/` | `USUARIO`, `ROTINA`, `AVISO` e a chave de idempotência |
| `repository/` | Consultas ao Oracle |
| `service/` | Regras da rotina, da previsão e do texto do aviso |

---

## 🔐 Papéis

| Papel | O que acessa |
|---|---|
| **CIDADAO** | Rotinas, avisos e `POST /viagens/planejar` |
| **GESTOR** | `GET /operacao/linhas-sem-gps` |
| Público | `POST /auth/registrar`, `POST /auth/login`, `GET /health` e o Swagger |

Uma conta nova, criada em `POST /auth/registrar`, nasce como cidadão.

### 🔑 Usuários de teste

Senha dos dois: `123456`.

| Nome | Email | Papel |
|---|---|---|
| Marina Costa | marina.costa@email.com | CIDADAO |
| Ricardo Nunes | ricardo.nunes@email.com | GESTOR |

A Marina já tem ida às 07:15, linha `7021-10`, ponto Roberto Selmi Dei, e volta às 18:10 no Terminal Pirituba.

---

## 🚀 Como executar

### Pré-requisitos

- JDK 17
- Oracle da FIAP, com as tabelas do script `db/001-tabelas-e-carga.sql` já criadas
- RabbitMQ em `localhost:5672`

O RabbitMQ pode ser o serviço do Windows. O `docker compose` da pasta do projeto só sobe o broker, e o Docker não é obrigatório. Usuário e senha locais: `guest` / `guest`.

### Variáveis no Eclipse

Na aba Environment da `NextbusApplication`:

| Variável | Valor |
|---|---|
| `DB_URL` | `jdbc:oracle:thin:@oracle.fiap.com.br:1521:ORCL` |
| `DB_USERNAME` | usuário da FIAP |
| `DB_PASSWORD` | senha do Oracle |
| `SPTRANS_TOKEN` | chave da Olho Vivo |
| `OPENAI_API_KEY` | chave da OpenAI, para a frase da tela |

As chaves ficam só no ambiente. Não entram no código.

### Subir

Abra o projeto no Eclipse e rode `NextbusApplication`. No computador, a API fica em `http://localhost:8080`.

O Swagger fica em:

```text
http://localhost:8080/swagger-ui.html
```

### Olho Vivo

Com `SPTRANS_TOKEN`, a previsão e a posição vêm da Olho Vivo. O relógio do aviso é o campo `hr`, o ônibus acompanhado é o campo `p`, e os minutos são a chegada prevista menos esse relógio. A consulta fica em cache por cerca de quinze segundos. O login da Olho Vivo é refeito quando a sessão expira.

Se o token não existir ou a Olho Vivo não responder, entra a demonstração: três ônibus em volta do horário da rotina, 13 minutos antes, 3 minutos depois e 26 minutos depois. O escolhido é o das 3 minutos depois. Numa rotina às 7h15, o aviso segue o das 7h18.

O `GET /health` mostra `olhoVivo` como `ok` quando o login responde, `demonstracao` sem token e `indisponivel` quando o login falha. `banco` mostra se o Oracle respondeu. A busca de linhas e o planejamento de paradas continuam no catálogo local.

### Ver um aviso

Entre como a Marina, cadastre uma rotina para daqui a cerca de 20 minutos, no dia da semana de hoje, e espere cerca de meio minuto. O `GET /avisos` devolve o aviso do dia.

---

## 📋 Endpoints

### Acesso

| Método | Caminho | Quem |
|---|---|---|
| POST | `/auth/registrar` | público |
| POST | `/auth/login` | público |
| GET | `/auth/eu` | autenticado |

### Rotina e aviso

| Método | Caminho | Quem |
|---|---|---|
| GET, POST | `/rotinas` | cidadão |
| GET, PUT, DELETE | `/rotinas/{id}` | cidadão, só as dele |
| GET | `/avisos` | cidadão |

### Mapa e previsão

| Método | Caminho | Quem |
|---|---|---|
| GET | `/linhas?q=` | autenticado |
| GET | `/paradas/proximas?lat&lng` | autenticado |
| GET | `/linhas/{codigo}/posicoes` | autenticado |
| GET | `/previsao?parada&linha` | autenticado |
| POST | `/viagens/planejar` | cidadão |

### Operação e saúde

| Método | Caminho | Quem |
|---|---|---|
| GET | `/operacao/linhas-sem-gps` | gestor |
| GET | `/health` | público |

As consultas de leitura repetidas em sequência recebem 429. Os erros de validação saem num único formato, com `status`, `erro` e `mensagens`.

---

## 🐇 RabbitMQ

A opção distribuída é a A. O agendador publica em `nextbus.rotina.na-janela` quando a rotina entra nos 20 minutos anteriores ao horário. Um consumidor escolhe o ônibus e publica em `nextbus.aviso.atualizado`. O outro grava o aviso.

Login, rotina, linhas, paradas, previsão, planejamento e o painel do gestor funcionam sem o broker. Sem o RabbitMQ, o aviso de chegada não anda na fila.

---

## 🤖 Declaração de uso de IA

Ferramenta: **Cursor**

Usamos o Cursor para explicar erros e revisar trechos pontuais: o login da Olho Vivo (sessão e cálculo dos minutos), o 409 da idempotência no `POST /rotinas` e a frase do aviso quando o modelo devolvia um texto em formato de ficha. O grupo leu e revisou o que ficou no código.

---

## 👥 Integrantes

Turma **2TDSPF**.

| Integrante | RM |
|---|---|
| Enzo Vaz | RM 561702 |
| Lucas Ryuji Fukuda | RM 562152 |
| Pietro Donela Salomão | RM 561722 |
