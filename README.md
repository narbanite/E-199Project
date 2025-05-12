# E-199 Project: Incident Management System for Firefighting Services

This repository contains the source code for the E-199 project, a web-based incident management system designed for firefighting services as part of the CS-359 course. The system facilitates the handling of incidents, user management, volunteer coordination, and communication, providing a robust platform for emergency response operations.

## Overview

The project is divided into two main components:
- **Frontend**: Built with HTML, CSS, and JavaScript, featuring a user-friendly interface for administrators, users, and volunteer firefighters.
- **Backend**: Implemented using Java Servlets, REST APIs, and AJAX, with a database schema including tables for incidents, messages, participants, users, and volunteers.

The application runs in a noCORS browser environment and leverages various APIs for enhanced functionality.

## Features

### Backend Functionality
- **User Management**: Registration, login, logout, and profile updates via Servlets (e.g., `LoadUser.java`, `RegistrationUser.java`).
- **Volunteer Management**: Volunteer registration, profile updates, and incident participation acceptance (e.g., `LoadVolunteer.java`, `AcceptIncidentParticipation.java`).
- **Incident Management**: Add, update, and retrieve incident details (e.g., `AddIncident.java`, `UpdateIncident.java`, `LoadIncidents.java`).
- **Communication**: Send and retrieve public/private messages (e.g., `SendPublicMessage.java`, `GetMessages.java`).
- **Statistics & Administration**: Generate statistics and manage volunteer positions (e.g., `GetStatistics.java`, `CreateSeats.java`).
- **Location Services**: Calculate nearby incidents and track user/volunteer locations (e.g., `CalculateNearbyIncidents.java`, `GetUserLocation.java`).
- **Database Management**: Initialize and maintain database tables (e.g., `InitDB.java`, `DeleteDB.java`).

### Frontend Components
- **HTML Files**: 
  - `index.html`: Main entry page with links to login, register, and report incidents.
  - `admin.html`: Admin interface for managing users, volunteers, and incidents.
  - `user.html`: User dashboard for incident reporting and tracking.
  - `volunteer.html`: Volunteer interface for participation and history.
- **CSS Files**: Custom styling for each page (e.g., `index.css`, `admin.css`).
- **JavaScript Files**: 
  - `ajax.js`: Handles real-time updates and form submissions.
  - `script.js`: Manages UI interactions, validations, maps, and data display.

## Technologies Used
- **Frontend**: HTML5, CSS3, JavaScript, OpenLayers (for maps), Google Charts (for visualizations).
- **Backend**: Java Servlets, REST APIs, AJAX.
- **APIs**: RAPID API (TrueWay Matrix, Geocoding), jQuery (light usage).
- **Database**: MySQL (via noCORS environment).

## Setup and Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/narbanite/E-199Project.git
   cd E-199Project
