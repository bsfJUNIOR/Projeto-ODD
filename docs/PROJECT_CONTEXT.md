# ODD – Organizador de Despesas
## Contexto técnico e funcional para continuidade do desenvolvimento com Codex

> **Objetivo deste arquivo:** servir como contexto técnico permanente para uma IA de desenvolvimento (Codex) atuar no projeto ODD.  
> Este documento diferencia explicitamente o que **já existe** do que é **planejado**. Quando houver conflito entre este documento e o código real, a IA deve analisar o código atual, apontar a divergência e não inventar uma solução.

**Última atualização:** 30/09/2026
**Repositório:** `bsfJUNIOR/Projeto-ODD`  
**Branch de referência:** `main`

---

# 1. Visão geral

O **ODD – Organizador de Despesas** é uma aplicação desktop desenvolvida em Java para facilitar a organização e o acompanhamento financeiro de forma simples e intuitiva.

O projeto está sendo desenvolvido como **Projeto Integrador do curso de Sistemas de Informação**. O CRUD atual representa a base funcional inicial; a intenção é evoluí-lo para uma solução financeira acadêmica mais completa, com acompanhamento financeiro, usuários, histórico, filtros e relatórios.

O sistema deve continuar sendo **desktop** durante o desenvolvimento e a entrega acadêmica atual. Não migrar para web, mobile ou outra arquitetura de aplicação sem decisão explícita.

O prazo atual é de aproximadamente **2 meses para finalizar o projeto**, incluindo desenvolvimento, testes, correções, documentação, diagramas e apresentação.

---

# 2. Objetivo do sistema

O ODD deve facilitar a organização de **despesas e receitas**, centralizando informações financeiras em uma aplicação desktop.

O problema que pretende resolver é a dificuldade de manter informações financeiras organizadas de forma prática, atualmente centralizando:

- receitas;
- despesas;
- vencimentos;
- situação das despesas;
- saldo;
- futuramente, histórico;
- futuramente, relatórios e indicadores.

A aplicação trabalha localmente e utiliza PostgreSQL como banco de dados.

---

# 3. Tecnologias e restrições

Stack atual:

- **Java 8**
- **Java Swing**
- **NetBeans 13**
- **Hibernate ORM**
- **PostgreSQL**
- **JPA Annotations**
- Aplicação **Desktop**

## Regra importante sobre tecnologia

Não trocar a stack apenas porque existem tecnologias mais modernas.

Não propor automaticamente:

- Spring Boot;
- JavaFX;
- React;
- Node.js;
- aplicação web;
- aplicação mobile;
- outro banco;
- outra linguagem.

Se uma mudança tecnológica realmente parecer necessária, primeiro explicar:

1. qual problema ela resolve;
2. qual o ganho;
3. qual o impacto no projeto;
4. qual o custo de migração;
5. se é compatível com o prazo;
6. se existe alternativa mais simples dentro da stack atual.

A solução deve ser **proporcional ao projeto**.

---

# 4. Funcionalidades atualmente implementadas

## Despesas

O sistema já possui:

- cadastro;
- listagem;
- edição;
- exclusão;
- definição do tipo;
- descrição opcional;
- valor;
- data de vencimento;
- status;
- identificação automática da situação de vencimento na tela principal.

O cadastro valida nome, valor numérico maior que zero, data válida no formato `dd/MM/yyyy`, tipo e status antes de persistir.

## Receitas

O sistema já possui:

- cadastro;
- listagem;
- edição;
- exclusão;
- tipo;
- descrição opcional;
- valor.

O cadastro valida nome, valor numérico maior que zero e tipo antes de persistir.

## Tela principal

A tela principal atualmente apresenta:

- total de receitas;
- total de despesas ainda devidas;
- saldo;
- tabela de vencimentos;
- despesas atrasadas;
- despesas próximas do vencimento;
- despesas em dia;
- despesas pagas.

Os totais exibidos consideram todos os registros cadastrados, não apenas o mês corrente. O cálculo atual é aproximadamente:

`Saldo = Total de Receitas - Total de Despesas não pagas`

