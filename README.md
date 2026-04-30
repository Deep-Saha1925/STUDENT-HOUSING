<div align="center">

<img src="assets/student_housing_logo.svg" width="500" alt="Student Housing Logo"/>

<h1>Student Housing Management System</h1>
<h3>A web-based platform for students, owners, and administrators</h3>

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=flat-square&logo=mysql)](https://www.mysql.com)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-darkgreen?style=flat-square&logo=thymeleaf)](https://www.thymeleaf.org)
[![Cloudinary](https://img.shields.io/badge/Cloudinary-Image%20Storage-3448C5?style=flat-square&logo=cloudinary)](https://cloudinary.com)
[![License](https://img.shields.io/badge/License-MIT-purple?style=flat-square)](LICENSE)

*A centralized, secure, and scalable platform for students, property owners, and administrators*

[Features](#-features) · [Tech Stack](#️-tech-stack) · [Screenshots](#️-screenshots) · [Setup](#️-running-the-project) · [API Docs](#-pages--routes) · [Author](#-conclusion)

</div>

---

## 📌 Overview

**Student Housing Management System** is a web-based platform built using **Java Spring Boot, Spring Security, Thymeleaf, and MySQL**. It enables **students** to search for rental properties, **owners** to list and manage their properties, and **admins** to manage users and maintain platform integrity.

> No extra app install needed. Works in any browser. Designed for students and property owners alike.

---

## 🚀 Features

<table>
<tr>
<td width="50%">

### 👨‍🎓 Student Features
- 🔐 Secure login & registration
- 👤 Personal profile (ID, email, phone)
- 🔍 Search properties by city and max rent
- 🖼️ View full property details with image carousel
- 📞 Owner contact info on property page
- 🏠 User-friendly dashboard

</td>
<td width="50%">

### 🏠 Owner Features
- 📝 Register & manage rental properties
- ☁️ Upload multiple images (Cloudinary)
- ✏️ Edit title, city, area, rent, description
- 🗑️ Delete individual images from listings
- 🔄 Toggle property availability from dashboard
- 📊 Dashboard with full properties table

</td>
</tr>
<tr>
<td width="50%">

### 🔑 Admin Features
- 📊 Dashboard cards for Users, Properties, Register User
- 👥 View all registered users (paginated table)
- ✏️ Edit or delete any user
- 🚫 Admins cannot be deleted
- 🏘️ View all properties (paginated table)
- 🚷 Access denied page for unauthorized routes

</td>
<td width="50%">

### ⚙️ System Highlights
- 🔐 Role-based access (STUDENT, OWNER, ADMIN)
- 🔄 Custom login redirect per role
- ☁️ Cloudinary auto-delete on property removal
- 🌙 Light / dark mode with `localStorage` persistence
- 📱 Bootstrap 5 responsive layout
- 🚫 Custom 403 & error pages

</td>
</tr>
</table>

---

## 🛠️ Tech Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| Java | 17 | Core language |
| Spring Boot + MVC | 3.x | REST API & MVC framework |
| Spring Security | 6.x | Role-based access control |
| Spring Data JPA | — | ORM & repository layer |
| MySQL | 8.0 | Persistent data storage |
| Cloudinary | — | Image upload & auto-delete |

### Frontend
| Technology | Purpose |
|---|---|
| Thymeleaf | Server-side templating |
| Bootstrap 5 | Responsive layout & components |
| Animate.css | Entry animations |
| DM Serif Display + DM Sans | Typography (Google Fonts) |

---

## 🎨 UI Design System

All pages share a consistent design system using CSS variables, supporting seamless light/dark theme switching. Theme preference is saved in `localStorage` under the key `sh-theme`.

### Color Palette
| Variable | Light Mode | Dark Mode |
|---|---|---|
| `--accent` | `#FF5E62` | `#FF5E62` |
| `--accent-2` | `#FF9966` | `#FF9966` |
| `--bg` | `#FAF7F4` | `#111112` |
| `--card-bg` | `#FFFFFF` | `#1E1E22` |
| `--text` | `#1A1A1A` | `#F0EDE8` |
| `--text-muted` | `#777777` | `#888888` |
| `--input-bg` | `#F5F2EF` | `#26252A` |

---

## ⚙️ Implementation Details

### 1️⃣ Authentication & Authorization
- Spring Security used for **login, logout, and role-based access control**
- Roles: `ROLE_STUDENT`, `ROLE_OWNER`, `ROLE_ADMIN`
- Custom `LoginSuccessHandler` redirects each role to their respective dashboard on login
- Unauthorized access to protected routes redirects to `/access-denied` instead of the default Whitelabel 403 error

### 2️⃣ File Storage
- Images uploaded to **Cloudinary**
- When properties are deleted, images are also automatically removed from Cloudinary

### 3️⃣ Error Handling
- Custom `/access-denied` page for 403 Forbidden errors
- Custom `error.html` for general exceptions (e.g., user not found)

### 4️⃣ Responsive UI
- Bootstrap 5 used for responsiveness
- Navbar buttons collapse into a dropdown on smaller screens
- Dark/light theme toggle on every page, synced via `localStorage`

---

## 📄 Pages & Routes

| Page | Template | Route | Access |
|---|---|---|---|
| Home | `index.html` | `/` | 🌐 Everyone |
| Login | `login.html` | `/login` | 🌐 Everyone |
| Register | `register-only-user.html` | `/register-user` | 🌐 Everyone |
| Access Denied | `access-denied.html` | `/access-denied` | 🌐 Everyone |
| Student Dashboard | `student-dashboard.html` | `/student/dashboard` | 🎓 STUDENT |
| Student Search | `student-search.html` | `/student-search` | 🎓 STUDENT |
| Owner Dashboard | `owner-dashboard.html` | `/properties/owner/{id}` | 🏠 OWNER |
| Add Property | `add-property.html` | `/properties/owner/{id}/add` | 🏠 OWNER |
| Edit Property | `edit-property.html` | `/properties/owner/{id}/edit/{pid}` | 🏠 OWNER |
| Property Details | `property-details.html` | `/properties/{id}` | 🔒 Auth |
| User Profile | `user-profile.html` | `/profile/{id}` | 🔒 Auth |
| Admin Profile | `admin-profile.html` | `/profile/admin` | 🔒 Auth |
| Admin Dashboard | `admin-dashboard.html` | `/admin/dashboard` | 🔐 ADMIN |
| Manage Users | `manage-users.html` | `/admin/users` | 🔐 ADMIN |
| Manage Properties | `manage-properties.html` | `/admin/properties` | 🔐 ADMIN |
| Edit User | `edit-user.html` | `/admin/users/edit/{id}` | 🔐 ADMIN |

---

## 👥 Roles & Access

| Role | Permissions |
|---|---|
| **Student** | Search properties, view property details, manage own profile |
| **Owner** | Add/edit/delete own properties, manage availability, upload/remove images |
| **Admin** | Manage all users and properties, register new users (cannot be deleted) |

---

## 📂 Project Structure
```
src/main/java/com/studenthousing
│── controller/     # Controllers for Student, Owner, Admin
│── model/          # Entity classes (User, Property)
│── repository/     # JPA Repositories
│── service/        # Business logic & Cloudinary integration
│── config/         # SecurityConfig, CustomLoginSuccessHandler
│
src/main/resources/templates
│── index.html                  # Home / landing page
│── login.html                  # Login page
│── register-only-user.html     # Registration page
│── student-dashboard.html      # Student dashboard
│── student-search.html         # Property search
│── owner-dashboard.html        # Owner dashboard
│── add-property.html           # Add property form
│── edit-property.html          # Edit property form
│── property-details.html       # Property detail view
│── user-profile.html           # User profile (student/owner)
│── admin-profile.html          # Admin contact profile
│── admin-dashboard.html        # Admin control panel
│── manage-users.html           # Admin user management table
│── manage-properties.html      # Admin properties table
│── edit-user.html              # Admin edit user form
│── access-denied.html          # 403 friendly error page
│── error.html                  # General error page
│── fragments/
│   └── property-list.html      # Reusable property card fragment

```
---

## 🖼️ Screenshots

### Home / Index Page
![Index Page](assets/index.png)

### Student Dashboard
![Student Dashboard](assets/student_dashboard.png)

### Owner Dashboard
![Owner Dashboard](assets/owner_dashboard.png)

### Admin Dashboard
![Admin Dashboard](assets/admin_dashboard.png)

### Property Details
![Property Details](assets/property_details.png)

### Search Properties
![Search Properties](assets/search_properties.png)

---

## ▶️ Running the Project

### Prerequisites
- Java 17+
- MySQL 8+
- Maven 3.8+

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/student-housing-system.git
cd student-housing-system
```

```bash
# 2. Create MySQL database
mysql -u root -p
```

```sql
CREATE DATABASE housing_db;
EXIT;
```

```bash
# 3. Configure application.properties
```

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/housing_db
spring.datasource.username=root
spring.datasource.password=yourpassword
spring.jpa.hibernate.ddl-auto=update

# Cloudinary
cloudinary.cloud_name=your_cloud_name
cloudinary.api_key=your_api_key
cloudinary.api_secret=your_api_secret
```

```bash
# 4. Build and run
mvn spring-boot:run
```

```bash
# 5. Open in browser
open http://localhost:8080
```

---

## 📌 Future Enhancements

- [ ] Booking system for students
- [ ] Payment gateway integration
- [ ] Notification system for owners/students
- [ ] Advanced analytics dashboard for admins
- [ ] Mobile-responsive PWA support

---

## 🏆 Conclusion

The **Student Housing Management System** provides a **centralized, secure, and scalable platform** for students, property owners, and administrators. Built on Spring Boot with a modern Thymeleaf frontend, it features full role-based access control, cloud image storage, a consistent design system with dark/light theming, and is structured for future extensibility.

---

<div align="center">

*Built with ❤️ · Java · Spring Boot · Thymeleaf · MySQL*

</div>