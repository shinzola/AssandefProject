# Sistema de Gestão ASSANDEF

> Documentação consolidada baseada em dois documentos do projeto:
>
> 1. Documentação Técnica – Sistema de Gestão ASSANDEF (2025)
> 2. Documento de Requisitos – Sistema de Gestão ASSANDEF – Práticas II (2026)

## Sumário

1. Introdução
2. Propósito do Documento
3. Visão Geral do Sistema
4. Definições e Siglas
5. Arquitetura de Software
6. Stack Tecnológica
7. Módulos do Sistema (Práticas I)
   - Gestão de Pacientes
   - Atendimento
   - Doadores
   - Almoxarifado
   - Funcionários
8. Estrutura do Banco de Dados
9. Requisitos (Resumo Práticas I)
10. Manual do Usuário (Resumo)
11. Evolução do Projeto – Práticas II
12. Descrição Geral do Sistema
13. Usuários e Hierarquia
14. Novos Requisitos Funcionais
    - Página Institucional
    - Financeiro
    - Cadastro de Doadores
    - Publicações
    - Aluguel de Salão
15. Requisitos Não Funcionais
16. Endpoints
17. Interfaces
18. Banco de Dados Atualizado
19. Considerações Finais

---

# 1. Introdução

O Sistema de Gestão ASSANDEF é uma aplicação web desenvolvida para apoiar a gestão administrativa da Associação Santanense do Deficiente Físico (ASSANDEF). O projeto surgiu na disciplina Práticas em Análise e Desenvolvimento de Sistemas em Sociedade e foi expandido em uma segunda etapa, agregando novos módulos administrativos e públicos.

# 2. Propósito do Documento

Esta documentação reúne em um único material a documentação técnica inicial (Práticas I) e a documentação de requisitos da continuação do projeto (Práticas II), servindo como referência para desenvolvimento, manutenção, testes e evolução.

# 3. Visão Geral do Sistema

O sistema centraliza processos administrativos da instituição, incluindo:

- Cadastro de pacientes
- Atendimento
- Doadores
- Almoxarifado
- Funcionários
- Financeiro
- Publicações
- Página institucional
- Aluguel de salão
- API JWT
- Recuperação de senha

Seu objetivo é digitalizar processos internos, melhorar a organização da instituição e disponibilizar serviços públicos para a comunidade.

# 4. Definições e Siglas

- API — Interface de Programação de Aplicações
- BCrypt — Algoritmo de hash de senhas
- Bootstrap — Framework CSS
- CRUD — Create, Read, Update e Delete
- CSV — Arquivo separado por vírgulas
- DER — Diagrama Entidade-Relacionamento
- Hibernate — ORM Java
- JWT — JSON Web Token
- MySQL — Banco de dados relacional
- PDF — Portable Document Format
- RBAC — Controle de acesso baseado em papéis
- Spring Boot — Framework Java
- Spring Security — Framework de autenticação
- Thymeleaf — Template Engine
- WCAG — Diretrizes de acessibilidade

# 5. Arquitetura de Software

Arquitetura monolítica com renderização Server Side (SSR).

Backend:
- Java
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA

Frontend:
- Thymeleaf
- Bootstrap
- JavaScript

Banco:
- MySQL

# 6. Stack Tecnológica

- Java 21
- Spring Boot
- Spring Security
- Hibernate
- Spring Data JPA
- Thymeleaf
- Bootstrap 5
- Bootstrap Icons
- MySQL 8+
- Tomcat Embedded

# 7. Módulos do Sistema (Práticas I)

## Gestão de Pacientes
- CRUD de pacientes
- Telefones
- Responsáveis
- Busca
- Relatórios

## Atendimento
- Triagem
- Encaminhamento
- Evoluções
- Prescrições
- Histórico
- Relatórios PDF/CSV

## Doadores
- Cadastro
- Gestão de boletos
- Upload de PDF
- Controle de status
- Relatórios

## Almoxarifado
- Categorias
- Materiais
- Solicitações
- Aprovação
- Controle de estoque
- Relatórios

