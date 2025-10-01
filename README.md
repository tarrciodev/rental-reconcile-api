# 🏦 Reconcile API - Serviço de Reconciliação Bancária Automatizada
Esta API é o backend central para o serviço de reconciliação automática de extratos bancários (PDF) e transações internas da empresa (Excel). O projeto utiliza um algoritmo de similaridade para emparelhar transações com base em valor, data e descrição.

O frontend para interação e upload de ficheiros pode ser acedido em:

🔗 https://reconcile-front.vercel.app/

## ✨ Tecnologias Utilizadas
Este projeto foi construído utilizando um stack moderno e eficiente, focado em estabilidade e automação:

Categoria	Tecnologia	Uso Principal
Backend Core	Java 17 / Spring Boot	Estrutura RESTful e lógica de negócios.
Banco de Dados	PostgreSQL (pgvector)	Armazenamento de dados transacionais e vetores de descrição.
Automação/Fluxo	n8n	Automação e orquestração de fluxos de trabalho externos.
Inteligência	Mistral AI API	Processamento de linguagem natural (NLP) para análise de descrições.
Infraestrutura	Docker / Hetzner Cloud	Contentorização e deploy na nuvem.

A reconciliação é realizada através de uma combinação de regras estritas e um algoritmo de similaridade para encontrar correspondências prováveis:

**Valor**: O valor da transação deve ser exatamente igual.

**Data**: As datas das transações devem ter uma distância máxima de 3 dias.

**Descrição**: É utilizado um algoritmo de similaridade semântica para avaliar quão semelhantes são as descrições da transação bancária e da transação interna da empresa.

## Acertividade do Algoritmo
O algoritmo de similaridade de descrições, potencializado por modelos de linguagem (Mistral AI), permite acertar em muitos casos onde as descrições não são idênticas, mas representam a mesma operação.

O grau de acertividade ainda não é 100%, pois a análise de texto é complexa. Com mais tempo e dados de treino, o algoritmo pode ser refinado e aperfeiçoado para alcançar uma precisão superior.

## 🚀 Como Executar Localmente (Docker)
Para executar a API e o banco de dados PostgreSQL localmente, clone o repositório e use o Docker Compose:

Clone o repositório:

Bash

git clone [SEU_REPO_URL]
cd reconcile-api
Configure o .env: Crie um ficheiro .env com as suas chaves de API necessárias (n8n, Mistral, etc.).

Inicie os serviços:

Bash

docker compose up -d
A API estará acessível em http://localhost:8080 (ou a porta configurada no seu docker-compose.yml).

## 💡 Melhorias Futuras e Oportunidades
O projeto tem um potencial significativo de expansão. As principais melhorias identificadas incluem:

Implementação de Multi-Tenancy (Multi-Empresa)
Criar um sistema de contas de empresa e login.

Modificar a base de dados para incluir um company_id em todas as transações, isolando os dados de diferentes clientes.

Melhoria Contínua do Algoritmo
Implementar feedback do utilizador (reconciliações aceites/rejeitadas manualmente) para treinar e ajustar o modelo de similaridade.

Explorar técnicas avançadas de NLP e Machine Learning para aumentar a taxa de acerto.

Outras Otimizações
Monitorização e Telemetria: Adicionar endpoints de Actuator do Spring Boot para monitorização em tempo real (Health Checks, Métricas).

Otimização do Upload: Processamento assíncrono do upload para evitar timeouts em grandes volumes de ficheiros.
