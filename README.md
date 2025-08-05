# 🦁 ITGO-server

> [곽생살조] - 서울과학기술대학교 멋쟁이사자처럼 연합 해커톤 프로젝트 


## 📌 Swagger API Docs

> http://43.200.120.6/swagger-ui/index.html#/


## 🚀 기술 스택

| 구분             | 내용                              |
|----------------|---------------------------------|
| **Language**   | Java 17                         |
| **Framework**  | Spring Boot 3.3.5               |
| **Build Tool** | Gradle                          |
| **Database**   | MySQL (RDS), Redis              |
| **Deployment** | AWS EC2, GitHub Actions, Docker |
| **Docs**       | SpringDoc Swagger UI            |
| **Auth**       | Spring Security + JWT           |
| **CI/CD**      | GitHub Actions + Docker Compose |


## 📂 프로젝트 구조

```bash
ITGO-server/
├── src/
│   ├── main/
│   │   ├── java/likelion/itgo/
│   │   └── resources/
│   └── test/
├── build.gradle
├── Dockerfile
├── docker-compose.yml
└── README.md
```


## 💻 로컬 실행 방법

### 1. 환경 변수 설정

`.env` 파일 생성:

```env
# Spring Profile
SPRING_PROFILES_ACTIVE=dev

# JPA 
SPRING_JPA_HIBERNATE_DDL_AUTO=update

# MySQL
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_DATABASE=your_database
MYSQL_USER=your_user
MYSQL_PASSWORD=your_password

# JWT
JWT_SECRET_KEY=your_secret
JWT_ACCESS_VALIDITY_IN_SECONDS=3600
JWT_REFRESH_VALIDITY_IN_SECONDS=1209600
```

### 2. 빌드 및 실행

```bash
> ./gradlew build -x test
> docker compose up --build -d
```
