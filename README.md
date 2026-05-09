# Task Management System

A full-stack Task Management application built using **Angular**, **Spring Boot**, and **PostgreSQL**.
The system supports role-based task assignment and management for:

* Supervisor/Manager
* Team Lead (TL)
* Developer

---

## 🚀 Live Demo

### Frontend

[CoreQueue Frontend](https://corequeue.netlify.app)

### Backend API

[Task Management Backend API](https://task-management-en0u.onrender.com)

---

# ✨ Features

## 👤 User Management

* Create users from UI
* Different user roles:

  * Supervisor/Manager
  * TL
  * Developer

---

## 📋 Task Management

### Supervisor

* Create tasks
* Assign tasks to TLs

### Team Lead (TL)

* View assigned tasks
* Create subtasks
* Assign subtasks to developers

### Developer

* View assigned subtasks

---

## 🔗 Frontend Features

* Angular standalone components
* Dynamic dropdowns
* API integration using Angular HttpClient
* Form-based task creation
* Role-based dashboards

---

## ⚙️ Backend Features

* REST APIs using Spring Boot
* Layered architecture:

  * Controller
  * Service
  * Repository
* PostgreSQL database integration
* Centralized logging
* CORS configuration
* Deployed on Render

---

# 🛠️ Tech Stack

## Frontend

* Angular 21
* TypeScript
* HTML
* CSS

## Backend

* Java 17
* Spring Boot
* Spring Data JPA
* Maven

## Database

* PostgreSQL (Supabase)

## Deployment

* Netlify (Frontend)
* Render (Backend)

---

# 📂 Project Structure

```text
Task_Management
│
├── Front-end
│   └── task-manager-ui
│
├── Taskmanager
│   └── Taskmanager
│
└── database
```

---

# 🔌 API Endpoints

## User APIs

### Create User

```http
POST /users
```

### Get Users By Role

```http
GET /users/role/{role}
```

---

## Task APIs

### Create Task

```http
POST /tasks
```

### Get Tasks For TL

```http
GET /tasks/tl/{tlId}
```

---

## Subtask APIs

### Create Subtask

```http
POST /subtasks
```

---

# 💻 Run Locally

## Backend

```bash
cd Taskmanager/Taskmanager
mvn spring-boot:run
```

---

## Frontend

```bash
cd Front-end/task-manager-ui
npm install
ng serve
```

Frontend runs on:

```text
http://localhost:4200
```

---

# 🗄️ Database Configuration

This project uses PostgreSQL with Supabase.

Update your `application.properties`:

```properties
spring.datasource.url=YOUR_DB_URL
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD
```

---

# 📸 Screenshots

## Create User

* Supervisor/TL/Developer creation UI

## Create Task

* Task assignment dashboard

## TL Dashboard

* Subtask creation and management

(Will add screenshots later)

---

# 📈 Future Improvements

* JWT Authentication
* Role-based login system
* Better UI/UX
* Task status tracking
* Email notifications
* Dashboard analytics
* File attachments

---

# 👨‍💻 Author

## Harsh Pal

GitHub:
[Harsh17380 GitHub Profile](https://github.com/Harsh17380)
