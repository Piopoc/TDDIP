# Topic-Driven Document Intelligence Platform (TDDIP)

## Overview

The **Topic-Driven Document Intelligence Platform (TDDIP)** is a document analysis and retrieval system designed to ingest large collections of textual documents from heterogeneous online sources, identify documents related to a given topic, store them in a scalable data layer, and make them searchable through a full-text search engine.

The project was developed as part of a **university exam** and focuses primarily on **system architecture, data pipelines, and information retrieval**, with additional components for topic modeling and frontend-based data viewer.



## Project Goals

The main objectives of the project are:

- Design a **modular and containerized architecture** for document ingestion and analysis  
- Integrate multiple **heterogeneous data sources** into a unified processing pipeline  
- Apply **unsupervised topic modeling (LDA)** to textual corpora  
- Enable **efficient full-text search** over large document collections  
- Provide a **REST API** and a basic **web-based frontend** for querying and inspection  


## Features

### Document Ingestion
- Automated ingestion from multiple online sources:
  - **[Harvard Dataverse](https://dataverse.harvard.edu/dataverse/harvard?q=New+Articles+csv&types=files&sort=score&order=desc&page=3)** (CSV-based datasets)
  - **[ParlaMint](https://www.clarin.si/repository/xmlui/handle/11356/2004)** (parliamentary textual corpora)
- Enhanced documents stored in MongoDB

### Topic Modeling
- Unsupervised topic modeling using **Latent Dirichlet Allocation (LDA)**
- Implemented via **MALLET**, integrated directly into the Java application
- Topic analysis performed during the document analysis phase

### Search and Indexing
- Full-text indexing using **ElasticSearch**
- Synchronization mechanism between MongoDB and ElasticSearch
- Search endpoint returning structured JSON representations of indexed documents

### Frontend
- React-based frontend for querying the backend
- It's designed as a simple exploratory and inspection tool

### Architecture
- Fully **Dockerized environment** using Docker Compose
- Clear separation between:
  - ingestion and analysis logic
  - storage layer
  - search engine
## Tech Stack

### Backend
- **Java** 17
- **Spring Boot** – REST APIs and orchestration
- **MongoDB** 7.0 – primary document storage
- **ElasticSearch** 9.2.1 – indexing and search
- **MALLET** 2.0.8 – topic modeling (LDA)

### Frontend
- **React** (see [Frontend Implementation](https://github.com/Piopoc/sp-frontend))

### Infrastructure
- **Docker & Docker Compose**
- **Kibana** – ElasticSearch inspection
- **Mongo Express** – MongoDB inspection

## Design Choices

- **MongoDB** was chosen due to the semi-structured nature of textual documents and metadata  
- **ElasticSearch** enables scalable and efficient full-text search  
- **MALLET (LDA)** provides robust unsupervised topic modeling
- **Docker Compose** ensures reproducibility and ease of deployment  
- The system is designed as a set of loosely coupled components, each responsible for a specific stage of the document lifecycle (ingestion, analysis, storage, indexing, and retrieval), rather than as a single monolithic application.


## Pre-requirements
Pull the required Docker images:

```bash
docker pull docker.elastic.co/elasticsearch/elasticsearch:9.2.1
docker pull docker.elastic.co/kibana/kibana:9.2.1
docker pull mongo:7.0
docker pull mongo-express:latest
```

## Run Locally

### Prerequisites
- Docker
- Docker Compose
### Installation & Setup
1. **Clone the repository**
```bash
  git clone https://github.com/Piopoc/TDDIP
  cd TDDIP
```
2. **Build and start the stack**
```bash
  docker-compose up --build -d
```
3. **Monitor application logs** 
```bash
    docker logs -f software-platform-demo
```
### Available Dashboards
- **Mongo Express** (username/password: admin)
    http://localhost:8081
- **Kibana**
    http://localhost:5601

4. **Stop and clean up**
```bash
    docker-compose down -v
```

## API Reference

- All APIs respond to the base address: `http://localhost:8080`

#### Import documents
Triggers the injection pipeline that fetches documents from the configured sources.

```http
  POST /api/import-from-pipeline
```

#### Analyze documents
Performs topic analysis using LDA on ingested documents.
```http
  POST /api/analyze
```
#### Sync MongoDB with ElasticSearch
Synchronizes stored documents with the search index.
```http
  POST /api/sync-elastic
```
#### Search documents
Performs a full-text search over indexed documents.
```http
  GET /api/search?query=${value}
```

- example output:

  ```json
  [{
      "domain": "abcnews.go.com",
      "id": "4",
      "title": "Appeals Court to Decide on Challenge to Trump's Immigration Executive Order",
      "topics": [
          {
              "topicId": 0,
              "weight": 0.8882812063257135
          },
          {
              "topicId": 6,
              "weight": 0.10794253766167639
          }
      ]
  }]
  ```

## Documentation

Detailed documentation including:
- architectural and design decisions

- Javadoc

  

  is available in the `./docs` directory as a PDF file.

## Authors

- [Filippo Corradi](https://github.com/Piopoc)
- [Alessandro Sartor](https://github.com/ale03-hub)
- [Lorenzo Tamburi](https://github.com/LorenzoTamburi)

