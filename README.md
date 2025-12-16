# Assandef System

Sistema web para gestão da ASSANDEF, incluindo:

- **Módulo de Atendimentos**
- **Módulo de Pacientes**
- **Módulo de Doadores**
- **Módulo de Almoxarifado (Estoque e Solicitações de Materiais)**
- **Relatórios em PDF/CSV**
- **Autenticação com Spring Security**

A aplicação foi desenvolvida com **Spring Boot**, **Spring Data JPA**, **Thymeleaf**, **MySQL** e empacotada em **Docker** para facilitar o deploy.

---

## Tecnologias Utilizadas

- **Backend**
  - Java 21
  - Spring Boot
  - Spring MVC
  - Spring Data JPA
  - Spring Security
  - Lombok (opcional, dependendo da sua configuração)
  - iText (lowagie) para geração de PDF

- **Frontend**
  - Thymeleaf
  - Bootstrap 5
  - Bootstrap Icons
  - JavaScript (para modais e interações)

- **Banco de Dados**
  - MySQL

- **Infraestrutura**
  - Docker / Docker Compose

---

## Estrutura Geral dos Módulos

### 1. Atendimentos

Funcionalidades principais:

- Cadastro e gerenciamento de **Atendimentos**.
- Associação de **Pacientes** e **Profissionais (Funcionários)**.
- Registro de:
  - Tipo de encaminhamento
  - Data/Hora de início e fim
  - Status (por exemplo: EM_ANDAMENTO, FINALIZADO)
- Evolução do atendimento (**Evolução**) e respectivas **Prescrições**.

Relatórios:

- **Relatório geral de atendimentos** (PDF/CSV) com filtro por período.
- **Relatório individual do paciente**:
  - Dados cadastrais do paciente
  - Históricos de atendimentos
  - Evoluções e prescrições
- **Ficha de atendimento (Folha de produção)**:
  - Gera um PDF com layout baseado no formulário físico
  - Logo da ASSANDEF no topo
  - Médico, Posto e Data
  - Tabela com até 16 linhas, preenchendo automaticamente com os pacientes atendidos no dia informado (nome e endereço).

Endpoints principais:

- `GET /atendimento` – página principal de atendimentos (Thymeleaf).
- `GET /atendimento/relatorio/dados` – dados em JSON para relatórios.
- `POST /atendimento/relatorio/gerar` – geração de relatórios (PDF/CSV).
- `GET /atendimento/relatorio/paciente/{idPaciente}` – relatório completo de um paciente (PDF ou CSV).
- `GET /atendimento/relatorio/ficha-atendimento` – ficha de atendimento (PDF), com parâmetros:
  - `data` (obrigatório para uso via modal)
  - `medico` (opcional)
  - `posto` (opcional, padrão “ASSANDEF”).

No frontend existe um **modal** para escolher a data e chamar a geração da ficha.

---

### 2. Pacientes

Funcionalidades:

- Cadastro de pacientes (dados pessoais, endereço, telefones, etc.).
- Vínculo com atendimentos, evoluções e prescrições.
- Telefones múltiplos por paciente (com descrição).

Relatórios:

- Incluídos no **relatório individual do paciente**, via `/atendimento/relatorio/paciente/{idPaciente}`:
  - Dados do paciente
  - Lista de atendimentos
  - Evoluções e prescrições.

---

### 3. Doadores

Funcionalidades:

- Cadastro de doadores (Pessoa Física ou Jurídica).
- Dados como:
  - Nome
  - CPF/CNPJ
  - Telefone
  - E-mail
  - Endereço
  - Sexo
  - Data de nascimento
  - Data de cadastro
  - Valor de mensalidade
  - Dia de vencimento

Relatórios:

- **Relatório de doadores** por período (PDF/CSV).
- **Ficha de parceiro** (documentação individual do doador) em PDF:
  - Nome, CPF/CNPJ, email, telefone, endereço, datas, valor da contribuição
  - Número de documento
  - Espaço para assinatura do contribuinte
  - Missão da instituição no rodapé.