## Funcionários
- Login
- Controle de acesso
- CRUD
- Hierarquia

# 8. Estrutura do Banco de Dados

Entidades originais:
- pacientes
- telefones
- atendimentos
- evolucoes
- prescricoes
- doadores
- boletos
- categorias
- materiais
- solicitacoes_material
- funcionarios

# 9. Requisitos (Resumo Práticas I)

Incluem gestão de pacientes, almoxarifado, atendimento, doadores, boletos, RBAC, autenticação segura, relatórios, HTTPS e acessibilidade.

# 10. Manual do Usuário (Resumo)

O sistema possui cinco módulos principais na primeira versão: Almoxarifado, Atendimento, Pacientes, Doadores e Funcionários, cada um com telas de consulta, cadastro, edição, exclusão e geração de relatórios conforme o perfil de acesso.

# 11. Evolução do Projeto – Práticas II

A segunda etapa amplia significativamente o sistema com módulos públicos e administrativos.

# 12. Descrição Geral do Sistema

Foram adicionados:
- Página institucional
- Financeiro
- Publicações
- Aluguel de salão
- Recuperação de senha
- API JWT
- Melhorias na página de doadores

# 13. Usuários e Hierarquia

Perfis:
- Diretoria
- Secretaria
- Administrativo
- Usuário Externo

Permissões conforme o documento de requisitos, restringindo acesso aos módulos conforme o papel.

# 14. Novos Requisitos Funcionais

## Página Institucional
- Página pública
- Informações institucionais
- Navegação integrada

## Financeiro
- Contas bancárias
- Categorias
- Receitas
- Despesas
- Consulta de contas
- Exportação PDF e CSV

## Cadastro de Doadores
- Página pública aprimorada
- Informações institucionais
- Imagens

## Publicações
- CRUD
- Imagens
- Vídeos YouTube
- Tipos (notícia, evento, artigo)
- Publicar
- Ocultar
- Visualização pública

## Aluguel de Salão
- Planos
- Fotos
- Calendário
- Solicitação pública
- Gestão administrativa
- Controle de status

# 15. Requisitos Não Funcionais

- Interface intuitiva
- Padronização visual
- Responsividade
- WCAG 2.1
- RBAC
- JWT
- Sessão Web
- Recuperação de senha
- Desempenho
- Exportação PDF/CSV

# 16. Endpoints

## Públicos
/
/login
/sobre
/publicacoes
/doadores/newdonation
/aluguel-salao
/api/auth/login

## Financeiro
/financeiro
/financeiro/contas
/financeiro/movimentacoes
/financeiro/categorias

## Publicações
/gestao/publicacoes

## Aluguel
/aluguel-salao/gestao

## Recuperação
/esqueci-senha
/password/forgot
/password/reset
/api/auth/password/forgot
/api/auth/password/reset

# 17. Interfaces

São descritas as telas de:
- Página Institucional
- Financeiro
- Publicações
- Aluguel de Salão (pública e administrativa)
- Cadastro de Doadores

# 18. Banco de Dados Atualizado

Novas entidades:
- contas_bancarias
- categoria_financeira
- movimentacoes_financeiras
- publicacoes
- publicacoes_imagens
- publicacoes_videos
- planos_aluguel_salao
- fotos_salao
- solicitacoes_aluguel_salao

Mantém compatibilidade com as tabelas da primeira etapa.

# 19. Considerações Finais

A evolução do Sistema de Gestão ASSANDEF amplia a solução originalmente criada para gestão administrativa, incorporando funcionalidades financeiras, comunicação institucional, aluguel de salão, autenticação moderna e recursos públicos. O sistema mantém uma arquitetura baseada em Spring Boot, MySQL e Thymeleaf, preservando compatibilidade com os módulos desenvolvidos anteriormente e consolidando uma plataforma única para apoiar as atividades da ASSANDEF.

# 20. Desenvolvido por
- *Rodrigo Noelli Duarte*
- *Pedro Rodrigues Coelho*
- *Bruno Andres (ADS I)*
- *William Ucha Giordano (ADS I)*
