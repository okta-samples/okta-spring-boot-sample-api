# Okta Spring Boot API

This example shows you how to use the [Okta Spring Boot Starter][] to protect a REST API with OAuth 2.0. The API validates incoming JWT access tokens and returns data to authorized callers.

## Prerequisites

- [Java 25+](https://sdkman.io/jdks)
- An Okta Integrator Free Plan account — sign up at [developer.okta.com](https://developer.okta.com/login)

## Add a Custom Scope and an Access Policy

This sample uses a custom scope, `messages.read`, to show scope-based authorization. In the Okta Admin Console:

1. Go to **Security > API** and open your authorization server (`default` works)
2. On the **Scopes** tab, click **Add Scope**, name it `messages.read`, and **Create**
3. On the **Access Policies** tab, confirm at least one policy exists with a rule that grants your scopes

> **Step 3 is not optional.** A custom authorization server with no access policy issues **no tokens at all**, and the failure appears while you are requesting the token — before any of this code runs. If token requests fail with `access_denied`, this is almost always why.

Requires API Access Management (included in Okta Integrator Free Plan accounts).

## Configure

Copy `.env.example` to `.env` and fill in your values:

```bash
cp .env.example .env
```

```text
ISSUER=https://your-okta-domain/oauth2/default
AUDIENCE=api://default
```

Find your **Issuer** at **Security > API** in the Admin Console. The default audience is `api://default`.

The `AUDIENCE` value matters: the starter rejects any token whose `aud` claim does not match it. If a token that looks valid returns `401`, compare its `aud` against this setting.

## Run

Load `.env` into your shell, then start the app:

```bash
set -a && source .env && set +a
./mvnw spring-boot:run
```

The API starts on port **8081**.

## Get an Access Token

You need an access token to call the protected endpoints. This API has no login UI of its own, so you need to get a token yourself:

### A real user's token (Authorization Code + PKCE, no app required)

Get a user token directly with `curl` and `openssl` — no browser-based app needs to be running.

1. Create a throwaway **SPA** app integration just for this: **Applications > Create App Integration > OIDC - OpenID Connect > Single-Page Application**. Set:
   - **Sign-in redirect URI**: `http://localhost:8888/callback`
   - **Grant type**: Authorization Code (default)

   Copy its **Client ID** from the **General** tab.

2. Generate a PKCE pair:

   ```bash
   CODE_VERIFIER=$(openssl rand -base64 32 | tr -d '\n=' | tr '+/' '-_')
   CODE_CHALLENGE=$(printf '%s' "$CODE_VERIFIER" | openssl dgst -sha256 -binary | openssl base64 | tr -d '\n=' | tr '+/' '-_')
   ```

3. Build the authorize URL and open it in a browser:

   ```bash
   CLIENT_ID=<the SPA app's client ID>
   echo "$ISSUER/v1/authorize?client_id=$CLIENT_ID&response_type=code&scope=openid%20profile%20messages.read&redirect_uri=http://localhost:8888/callback&state=test&code_challenge=$CODE_CHALLENGE&code_challenge_method=S256"
   ```

   Open the printed URL, log in. Okta redirects to `http://localhost:8888/callback?code=...&state=test` — nothing is listening on port 8888, so the browser shows a connection-refused page. That's expected; copy the `code` value out of the address bar.

4. Exchange the code for a token:

   ```bash
   CODE=<the code from the redirect URL>
   curl -s -X POST "$ISSUER/v1/token" \
     -H "Content-Type: application/x-www-form-urlencoded" \
     -d grant_type=authorization_code \
     -d client_id="$CLIENT_ID" \
     -d code="$CODE" \
     -d redirect_uri=http://localhost:8888/callback \
     -d code_verifier="$CODE_VERIFIER"
   ```

   The response's `access_token` has a real user as `sub`. To test the scope-rejection path, repeat with `scope=openid%20profile` (drop `messages.read`) and confirm `/api/messages` returns `403` with that token.

## Test the API

```bash
TOKEN=eyJraWQiOiJ...

# Any valid token
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/hello
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/whoami

# Requires the messages.read scope
curl -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/messages
```

Or with [HTTPie](https://httpie.io/cli):

```bash
http :8081/api/hello Authorization:"Bearer $TOKEN"
```

### Confirm it actually rejects bad requests

A resource server is only interesting if it says no. All three of these should fail:

```bash
# No token at all → 401
curl -i http://localhost:8081/api/hello

# Malformed token → 401
curl -i -H "Authorization: Bearer not-a-real-token" http://localhost:8081/api/hello

# Valid token, missing the messages.read scope → 403
curl -i -H "Authorization: Bearer $TOKEN" http://localhost:8081/api/messages
```

The difference between **401** and **403** is the thing to notice: 401 means "I don't know who you are," 403 means "I know who you are, and you lack the required scope."

## Endpoints

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/api/hello` | Valid token | Returns `Hello, {subject}!` |
| GET | `/api/whoami` | Valid token | Returns all JWT claims as JSON |
| GET | `/api/messages` | `messages.read` scope | Returns sample data |

## How It Works

`SecurityConfiguration` configures only `oauth2ResourceServer(jwt)`. There is no login flow, no session, and no cookies — the API is fully stateless, and CSRF protection is disabled because there are no cookies to forge.

Two layers of authorization:

- **Chain level** — `anyRequest().authenticated()` requires a valid token for every endpoint.
- **Method level** — `@PreAuthorize("hasAuthority('SCOPE_messages.read')")` requires a specific scope. Spring maps each scope in the token to a `SCOPE_*` authority.

Method-level checks require `@EnableMethodSecurity` on the configuration class. Without that annotation `@PreAuthorize` is **silently ignored** and the endpoint is left unprotected.


## Related Samples

- [okta-samples](https://github.com/okta-samples/) — Explore the other samples from this and build with Okta and other frameworks.

## Learn More

- [Okta Spring Boot Starter](https://github.com/okta/okta-spring-boot)
- [OAuth 2.0 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Create custom scopes](https://developer.okta.com/docs/guides/customize-authz-server/main/#create-scopes)

[Okta Spring Boot Starter]: https://github.com/okta/okta-spring-boot
