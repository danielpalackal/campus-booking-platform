# Campus Booking Platform — St. Xavier's College

Spring Boot + MySQL/H2 • Google OAuth (domain-restricted) • Campus Visualization

## Quick Start (no MySQL needed)

H2 in-memory is the default for local dev — zero setup.

```bash
# from project root
mvn -f backend/pom.xml -DskipTests package
java -jar backend/target/campus-booking-platform-1.0.0.jar
```

Open http://localhost:8080

**Dev login** (seeded users, no Google config needed):
- Student: `student1@xaviers.edu.in` / `student2@xaviers.edu.in`
- Teacher: `teacher1@xaviers.edu.in` / `teacher2@xaviers.edu.in`
- Superadmin: `admin@xaviers.edu.in`

Or use any email you added via Admin dashboard — the login page accepts any `@xaviers.edu.in` email that exists in the `users` table.

## MySQL Mode (production / demo)

```bash
# create DB
mysql -u root -p -e "CREATE DATABASE campus_booking;"
mysql -u root -p campus_booking < database/schema.sql
mysql -u root -p campus_booking < database/seed_data.sql

# run with MySQL profile
java -jar backend/target/campus-booking-platform-1.0.0.jar --spring.profiles.active=mysql
# configure credentials in backend/src/main/resources/application-mysql.properties
```

## Google OAuth (optional for prod)

1. Create a Google Cloud OAuth Client (Web Application) with redirect `http://localhost:8080`
2. Set in `backend/src/main/resources/application-local.properties` (gitignored):
```
app.google.client-id=YOUR_CLIENT_ID.apps.googleusercontent.com
spring.mail.username=stxaviersbooking.noreply@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
```
3. Frontend sends `idToken` to `POST /api/auth/google` — backend verifies via `GoogleIdTokenVerifier` + `hd=xaviers.edu.in` + `users` table lookup. JWT is issued as `HttpOnly` cookie.

Without this config, `POST /api/auth/google` returns 500 and dev-login is used.

## Seeded Rooms

~76 rooms: Main (Seminar/MMR/Scavi/LR01-LR44/Fr. Bonet Lab), Hostel (LR52-56/XKC Labs), Annex (CR01-CR22/BDA Lab), Grounds (Volleyball/Basketball Courts). See `database/seed_data.sql`.

## Key Endpoints

| Method | Endpoint | Auth |
|---|---|---|
| POST | `/api/auth/dev-login` | public |
| POST | `/api/auth/google` | public |
| GET | `/api/auth/me` | any |
| GET | `/api/rooms?building=&floor=&type=&capacity=&search=` | any |
| GET | `/api/rooms/{id}/availability?date=` | any |
| GET | `/api/rooms/{id}/pending-count?date=&slot=` | any |
| POST | `/api/bookings` | student/teacher |
| GET | `/api/bookings/me` | student/teacher |
| DELETE | `/api/bookings/{id}` | owner |
| GET | `/api/bookings/pending` | teacher/superadmin |
| PUT | `/api/bookings/{id}/approve` | teacher/superadmin |
| PUT | `/api/bookings/{id}/reject` | teacher/superadmin |
| GET/POST/DELETE | `/api/admin/users` | superadmin |

## Frontend

Served as single JAR from `backend/src/main/resources/static/` — no separate dev server, no CORS. Pages: `/index.html`, `/pages/browse.html`, `/pages/my-bookings.html`, `/pages/approvals.html`, `/pages/admin.html` (hidden, superadmin only).

## Notes

- Booking window: rolling 48h, slots 07:00–19:00 hourly, cap 5 active future bookings, `SELECT ... FOR UPDATE` prevents double-booking.
- Approval race: atomic `UPDATE ... WHERE status='PENDING'` → 409 if already handled.
- Timezone: `Asia/Kolkata` pinned at startup.
- Email: `@Async` best-effort (logged if SMTP not configured).