Despesas com status persistido como `"Pago"` são excluídas do cálculo atual de despesas devidas.

**Não assumir que esse modelo financeiro é definitivo.** Ele deve ser revisado antes da implementação de relatórios e indicadores.

## Usuários e autenticação

Foi implementada a fundação de autenticação local:

- tela `Login`, aberta pelo ponto de entrada antes do menu;
- entidade persistida `Usuario`;
- gerenciamento de usuários para administradores, com listagem, cadastro, edição e exclusão confirmada;
- sessão simples em memória para o usuário autenticado;
- logout, que limpa a sessão e volta ao login.

No primeiro início com a tabela `usuarios` vazia, o sistema cria o administrador inicial `admin` com senha `admin`. Essa senha deve ser trocada pela tela de gerenciamento imediatamente após o primeiro acesso.

---

# 5. Funcionalidades planejadas – ainda NÃO implementadas

Estas funcionalidades são requisitos futuros e não devem ser tratadas como existentes:

- histórico/auditoria das ações;
- inatividade em vez de exclusão física;
- relatórios financeiros;
- filtros;
- separação clara entre valores pagos e valores ainda devidos;
- indicadores financeiros adicionais;
- outras melhorias identificadas durante a análise.

A IA deve sempre diferenciar **código existente** de **requisito futuro**.

---

# 6. Estrutura atual do projeto

A estrutura conhecida de `src` é:

```text
src/
├── entidades/
│   ├── Despesa.java
│   ├── NivelAcesso.java
│   ├── Receita.java
│   └── Usuario.java
│
├── persistencia/
│   └── HibernateUtil.java
│
├── seguranca/
│   └── SenhaUtil.java
│
├── servicos/
│   └── UsuarioService.java
│
├── sessao/
│   └── SessaoUsuario.java
│
├── sistemaodd/
│   └── SistemaODD.java (ponto de entrada; prepara o administrador inicial e abre Login)
│
└── telas/
    ├── CadastroDespesas.java
    ├── CadastroDespesas.form
    ├── CadastroReceita.java
    ├── CadastroReceita.form
    ├── GerenciamentoUsuarios.java
    ├── Login.java
    ├── MenuPrincipal.java
    └── MenuPrincipal.form
```

A estrutura já separa entidades, persistência, telas e inicialização, mas **não existe atualmente uma arquitetura formal completa em camadas**.

---

# 7. Arquitetura atual

O fluxo atual é essencialmente:

```text
Tela Swing
    ↓
HibernateUtil
    ↓
Hibernate Session
    ↓
PostgreSQL
```

As telas acessam diretamente o Hibernate e executam consultas HQL.

Isso funciona para o tamanho atual, mas cria acoplamento entre interface e persistência.

## Direção recomendada

A evolução deve considerar gradualmente algo próximo de:

```text
Telas
  ↓
Serviços / regras de negócio
  ↓
Camada de persistência / Repository
  ↓
Hibernate
  ↓
PostgreSQL
```

### Importante

Não criar camadas apenas para "parecer profissional".

Cada camada deve existir porque possui uma responsabilidade real.

A refatoração deve ser **incremental**, preservando o funcionamento atual.

---

# 8. Decisão sobre DAO

**DAO não faz parte da arquitetura atual.**

A ideia de DAO já foi discutida anteriormente, mas não deve ser reintroduzida automaticamente.

Se uma camada de persistência for necessária, o Codex deve avaliar uma solução simples e coerente, como Repository, em vez de criar DAO apenas por padrão de mercado.

Se recomendar Repository ou outra abstração, deve explicar:

- qual problema resolve;
- quais classes serão afetadas;
- por que melhora a manutenção;
- se a complexidade adicional vale a pena.

---

# 9. Entidade Despesa

Classe:

```java
@Entity
@Table(name = "despesas")
public class Despesa
```

Campos atuais:

