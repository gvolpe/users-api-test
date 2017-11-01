users-api
=========

### Base User Actions

##### Sign Up:
* POST /v1/signup

```json
{
  "username": "gvolpe",
  "email": "gvolpe@github.com",
  "password": "123456" // optional
}
```

##### Retrieve all users:
* GET /v1/users

##### Retrieve user by id:
* GET /v1/users/**{id}**

##### Update user's email
* PATCH /v1/users/**{id}**

```json
{
  "email": "gvolpe@gmail.com"
}
```

##### Update user's password
* PATCH /v1/users/**{id}**

```json
{
  "password: "gvolpe@gmail.com"
}
```

##### Forgot Password
* POST /v1/users/**{id}**/forgotpassword

##### Signin:
* POST /v1/signin

```json
{
  "username": "gvolpe",
  "password": "123456"
}
```

### Admin User Actions

Requires Authentication (/v1/signin)

##### Delete user by id:
* DELETE /v1/admin/users/**{id}**

##### Block user by id:
* POST /v1/admin/users/**{id}**/block

##### Unblock user by id:
* POST /v1/admin/users/**{id}**/unblock

### Notes

For the sake of simplicity in this example the user with username = "gvolpe" and password = "" will be the only one authenticated as an administrator. Any other user will not be authorized to perform requests that require authentication.

Start the server by executing `sbt run`. The server will be listening at [http://localhost:8080](http://localhost:8080).
