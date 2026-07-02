# Sistema de Gestão ASSANDEF

Este sistema web foi desenvolvido para a **ASSANDEF (Associação Santanense do Deficiente Físico)** como parte do projeto integrador da disciplina de Práticas em ADS do IFSul. O objetivo principal é a informatização de processos administrativos, financeiros e de comunicação da instituição.

---

## 📑 Índice
1. [Visão Geral](#-visão-geral)
2. [Tecnologias Utilizadas](#-tecnologias-utilizadas)
3. [Arquitetura e Módulos](#-arquitetura-e-módulos)
4. [Requisitos Funcionais](#-requisitos-funcionais)
5. [Requisitos Não Funcionais](#-requisitos-não-funcionais)
6. [Segurança e Perfis](#-segurança-e-perfis)
7. [Estrutura do Banco de Dados](#-estrutura-do-banco-de-dados)
8. [Documentação da API](#-documentação-da-api)
9. [Instalação e Execução](#-instalação-e-execução)

---

## 🏛️ Visão Geral
O projeto visa substituir controles manuais por uma plataforma centralizada que atenda tanto às necessidades internas (gestão de pacientes e financeiro) quanto externas (divulgação de eventos e aluguel de salão).

### Objetivos Principais:
- Informatização do controle financeiro (receitas e despesas).
- Gestão de publicações (notícias e eventos).
- Automação de solicitações de aluguel de salão.
- Facilitação do cadastro de novos doadores.

---

## 💻 Tecnologias Utilizadas

### Backend
- **Linguagem:** Java 21
- **Framework:** Spring Boot 3+
- **Segurança:** Spring Security (Session & JWT)
- **Persistência:** Spring Data JPA / Hibernate

### Frontend
- **Template Engine:** Thymeleaf
- **Estilização:** Bootstrap 5, HTML5 e CSS3
- **Scripting:** JavaScript (Vanilla/jQuery)

### Banco de Dados
- **SGBD:** MySQL

### Ferramentas e Relatórios
- **Gerador de PDF:** iText / JasperReports
- **Exportação:** CSV

---

## 🏗️ Arquitetura e Módulos

O sistema é dividido em duas grandes áreas:

### 1. Área Pública
- **Página Inicial/Sobre:** Informações institucionais.
- **Página de Doadores:** Cadastro de interessados em contribuir.
- **Portal de Publicações:** Galeria de fotos, vídeos e notícias.
- **Aluguel de Salão:** Consulta de disponibilidade e envio de solicitações.

### 2. Área Administrativa
- **Gestão de Pacientes e Atendimentos:** Controle de prontuários.
- **Almoxarifado:** Controle de estoque de insumos.
- **Módulo Financeiro:** Gestão de contas bancárias e fluxo de caixa.
- **Gestão de Funcionários:** Controle de usuários do sistema.

---

## 🛠️ Requisitos Funcionais

### Financeiro
- **RF-01:** Cadastro de contas bancárias.
- **RF-02:** Cadastro de categorias financeiras (saúde, manutenção, doação).
- **RF-03:** Registro de entradas (receitas) e saídas (despesas).
- **RF-04:** Geração de relatórios financeiros em PDF e CSV.

### Publicações
- **RF-05:** Gerenciamento de postagens (Título, Descrição, Data).
- **RF-06:** Upload de múltiplas fotos e links de vídeos para cada postagem.
- **RF-07:** Controle de status (Rascunho/Publicado).

### Aluguel de Salão
- **RF-08:** Cadastro de planos de aluguel (valores e descrições).
- **RF-09:** Calendário de datas ocupadas.
- **RF-10:** Formulário de solicitação de reserva para usuários externos.

---

## ⚙️ Requisitos Não Funcionais
- **RNF-01 (Responsividade):** A interface deve se adaptar a dispositivos móveis e desktops.
- **RNF-02 (Segurança):** Senhas devem ser criptografadas (BCrypt).
- **RNF-03 (Acessibilidade):** Seguir as diretrizes do WCAG para facilitar o acesso de pessoas com deficiência.
- **RNF-04 (Autenticação):** Uso de JWT para endpoints de API e Session para o Portal Web.

---

## 🔐 Segurança e Perfis

O acesso é controlado por roles (RBAC):
- **ROLE_DIRETORIA:** Acesso total ao sistema, incluindo relatórios e funcionários.
- **ROLE_SECRETARIA:** Foco em atendimentos, pacientes e gestão do salão.
- **ROLE_ADMINISTRATIVO:** Foco em financeiro, almoxarifado e doadores.
- **ANONYMOUS:** Visualização da página institucional e envio de solicitações.

---

## 🗄️ Estrutura do Banco de Dados

Principais entidades do sistema:
- `contas_bancarias`: Dados das contas da instituição.
- `categoria_financeira`: Classificação de entradas e saídas.
- `movimentacoes_financeiras`: Registros de fluxo de caixa.
- `publicacoes`: Tabela mestre para notícias e eventos.
- `publicacoes_imagens`: Armazenamento de caminhos de arquivos de imagem.
- `solicitacoes_aluguel_salao`: Registros de pedidos de reserva feitos pelo público.

---

## 🔌 Documentação da API

### Autenticação
- `POST /api/auth/login`: Autentica o usuário e retorna um Token JWT.

### Recuperação de Senha
- `POST /api/auth/password/forgot`: Solicita link de recuperação por e-mail.
- `POST /api/auth/password/reset`: Define nova senha via token.

---

## 🚀 Instalação e Execução

### Pré-requisitos
- JDK 21
- Maven 3.8+
- MySQL 8.0

### Configuração
1. Clone o repositório:
   ```bash
   git clone https://github.com/usuario/assandef-sistema.git
   ```
2. Configure as credenciais do banco de dados no arquivo `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/db_assandef
   spring.datasource.username=seu_usuario
   spring.datasource.password=sua_senha
   ```
3. Compile e rode a aplicação:
   ```bash
   mvn spring-boot:run
   ```

---

## 👨‍💻 Desenvolvedores
Projeto desenvolvido para a disciplina de **Práticas em ADS II** - Instituto Federal Sul-rio-grandense (IFSul).

- *Instituição:* ASSANDEF (Associação Santanense do Deficiente Físico)
- *Local:* Santana do Livramento - RS