| Campo | Tipo atual | Função |
|---|---|---|
| `id` | `Long` | identificador |
| `despesa` | `String` | nome definido pelo usuário |
| `tipo` | `String` | classificação `Fixa` ou `Variável` |
| `descricao` | `String` | opcional |
| `valor` | `Double` | valor da despesa |
| `dataVencimento` | `Date` | vencimento |
| `status` | `String` | situação da despesa |

ID:

```java
@GeneratedValue(strategy = GenerationType.IDENTITY)
```

Tabela:

```text
despesas
```

---

# 10. Entidade Receita

Classe:

```java
@Entity
@Table(name = "receita")
public class Receita
```

Campos atuais:

| Campo | Tipo atual | Função |
|---|---|---|
| `id` | `Long` | identificador |
| `nome` | `String` | nome da receita |
| `descricao` | `String` | opcional |
| `valor` | `Double` | valor |
| `tipo` | `String` | tipo da receita |

ID:

```java
@GeneratedValue(strategy = GenerationType.AUTO)
```

Tabela:

```text
receita
```

---

# 11. Banco de dados

Banco:

**PostgreSQL**

Atualmente existem três entidades/tabelas de negócio:

```text
despesas
receita
usuarios
```

Não existe relacionamento entre elas atualmente.

O Hibernate utiliza:

```text
hibernate.hbm2ddl.auto = update
```

e registra `Despesa`, `Receita` e `Usuario`. A conexão está configurada diretamente em `HibernateUtil` para `jdbc:postgresql://localhost:5432/Odd`; usuário e senha de desenvolvimento também estão no código e continuam uma pendência de segurança para a distribuição final.

## Regra importante

Não criar relacionamento entre Receita e Despesa apenas porque ambas participam do cálculo financeiro.

Um relacionamento deve existir somente se houver uma regra de negócio que o justifique.

---

# 12. Tipos de despesas e receitas

## Despesas

Não existem categorias pré-definidas.

O usuário define o nome da despesa e pode escrever uma descrição opcional.

O campo `tipo` armazena a classificação selecionada na interface entre:

```text
Fixa
Variável
```

## Receitas

Atualmente usam a mesma classificação:

```text
Fixa
Variável
```

Não assumir categorias de negócio fixas sem decisão explícita.

---

# 13. Status e vencimentos

A situação exibida na interface considera a data atual.

O status de pagamento persistido é limitado pela interface a:

```text
Pendente
Pago
```

A situação de vencimento calculada para a tela principal é:

```text
Pago
Atrasada
Vence em breve
Em dia
```

A lógica atual considera até **3 dias** como "vence em breve" e compara somente a data de vencimento; uma despesa com vencimento na data atual não é marcada como atrasada.

Existe uma diferença importante entre:

- o `status` persistido na entidade;
- o status calculado dinamicamente para exibição.

Antes de alterar essa regra, decidir se o status deverá:

1. ser persistido;
2. ser calculado;
3. ou usar uma combinação dos dois.

Não alterar essa regra sem analisar o impacto no cálculo financeiro, histórico e relatórios.

---

# 14. Exclusão

Atualmente exclusão física remove o registro do banco.

Existe uma intenção futura de substituir isso, onde fizer sentido, por **inatividade/exclusão lógica**, por exemplo:

```text
ativo = false
```

Isso ainda não está implementado.

Antes de implementar, definir:

- quais registros serão inativáveis;
- como serão exibidos;
- se poderão ser reativados;
- como relatórios tratarão inativos;
- como impedir que registros inativos apareçam nas listagens normais.

---

# 15. Usuários e histórico

## Implementado nesta etapa

`Usuario` é uma entidade JPA na tabela `usuarios`, com os campos `id`, `usuario`, `senha` e `nivelAcesso`. O nome de usuário é obrigatório e único; senha e nível também são obrigatórios. Os níveis são o enum `ADMIN` e `USUARIO`, persistido como texto.

`SenhaUtil` usa PBKDF2 com sal aleatório e `PBKDF2WithHmacSHA1`, disponível no Java 8. O banco armazena somente o resultado no formato `iterações:sal:hash`, nunca a senha informada.

