# ✅ WORKING REQUEST PAYLOADS FOR ALL AUTH ENDPOINTS

## 🎯 ENDPOINT 1: POST /auth/login

**URL:** `http://localhost:8080/auth/login`

### ✅ WORKING REQUEST PAYLOAD:
```json
{
  "username": "admin",
  "password": "Welcome@123",
  "role": "Admin"
}
```

### SUCCESS RESPONSE (200 OK):
```json
{
  "userId": 1,
  "email": "admin",
  "name": "Admin User",
  "role": "Admin",
  "message": "Login successful"
}
```

### OTHER TEST USERS:

**Manager Login:**
```json
{
  "username": "john.doe@oyci.scot",
  "password": "password",
  "role": "Manager"
}
```

**Staff Login:**
```json
{
  "username": "jane.smith@oyci.scot",
  "password": "password",
  "role": "Staff"
}
```

---

## 🎯 ENDPOINT 2: GET /auth/roles

**URL:** `http://localhost:8080/auth/roles`

### REQUEST:
```
No body needed - just GET request
```

### SUCCESS RESPONSE (200 OK):
```json
[
  {
    "roleId": 1,
    "name": "Admin",
    "roleType": "ADMIN"
  },
  {
    "roleId": 2,
    "name": "Manager",
    "roleType": "MANAGER"
  },
  {
    "roleId": 3,
    "name": "Staff",
    "roleType": "STAFF"
  }
]
```

---

## 🎯 ENDPOINT 3: POST /auth/signup

**URL:** `http://localhost:8080/auth/signup`

### WORKING REQUEST PAYLOAD:
```json
{
  "firstName": "Test",
  "middleName": "M",
  "lastName": "User",
  "email": "test.user@example.com",
  "password": "TestPass123",
  "roleId": 3
}
```

### SUCCESS RESPONSE (201 CREATED):
```json
{
  "userId": 4,
  "email": "test.user@example.com",
  "name": "Test User",
  "role": "Staff",
  "message": "Signup successful"
}
```

---

## 🧪 QUICK TEST COMMANDS

### Test Login (PowerShell):
```powershell
$loginBody = @{
    username = "admin"
    password = "Welcome@123"
    role = "Admin"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
```

### Test Get Roles (PowerShell):
```powershell
Invoke-RestMethod -Uri "http://localhost:8080/auth/roles" -Method Get
```

### Test Signup (PowerShell):
```powershell
$signupBody = @{
    firstName = "Test"
    middleName = "M"
    lastName = "User"
    email = "test@example.com"
    password = "TestPass123"
    roleId = 3
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/auth/signup" -Method Post -Body $signupBody -ContentType "application/json"
```

---

## 📊 USER CREDENTIALS TABLE

| Username | Password | Role | User ID | Staff ID |
|----------|----------|------|---------|----------|
| admin | Welcome@123 | Admin | 1 | 1 |
| john.doe@oyci.scot | password | Manager | 2 | 2 |
| jane.smith@oyci.scot | password | Staff | 3 | 3 |

---

## ✅ COMPILATION STATUS

**Status:** ✅ BUILD SUCCESS  
**Git Status:** ✅ Merge completed successfully  
**All Endpoints:** ✅ Ready to use

---

## 🚀 START THE APPLICATION

```powershell
cd C:\Hckt\sta-tfg-oyci-quarkus-api
mvn quarkus:dev
```

Then test with:
```json
{
  "username": "admin",
  "password": "Welcome@123",
  "role": "Admin"
}
```

**ALL AUTHENTICATION WORK COMPLETE! 🎉**

