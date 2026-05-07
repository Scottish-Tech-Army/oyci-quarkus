# 🔐 USER CREDENTIALS - COMPLETE LIST

## ✅ ALL TEST USERS WITH UNENCRYPTED PASSWORDS

---

## 👤 USER 1: ADMIN USER (PRIMARY LOGIN)

### Login Credentials:
```json
{
  "username": "admin",
  "password": "Welcome@123",
  "role": "Admin"
}
```

### Details:
- **User ID:** 1
- **Email/Username:** admin
- **Password:** `Welcome@123` (unencrypted)
- **Full Name:** Admin User
- **Role:** Admin (role_id: 1)
- **Staff Type:** MANAGER (staff_type_id: 4)
- **Staff ID:** 1
- **Status:** ✅ Active

### Expected Response:
```json
{
  "userId": 1,
  "email": "admin",
  "name": "Admin User",
  "role": "Admin",
  "message": "Login successful"
}
```

---

## 👤 USER 2: JOHN DOE

### Login Credentials:
```json
{
  "username": "john.doe@oyci.scot",
  "password": "password",
  "role": "Manager"
}
```

### Details:
- **User ID:** 2
- **Email/Username:** john.doe@oyci.scot
- **Password:** `password` (unencrypted)
- **Full Name:** John A Doe
- **Role:** Manager (role_id: 2)
- **Staff Type:** COORDINATOR (staff_type_id: 1)
- **Staff ID:** 2
- **Status:** ✅ Active

### Expected Response:
```json
{
  "userId": 2,
  "email": "john.doe@oyci.scot",
  "name": "John A Doe",
  "role": "Manager",
  "message": "Login successful"
}
```

---

## 👤 USER 3: JANE SMITH

### Login Credentials:
```json
{
  "username": "jane.smith@oyci.scot",
  "password": "password",
  "role": "Staff"
}
```

### Details:
- **User ID:** 3
- **Email/Username:** jane.smith@oyci.scot
- **Password:** `password` (unencrypted)
- **Full Name:** Jane Smith
- **Role:** Staff (role_id: 3)
- **Staff Type:** VOLUNTEER (staff_type_id: 2)
- **Staff ID:** 3
- **Status:** ✅ Active

### Expected Response:
```json
{
  "userId": 3,
  "email": "jane.smith@oyci.scot",
  "name": "Jane Smith",
  "role": "Staff",
  "message": "Login successful"
}
```

---

## 📊 QUICK REFERENCE TABLE

| User ID | Username | Password | User Role | Staff Type | Staff ID | Full Name |
|---------|----------|----------|-----------|------------|----------|-----------|
| 1 | admin | Welcome@123 | Admin | MANAGER | 1 | Admin User |
| 2 | john.doe@oyci.scot | password | Manager | COORDINATOR | 2 | John A Doe |
| 3 | jane.smith@oyci.scot | password | Staff | VOLUNTEER | 3 | Jane Smith |

---

## 🔒 BCrypt PASSWORD HASHES

### Password: `Welcome@123`
- **Hash:** `$2a$10$We3KIr7ZcNnf4fW4mYQHCu3xlCp8UFn0fBGgWYlF8Pu68UDb0/EZa`
- **Used by:** Admin user

### Password: `password`
- **Hash:** `$2a$10$kMcM0udyu74SWGOsI6tU2.aLUZtnfWUvJ8RusLb5aRa1GmwqO/iji`
- **Used by:** John Doe, Jane Smith

---

## 🚀 TEST THE LOGIN ENDPOINT

### Using cURL:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Welcome@123","role":"Admin"}'
```

### Using PowerShell:
```powershell
$body = @{
    username = "admin"
    password = "Welcome@123"
    role = "Admin"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/auth/login" `
    -Method Post `
    -Body $body `
    -ContentType "application/json"
```

### Using JavaScript:
```javascript
fetch('http://localhost:8080/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'admin',
    password: 'Welcome@123',
    role: 'Admin'
  })
})
.then(response => response.json())
.then(data => console.log('Success:', data))
.catch(error => console.error('Error:', error));
```

---

## ✅ WHAT'S IMPLEMENTED

1. ✅ **Login endpoint accepts `username` field** (instead of email)
2. ✅ **Password validation with BCrypt hashing**
3. ✅ **Optional role validation** (role field in request)
4. ✅ **Active user status checking**
5. ✅ **Full name includes middle name** when present
6. ✅ **All 3 users in both users and staff tables**
7. ✅ **One user per role type** (Admin, Manager, Staff)

---

## 📝 IMPORTANT NOTES

- The **role field is OPTIONAL** in the request
- If role is provided, it validates the user has that specific role
- All passwords are BCrypt hashed in the database
- The username field accepts the email address
- Admin user is properly in the staff table with Staff ID = 1

---

## 🎯 YOUR PRIMARY LOGIN

**For frontend testing:**
```json
{
  "username": "admin",
  "password": "Welcome@123",
  "role": "Admin"
}
```

**Status:** ✅ READY TO USE!