`SessaoUsuario` mantém em memória somente ID, nome e nível do usuário autenticado. `Login` consulta o usuário pelo nome, valida a senha pelo hash e inicia essa sessão antes de abrir `MenuPrincipal`. O menu exibe o gerenciamento de usuários apenas para `ADMIN`. O logout limpa a sessão, fecha o menu e reabre o login. Não há restrição adicional sobre os CRUDs financeiros nesta etapa.

## Ainda não implementado

Histórico/auditoria das ações e regras de autorização mais amplas ainda são requisitos futuros.

---

# 16. Relatórios e filtros

Ainda não implementados.

Relatórios a avaliar:

- receitas por período;
- despesas por período;
- despesas pagas;
- despesas pendentes;
- despesas atrasadas;
- receitas versus despesas;
- saldo por período;
- despesas por tipo;
- despesas fixas versus variáveis;
- valores pagos;
- valores ainda devidos.

Filtros possíveis:

- período;
- status;
- tipo;
- valor;
- pagas;
- pendentes;
- receitas.

Não implementar todos automaticamente. Priorizar os que realmente agregam valor e cabem no prazo.

Filtros e relatórios devem compartilhar as mesmas regras de negócio sempre que possível.

---

# 17. Principais problemas técnicos já identificados

Estes pontos foram identificados na análise do código atual e devem ser considerados no planejamento.

## 🔴 Alta prioridade

### 17.1 Telas acessam Hibernate diretamente

As telas executam consultas e operações de persistência diretamente.

Problema:

- alto acoplamento;
- dificuldade de manutenção;
- regras espalhadas;
- maior dificuldade para testar;
- pior evolução para usuários, histórico e relatórios.

Recomendação: avaliar separação gradual em **Service + Repository/camada de persistência**, sem criar complexidade desnecessária.

### 17.2 Regras de negócio estão misturadas à interface

Cálculos financeiros, validações e manipulação de dados aparecem nas telas.

Recomendação: mover regras reais de negócio para serviços, mantendo a tela responsável principalmente pela apresentação e interação.

### 17.3 `Double` para valores monetários

Atualmente:

```java
Double valor;
```

`BigDecimal` é tecnicamente mais adequado para valores monetários.

**Não alterar automaticamente.** Primeiro avaliar impacto em entidade, banco, cálculos, formulários e relatórios.

### 17.4 Segurança das credenciais do banco

A configuração atual contém credenciais diretamente no código.

Isso é inadequado para uma aplicação profissional e deve ser corrigido antes da distribuição final.

As informações atuais são de desenvolvimento/fictícias segundo o desenvolvedor, portanto isso **não deve bloquear a refatoração imediata**, mas deve permanecer como pendência para a versão final.

Nunca introduzir credenciais reais no repositório.

---

## 🟠 Prioridade média

### 17.5 `String` para status/tipos

Avaliar se `enum` é apropriado para valores realmente fechados, como status ou classificação fixa.

Não transformar campos livres do usuário em enum.

### 17.6 Tratamento de datas

A aplicação utiliza `java.util.Date`/`SimpleDateFormat`.

Como o projeto está em Java 8, avaliar gradualmente `LocalDate` quando compatível com a versão do Hibernate e com o restante do código.

Não fazer uma migração ampla apenas por modernização.

### 17.7 Consultas que carregam todas as entidades

O dashboard atualmente busca registros e realiza cálculos em Java.

Para o volume acadêmico atual isso pode ser aceitável.

No futuro, avaliar agregações no banco quando houver necessidade real de desempenho.

Não otimizar prematuramente.

### 17.8 Componentes de interface

Existem pontos da interface que podem ser simplificados, como opções mutuamente exclusivas representadas por checkboxes.

Melhorias visuais/estruturais devem ser feitas sem destruir o formulário gerado pelo NetBeans.

### 17.9 Correções de estabilidade realizadas em 25/09/2026

