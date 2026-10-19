Spring Boot API Quickstart
==================

This is the completed project corresponding to the guide "[Protect your API endpoints](https://developer.okta.com/docs/guides/protect-your-api/springboot/main/)".


Prerequisites
-------------

- Java 25 or later
- An Okta org with a **custom authorization server** (the `default` one works). The org authorization server cannot issue access tokens that third-party APIs can validate.
- A custom scope named `messages.read` on that authorization server, and an access policy with a rule that grants it. Without a policy, no tokens are issued.

Getting Started
---------------

Copy the file `.env.example` to `.env` and set:

- `ISSUER`: the issuer URI of your authorization server, for example `https://{yourOktaDomain}/oauth2/default`.
- `AUDIENCE`: the audience configured on that authorization server, for example `api://default`.

Load the variables and run the app (it starts on port 8081):

```bash
set -a && source .env && set +a
./mvnw spring-boot:run
```

Get an access token from your authorization server using any OAuth 2.0 client (for example, the authorization code flow with PKCE for a user). Then call the API with it:

```bash
# Any valid token
curl -H "Authorization: Bearer TOKEN" http://localhost:8081/api/hello

# Any valid token: returns the token claims, or 401 without a valid token
curl -H "Authorization: Bearer TOKEN" http://localhost:8081/api/whoami

# Requires the messages.read scope: returns 403 if the scope is missing
curl -H "Authorization: Bearer TOKEN" http://localhost:8081/api/messages
```
