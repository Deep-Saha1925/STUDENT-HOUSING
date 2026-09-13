<div align="center">

<img src="assets/logo.png" width="500" alt="Student Housing Logo"/>

<h1>Student Housing Management System</h1>
<h3>A web-based platform for students, owners, and administrators</h3>

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Neon-blue?style=flat-square&logo=postgresql)](https://neon.tech)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-darkgreen?style=flat-square&logo=thymeleaf)](https://www.thymeleaf.org)
[![Cloudinary](https://img.shields.io/badge/Cloudinary-Image%20Storage-3448C5?style=flat-square&logo=cloudinary)](https://cloudinary.com)
[![License](https://img.shields.io/badge/License-MIT-purple?style=flat-square)](LICENSE)

*A centralized, secure, and scalable platform for students, property owners, and administrators*

[Features](#-features) · [Tech Stack](#️-tech-stack) · [Screenshots](#️-screenshots) · [Setup](#️-running-the-project) · [API Docs](#-pages--routes) · [Author](#-conclusion)

</div>

---

## 📌 Overview

**Student Housing Management System** is a web-based platform built using **Java Spring Boot, Spring Security, Thymeleaf, and PostgreSQL (Neon)**. It enables **students** to search for and book rental properties, **owners** to list, manage, and rent out their properties, and **admins** to manage users and maintain platform integrity.

> No extra app install needed. Works in any browser. Designed for students and property owners alike. Data is stored in the cloud via Neon PostgreSQL — accessible from any machine.

---

## 🚀 Features

<table>
<tr>
<td width="50%">

### 👨‍🎓 Student Features
- 🔐 Secure login & registration
- 👤 Personal profile (ID, email, phone)
- 🔍 Search properties by city, area, max rent, and **rental type (Monthly / Daily / Any)**
- 📍 **Location-based search** — find properties near you using GPS
- 🗺️ Adjustable search radius (2 / 5 / 10 / 20 km)
- 🖼️ View full property details with image carousel
- 🏷️ See at a glance whether a listing is Monthly, Daily, or both
- 📅 **Book a property** with a live availability calendar — pick a date range for daily stays, or a move-in date + duration for monthly stays
- 📑 **My Bookings** — track every booking request (Pending / Confirmed / Cancelled), split by Monthly and Daily
- ↩️ Withdraw a booking request while it's still pending
- 📞 Owner contact info on property page
- 🏠 User-friendly dashboard

</td>
<td width="50%">

### 🏠 Owner Features
- 📝 Register & manage rental properties
- 🏷️ Offer each property for **Monthly rent, Daily rent, or both** independently, with separate pricing for each
- ☁️ Upload multiple images (Cloudinary)
- ✏️ Edit title, city, area, rent(s), rental type(s), description
- 🗑️ Delete individual images from listings
- 🔄 Toggle property availability from dashboard
- 📊 Dashboard with full properties table
- ✅ **Approve or reject booking requests** from students, per property
- 🔔 **Pending-request notification badge** on the My Properties dashboard — see at a glance which properties have requests waiting
- 🚫 Cancel a confirmed booking if needed
- 🌐 **Auto-geocoding** — GPS coordinates saved automatically from city + area on property save

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
- 📅 **Booking system** with an owner-approval workflow (Pending → Confirmed/Cancelled) and double-booking prevention
- ☁️ Cloudinary auto-delete on property removal
- 🌙 Light / dark mode with `localStorage` persistence
- 📱 Bootstrap 5 responsive layout
- 🚫 Custom 403, "Unauthorized," and general error pages
- 🌐 Cloud-hosted database (Neon PostgreSQL)
- 📍 **Smart location search** with GPS + text fallback, filterable by rental type

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
| PostgreSQL (Neon) | — | Cloud-hosted persistent data storage |
| Cloudinary | — | Image upload & auto-delete |
| OpenStreetMap Nominatim | — | Free geocoding & reverse geocoding (no API key needed) |

### Frontend

| Technology | Purpose |
|---|---|
| Thymeleaf | Server-side templating |
| Bootstrap 5 | Responsive layout & components |
| Animate.css | Entry animations |
| DM Serif Display + DM Sans | Typography (Google Fonts) |
| Browser Geolocation API | Student location detection |
| Vanilla JS calendar widget | Custom-built date-range (daily) and move-in + duration (monthly) booking picker — no external calendar library |

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
- A separate `UnauthorizedActionException` + `GlobalExceptionHandler` catches ownership-mismatch actions (e.g. an owner trying to view another owner's bookings) and renders a themed **"Unauthorized"** page instead of a raw stack trace

### 2️⃣ File Storage

- Images uploaded to **Cloudinary**
- When properties are deleted, images are also automatically removed from Cloudinary

### 3️⃣ Database — Neon PostgreSQL (Cloud)

- Database is hosted on **Neon** (free tier) — a serverless PostgreSQL platform
- Data is accessible from **any machine** without local setup
- Hibernate auto-creates and manages tables via `ddl-auto=update`
- New columns/tables (rental types, bookings) use `columnDefinition` defaults so schema migrations backfill existing rows safely instead of failing on `NOT NULL` constraints

### 4️⃣ Rental Types & Booking System

Each property can independently offer **Monthly rent, Daily rent, or both**, each with its own price (`monthlyRent`, `dailyRent`, `availableMonthly`, `availableDaily`).

**Booking workflow (owner-approval model):**

- A student picks dates on the property page and submits a **booking request**, which is created with status `PENDING`
- The request already blocks those dates for other students — a property is occupied by at most one active booking (monthly or daily) at a time, regardless of type, so two people can never be approved into overlapping dates
- The **owner** reviews requests on their property's Bookings page and **Approves** (→ `CONFIRMED`) or **Rejects** (→ `CANCELLED`) each one
- A red, pulsing **notification badge** on the owner's My Properties dashboard shows the pending-request count per property
- Students track all their requests on **My Bookings**, split into Monthly and Daily sections, and can **withdraw** a request while it's still pending (not once it's confirmed)

**Calendar UI — different behavior per rental type:**

- **Daily** bookings: click a start day and an end day on a custom calendar widget; booked/past days are greyed out and unclickable
- **Monthly** bookings: click a single move-in date, then choose a duration (1/2/3/6/12 months) from a dropdown — the move-out date is computed automatically (handling month-length edge cases) and checked against existing bookings
- Dates are formatted in local time throughout (never converted through UTC), avoiding the off-by-one-day bugs that timezone-naive date handling commonly introduces

### 5️⃣ Location-Based Property Search

The platform features a **smart two-mode search system**:

**Mode 1 — Manual Search**

- Student enters city/area name, optional max rent, and optional **rental type filter** (Monthly only / Daily only / Any)
- Searches both `city` and `area` fields (case-insensitive, partial match)
- e.g. searching "Salbari" matches a property with `area="Salbari"` even if `city="Sukna"`

**Mode 2 — Find Near Me (GPS)**

- Student clicks "Find Near Me" — browser requests location permission
- Student can choose search radius: 2 / 5 / 10 / 20 km
- **Step 1 (GPS):** Haversine formula query finds properties with stored coordinates within the chosen radius, sorted by distance
- **Step 2 (Fallback):** If no GPS results, reverse geocoding via Nominatim converts the student's coordinates to a city/area name, then falls back to text-based matching
- This ensures results are always returned even when properties don't have GPS coordinates yet

**Auto-Geocoding on Property Save**

- When an owner saves a property, `GeoCodingService` automatically calls the Nominatim API to convert `city + area` into `latitude` + `longitude`
- Coordinates are stored silently — owners never need to enter GPS data manually
- As more properties are saved, GPS-based search coverage improves automatically

### 6️⃣ Error Handling

- Custom `/access-denied` page for 403 Forbidden errors
- Custom themed **"Unauthorized"** page for ownership-mismatch actions (e.g. editing another owner's property)
- Custom `error-page.html` for general exceptions (e.g., user not found)

### 7️⃣ Responsive UI

- Bootstrap 5 used for responsiveness
- Navbar buttons collapse into a dropdown on smaller screens
- Dark/light theme toggle on every page, synced via `localStorage`
- The booking calendar adapts its layout for mobile; dashboard cards reflow from a grid to a row layout on small screens

---

## 📄 Pages & Routes

| Page | Template | Route | Access |
|---|---|---|---|
| Home | `index.html` | `/` | 🌐 Everyone |
| Login | `login.html` | `/login` | 🌐 Everyone |
| Register | `register-only-user.html` | `/register-user` | 🌐 Everyone |
| Access Denied | `access-denied.html` | `/access-denied` | 🌐 Everyone |
| Unauthorized | `unauthorized.html` | shown on ownership-mismatch actions | 🌐 Everyone |
| Student Dashboard | `student-dashboard.html` | `/student/dashboard` | 🎓 STUDENT |
| Search Properties | `search-properties.html` | `/student-search` | 🎓 STUDENT |
| Search — Manual | `fragments/property-list` | `/search?city=&rent=&rentalType=` | 🎓 STUDENT |
| Search — Near Me | `fragments/property-list` | `/properties/nearby?lat=&lng=&radius=` | 🎓 STUDENT |
| My Bookings | `my-bookings.html` | `/properties/my-bookings` | 🎓 STUDENT |
| Owner Dashboard | `owner-properties.html` | `/properties/owner/{id}` | 🏠 OWNER |
| Add Property | `add-property.html` | `/properties/owner/{id}/add` | 🏠 OWNER |
| Edit Property | `edit-property.html` | `/properties/owner/{id}/edit/{pid}` | 🏠 OWNER |
| Property Bookings | `property-bookings.html` | `/properties/owner/{id}/bookings/{pid}` | 🏠 OWNER |
| Property Details & Booking | `property-details.html` | `/properties/{id}` | 🔒 Auth |
| Book a Property | — | `POST /properties/{id}/book` | 🎓 STUDENT |
| Approve Booking | — | `POST /properties/bookings/{id}/approve` | 🏠 OWNER |
| Reject Booking | — | `POST /properties/bookings/{id}/reject` | 🏠 OWNER |
| Cancel/Withdraw Booking | — | `POST /properties/bookings/{id}/cancel` | 🎓 STUDENT / 🏠 OWNER |
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
| **Student** | Search properties (manual + GPS, filterable by rental type), view property details, book properties (monthly or daily), track/withdraw booking requests, manage own profile |
| **Owner** | Add/edit/delete own properties, set Monthly/Daily availability and pricing independently, manage listing availability, upload/remove images (coordinates auto-saved), approve/reject/cancel booking requests |
| **Admin** | Manage all users and properties, register new users (cannot be deleted) |

---

## 📂 Project Structure