- corrigida a edição de despesas, que não ativava o fluxo de `merge`;
- validações de valor e data passaram a impedir valores inválidos e datas inexistentes antes da persistência;
- as telas passaram a fechar sessões após consultas e, em falhas de escrita, executar rollback antes de fechar a sessão;
- a tabela de receitas deixou de registrar um novo listener de seleção a cada recarga;
- seleção e preenchimento de tabelas passaram a tolerar dados nulos legados;
- tabelas carregadas em tempo de execução são não editáveis e declaram os tipos efetivamente exibidos;
- os rótulos do resumo financeiro foram ajustados para não indicar incorretamente um recorte mensal;
- `SistemaODD` passou a iniciar a tela principal configurada para a aplicação.

---

# 18. Regras de atuação do Codex

O Codex deve atuar como **desenvolvedor sênior e mentor técnico**, não como um simples gerador de código.

## 18.1 Não concordar automaticamente

Se o usuário propuser uma solução ruim, desnecessariamente complexa ou inferior:

1. dizer claramente que não recomenda;
2. explicar o problema;
3. apresentar a alternativa;
4. justificar tecnicamente;
5. explicar os impactos;
6. só então seguir após decisão do usuário.

Não concordar apenas para agradar.

## 18.2 Não assumir que o usuário está sempre certo

As solicitações do usuário são requisitos/intenção, não decisões arquiteturais imutáveis.

O Codex deve questionar:

- arquitetura;
- banco;
- segurança;
- modelagem;
- desempenho;
- manutenibilidade;
- complexidade;
- escopo.

## 18.3 Não complicar o projeto sem necessidade

Evitar:

- padrões de projeto sem necessidade;
- abstrações excessivas;
- frameworks novos;
- camadas artificiais;
- overengineering;
- refatorações puramente estéticas.

A melhor solução para o ODD é a **mais simples que resolva corretamente o problema e permita evolução**.

## 18.4 Analisar antes de alterar

Antes de modificar arquivos:

1. localizar os arquivos envolvidos;
2. entender dependências;
3. entender o comportamento atual;
4. identificar impactos;
5. propor a solução;
6. implementar;
7. testar.

Para alterações significativas, apresentar um plano antes de executar.

## 18.5 Preservar funcionalidades

Não quebrar o CRUD existente.

Antes de refatorar:

```text
backup/commit
↓
alteração pequena
↓
compilação
↓
teste
↓
próxima alteração
```

## 18.6 Ensinar durante o desenvolvimento

Quando fizer uma mudança relevante, explicar:

- o que estava errado;
- por que a mudança é necessária;
- como a solução funciona;
- quais arquivos foram afetados;
- como testar;
- quais consequências existem.

O objetivo é também ajudar o estudante a aprender desenvolvimento profissional.

## 18.7 Não inventar contexto

Se algo não estiver no código ou neste documento:

- analisar o código;
- perguntar;
- ou declarar a incerteza.

Não inventar classes, tabelas, funcionalidades ou regras.

---

# 19. Prioridade recomendada de evolução

A ordem deve ser validada pelo Codex após analisar o código real, mas a direção inicial é:

## Fase 1 – Estabilizar a base

1. revisar CRUD;
2. corrigir inconsistências;
3. revisar validações;
4. revisar cálculos;
5. revisar status;
6. testar casos extremos;
7. criar commits estáveis.

## Fase 2 – Organizar arquitetura

1. identificar responsabilidades das telas;
2. separar regras de negócio;
3. avaliar Services;
4. organizar persistência;
5. reduzir acoplamento;
6. preservar simplicidade.

**Não criar arquitetura em camadas apenas por estética.**

## Fase 3 – Modelo financeiro

Definir claramente:

- receita total;
- despesa paga;
- despesa pendente;
- despesa atrasada;
- saldo;
- saldo disponível/projetado, se necessário.

Essas regras devem preceder relatórios.

## Fase 4 – Usuários

Projetar:

- entidade;
- autenticação;
- senha;
- sessão;
- permissões somente se necessárias.

## Fase 5 – Histórico

Definir:

- ações;
- usuário responsável;
- data/hora;
- registro afetado;
- tipo da ação.

## Fase 6 – Inatividade

Implementar exclusão lógica onde fizer sentido.

## Fase 7 – Filtros e relatórios