Endpoints relevantes:

- `GET /doadores`
- `GET /doadores/relatorio/dados`
- `POST /doadores/relatorio/gerar`
- `GET /doadores/relatorio/documentacao/{id}` – ficha individual do doador.

---

### 4. Almoxarifado (Estoque e Solicitações de Material)

Funcionalidades:

- Cadastro de **Materiais**, com:
  - Nome
  - Categoria
  - Quantidade atual
  - Data de validade
  - Fornecedor

- **Solicitações de Material**, com:
  - Material
  - Quantidade solicitada
  - Funcionario solicitante
  - Data da solicitação
  - Tipo de saída
  - Status (ABERTA, APROVADA, NEGADA, etc.)
  - Justificativa/descrição

- Regras de negócio:
  - Ao **aprovar** uma solicitação, o sistema reduz o estoque do material.
  - Validações para não permitir retirada se não houver quantidade suficiente.

Relatórios:

- **Relatório de estoque** (PDF/CSV).
- **Relatório de solicitações de material** (por período, PDF/CSV).

Endpoints (via `RelatorioController`):

- `GET /almoxarifado/relatorio/dados`
- `POST /almoxarifado/relatorio/gerar`

---

### 5. Autenticação e Segurança

- Implementado com **Spring Security**.
- Login via página customizada (Thymeleaf).
- Usuário precisa estar autenticado para acessar a maior parte das funcionalidades.
- Exceções configuradas para links públicos, como “Tornar-se Doador”.
- Senhas armazenadas com **BCrypt**.

---

### 6. Geração de Relatórios

Centralizado no `RelatorioController`, com `@RequestMapping`:

```java
@RequestMapping({"/almoxarifado/relatorio", "/doadores/relatorio", "/atendimento/relatorio"})
```

Suporta:

- **CSV**  
  - Estoque  
  - Solicitações  
  - Doadores  
  - Atendimentos  
  - Relatório detalhado de paciente

- **PDF**
  - Relatório de estoque, solicitações, doadores e atendimentos (tabelas em paisagem).
  - Ficha de parceiro (doador).
  - Relatório detalhado de paciente.
  - Ficha de atendimento (folha de produção) por dia.

---

## Executando Localmente com Docker

Pré-requisitos:

- Docker
- Docker Compose

### 1. Arquivos principais

- `Dockerfile` – build da aplicação (etapa Maven + JRE).
- `docker-compose.yml` – sobe:
  - container do **MySQL**
  - container da aplicação Spring Boot

Exemplo de `docker-compose.yml` (ajuste se o seu for diferente):

```yaml
services:
  mysql:
    image: 'mysql:latest'
    container_name: assandef-mysql
    environment:
      - MYSQL_DATABASE=assandef_db
      - MYSQL_PASSWORD=secret
      - MYSQL_ROOT_PASSWORD=verysecret
      - MYSQL_USER=myuser
    volumes:
      - assandef-mysql-data:/var/lib/mysql
    networks:
      - assandef-net

  app:
    build: .
    container_name: assandef-app
    depends_on:
      - mysql
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/assandef_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      - SPRING_DATASOURCE_USERNAME=myuser
      - SPRING_DATASOURCE_PASSWORD=secret
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
      - SPRING_PROFILES_ACTIVE=prod
    ports:
      - "8081:8080"   # acessa via http://localhost:8081
    networks:
      - assandef-net

volumes:
  assandef-mysql-data:

networks:
  assandef-net:
    driver: bridge
```

### 2. Build e execução

Na raiz do projeto (onde está `docker-compose.yml`):

```bash
docker-compose build --no-cache
docker-compose up
```

Acesse:

- Aplicação: http://localhost:8081

---

## Deploy na Nuvem (Hostinger) — Tutorial Resumido

Existem alguns jeitos diferentes de publicar na Hostinger. O mais simples para este projeto, que já está em Docker, é:

- Usar uma **VPS na Hostinger** (Ubuntu),
- Instalar Docker + Docker Compose,
- Clonar o projeto e rodar o `docker-compose up -d`.

### 1. Contratar e acessar uma VPS

1. No painel da Hostinger, contrate uma **VPS Linux (Ubuntu)**.
2. Depois que a VPS estiver pronta, acesse por **SSH**:

```bash
ssh root@SEU_IP_DA_VPS
```

(use a senha ou chave SSH que a Hostinger fornecer).

### 2. Instalar Docker e Docker Compose na VPS

No SSH da VPS (como root):

```bash
# Atualizar pacotes
apt update && apt upgrade -y

# Instalar Docker
apt install -y docker.io

# Habilitar Docker na inicialização
systemctl enable docker
systemctl start docker

# Instalar docker-compose (caso não venha junto)
apt install -y docker-compose
```

Confirme se está tudo ok:

```bash
docker --version
docker-compose --version
```

### 3. Copiar o projeto para a VPS

Opção A – Clonar do Git (recomendado se você tem o projeto em repositório):

```bash
cd /opt
git clone https://SEU_REPOSITORIO.git assandef-system
cd assandef-system
```

Opção B – Upload do código via painel da Hostinger ou SFTP, jogando os arquivos do projeto em alguma pasta (por exemplo `/opt/assandef-system`).

### 4. Configurar variáveis de ambiente para produção

Edite o arquivo `docker-compose.yml` na VPS (por exemplo com `nano`):

```bash
nano docker-compose.yml
```

Ajuste:

- `MYSQL_PASSWORD`, `MYSQL_ROOT_PASSWORD`, `MYSQL_USER`
- `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
- `SPRING_JPA_HIBERNATE_DDL_AUTO` (em produção pode ser `none` depois de estar estável)

Salve o arquivo.

### 5. Subir os containers em segundo plano

Na pasta do projeto:

```bash
docker-compose build
docker-compose up -d
```

Verifique os containers:

```bash
docker ps
```

Se tudo estiver certo, a aplicação estará rodando na porta 8081 da VPS:

- `http://SEU_IP_DA_VPS:8081`

### 6. Configurar domínio e HTTPS (opcional, mas recomendado)

1. No painel da Hostinger, aponte o seu domínio (ex.: `sistema.assandef.org.br`) para o IP da VPS, criando um **registro A**.
2. Instale e configure um proxy reverso **NGINX** na VPS para:
   - Redirecionar `80` e `443` para o container da aplicação.
   - Gerar certificado SSL (pode usar **Certbot/Let’s Encrypt**).

Passos bem resumidos:

```bash
apt install -y nginx

# arquivo de configuração simples em /etc/nginx/sites-available/assandef
```

Exemplo mínimo de bloco de servidor (sem SSL, apenas para testar):

```nginx
server {
    listen 80;
    server_name sistema.assandef.org.br;

    location / {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

Ativar o site:

```bash
ln -s /etc/nginx/sites-available/assandef /etc/nginx/sites-enabled/assandef
nginx -t
systemctl restart nginx
```

Depois você pode rodar o **Certbot** para habilitar HTTPS.

---

## Desenvolvimento Local (sem Docker) – Opcional

Se você quiser rodar localmente usando apenas o Spring Boot (por exemplo via IntelliJ):

1. Configurar o `application.properties` com as credenciais do MySQL local.
2. Rodar a aplicação pela classe `@SpringBootApplication` principal.
3. Acessar: `http://localhost:8080`.

> Lembrar de não conflitar com a porta usada pelo container Docker se estiver rodando ao mesmo tempo.

---

## Contribuição e Manutenção

- Ajuste os controllers, services e templates conforme a evolução das necessidades da ASSANDEF.
- Para mudanças no banco, use **migrations** (Flyway/Liquibase) em produção.
- Atualize este README sempre que adicionar novos módulos ou endpoints importantes.

---
