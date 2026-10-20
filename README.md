Spring Boot API Quickstart
==================

This is the completed project corresponding to the guide "[Protect your API endpoints](https://developer.okta.com/docs/guides/protect-your-api/springboot/main/)".


Prerequisites
-------------

- Java 25 or later
- An Okta org with a **custom authorization server** (the `default` one works). The org authorization server cannot issue access tokens that third-party APIs can validate.
- A custom scope named `messages.read` on that authorization server, and an access policy with a rule that grants it. Without a policy, no tokens are issued.

This API has no login page of its own. To use it, run it with a front-end that signs the user in and sends the access token to the API. Use [okta-spring-boot-sample-app](https://github.com/okta-samples/okta-spring-boot-sample-app), which is set up to call this API.

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

Test the API
------------

1. Keep the API running on port 8081.
2. In a second terminal, run [okta-spring-boot-sample-app](https://github.com/okta-samples/okta-spring-boot-sample-app) using the same `ISSUER`.
3. Open `http://localhost:8080`, sign in, and click **Messages**. The app calls `/api/messages` with your access token and shows the response.