Implementar primeiro as regras/dados e depois as telas.

## Fase 8 – Acabamento

- validações;
- mensagens;
- tratamento de erros;
- usabilidade;
- consistência visual;
- testes;
- segurança;
- desempenho básico;
- documentação.

## Fase 9 – Documentação e diagramas

Diagramas devem representar **exatamente o sistema implementado**.

Nunca criar diagramas de funcionalidades inexistentes apenas para parecer mais completo.

---

# 20. O que NÃO deve ser assumido como existente

Não assumir que existem:

- DAO;
- login;
- usuários;
- permissões;
- histórico;
- relatórios;
- filtros;
- exclusão lógica;
- categorias pré-definidas;
- relacionamento entre Receita e Despesa;
- MVC completo;
- arquitetura em camadas completa;
- API;
- aplicação web;
- aplicativo mobile;
- funcionalidades que não estejam comprovadas no código.

---

# 21. Critérios para qualquer alteração

Antes de implementar algo, considerar:

### Correção
A funcionalidade atende ao requisito?

### Manutenibilidade
Outro desenvolvedor conseguiria entender?

### Coesão
A responsabilidade está na classe certa?

### Acoplamento
A alteração cria dependências desnecessárias?

### Segurança
Existe algum risco introduzido?

### Dados
A alteração preserva consistência?

### Compatibilidade
Funciona com Java 8, NetBeans 13, Hibernate e PostgreSQL atuais?

### Escopo
Vale a pena para um projeto acadêmico com aproximadamente 2 meses restantes?

### Testabilidade
É possível verificar se a alteração funcionou?

---

# 22. Git e segurança durante alterações

Antes de alterações estruturais importantes:

- verificar `git status`;
- garantir que não existam alterações não relacionadas;
- criar commit/backup;
- trabalhar em alterações pequenas;
- revisar diff;
- testar;
- só depois consolidar.

Não apagar ou substituir grandes partes do projeto sem entender o impacto.

---

# 23. Documentação do projeto

A documentação acadêmica deve acompanhar o sistema real.

Possíveis diagramas:

- caso de uso;
- classes;
- DER;
- sequência;
- implantação/deploy;
- WBS/organização.

Atualizar os diagramas quando a arquitetura ou funcionalidades mudarem.

Nunca representar como implementado algo que existe apenas como requisito futuro.

---

# 24. Estado do projeto em uma frase

> **O ODD atualmente é uma aplicação desktop Java 8/Swing com Hibernate e PostgreSQL, contendo CRUD de Despesas e Receitas, autenticação local por usuários, sessão e gerenciamento administrativo de usuários; histórico, relatórios, filtros e demais evoluções ainda serão desenvolvidos.**

---

# 25. Prioridade absoluta

Ao continuar o projeto:

1. **Não quebrar o CRUD existente.**
2. **Entender o código antes de refatorar.**
3. **Diferenciar implementação atual de requisito futuro.**
4. **Não introduzir DAO automaticamente.**
5. **Não trocar a stack sem necessidade.**
6. **Questionar decisões ruins ou pouco justificadas.**
7. **Ensinar durante a implementação.**
8. **Evitar overengineering.**
9. **Priorizar funcionalidades de maior valor.**
10. **Manter o escopo compatível com aproximadamente 2 meses.**
11. **Pensar nos relatórios antes de alterações importantes no modelo financeiro.**
12. **Preservar dados históricos quando inatividade/histórico forem implementados.**
13. **Corrigir a estratégia de credenciais antes da entrega/distribuição final.**
14. **Fazer a documentação refletir exatamente o sistema implementado.**
15. **Preferir alterações incrementais e verificáveis.**

---

# 26. Fonte do código atual

Repositório:

`https://github.com/bsfJUNIOR/Projeto-ODD`

O código real deve ser considerado a fonte de verdade sobre o estado de implementação. Este documento é o contexto funcional, técnico e de decisões do projeto e deve ser atualizado quando decisões importantes ou funcionalidades relevantes mudarem.

**Este arquivo deve ser mantido atualizado durante o desenvolvimento.**
