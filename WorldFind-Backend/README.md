# WorldFind Backend API

Backend API for **WorldFind**, an India-focused e-commerce application powered by the AliExpress Open Platform (Affiliate API).

> **Important Verification Notice:**
> **AliExpress live integration is pending verification against the active OpenService application credentials and protocol.**
> Public legacy Affiliate API documentation must NOT be mixed with the application's active OpenService protocol.

---

## 1. Project Purpose

WorldFind Backend serves as a secure intermediate server between the WorldFind Android app and the AliExpress Open Platform API. It handles:
- **Security:** Hiding sensitive secrets (`app_secret`, `access_token`, `refresh_token`, Firebase private keys) from the Android client.
- **Localization:** Setting default delivery target to **India (`ship_to_country = IN`)**, prices in **INR (`target_currency = INR`)**, and language to **English (`target_language = EN`)**.
- **Product Normalization:** Transforming raw AliExpress Open Platform payloads into clean WorldFind standard product DTOs.
- **Authentication:** Managing OAuth 2.0 authorization code flow and AES-256-GCM encrypted token persistence at rest.

---

## 2. Architecture

```
┌─────────────────────────┐
│ WorldFind Android Client│
└────────────┬────────────┘
             │ HTTPS (WorldFind DTO)
             ▼
┌─────────────────────────┐
│   WorldFind Backend     │
│ (Express.js / Node.js)   │
│  ┌───────────────────┐  │
│  │ Protocol Adapters │  │
│  └───────────────────┘  │
└────────────┬────────────┘
             │ Signed IoP / TOP Protocol Requests
             ▼
┌─────────────────────────┐
│ AliExpress Open Platform│
└─────────────────────────┘
```

---

## 3. Folder Structure

```
WorldFind-Backend/
├── src/
│   ├── config/
│   │   ├── env.ts                   # Environment schema & validation (Zod)
│   │   └── firebase.ts              # Firebase Admin SDK & Firestore initialization
│   ├── controllers/
│   │   ├── health.controller.ts     # /health & root info controller
│   │   ├── aliexpress.controller.ts # OAuth callback & token status controller
│   │   └── product.controller.ts    # Product search, details & affiliate link controller
│   ├── middleware/
│   │   ├── auth.middleware.ts       # API Key auth middleware
│   │   ├── error.middleware.ts      # Global centralized error handler
│   │   └── rateLimit.middleware.ts  # Rate limiting middleware
│   ├── models/
│   │   ├── Product.ts               # WorldFind Product DTOs & filter interfaces
│   │   └── AliExpressToken.ts       # Token & encrypted storage interfaces
│   ├── routes/
│   │   ├── health.routes.ts         # /health & / routes
│   │   ├── aliexpress.routes.ts     # /api/aliexpress/* routes
│   │   └── product.routes.ts        # /api/products & /api/categories routes
│   ├── services/
│   │   ├── aliexpress/
│   │   │   ├── adapters/
│   │   │   │   ├── aliexpress.protocol.adapter.ts # Protocol request wrapper & app_signature handler
│   │   │   │   ├── aliexpress.oauth.adapter.ts    # OAuth token adapter
│   │   │   │   └── aliexpress.product.adapter.ts  # Product API adapter
│   │   │   ├── aliexpress.client.ts            # Core HTTP client with signing
│   │   │   ├── aliexpress.oauth.service.ts     # OAuth flow & token exchange
│   │   │   ├── aliexpress.product.service.ts   # Product query & link service
│   │   │   ├── aliexpress.signature.service.ts # HMAC-SHA256 signature calculation
│   │   │   └── aliexpress.token.service.ts     # AES-256-GCM token storage service
│   │   └── product/
│   │       └── product.normalizer.ts           # Response normalizer into WorldFind DTO
│   ├── utils/
│   │   ├── errors.ts                # Custom AppError classes
│   │   └── logger.ts                # Winston logger with secret sanitization
│   ├── app.ts                       # Express application setup
│   └── server.ts                    # HTTP server entry point
├── tests/
│   ├── health.test.ts               # Health check integration tests
│   ├── normalizer.test.ts           # Product normalizer unit tests
│   ├── oauth.test.ts                # OAuth & encryption unit tests
│   ├── security.test.ts             # Security & state protection unit tests
│   ├── signature.test.ts            # HMAC-SHA256 signature unit tests
│   └── validation.test.ts           # Zod query & body validation tests
├── .env.example
├── .gitignore
├── jest.config.js
├── package.json
├── tsconfig.json
└── README.md
```

---

## 4. Environment Variables

Create `.env` based on `.env.example`:

| Environment Variable | Description |
| :--- | :--- |
| `PORT` | HTTP Server port (Default: `8080`) |
| `NODE_ENV` | Environment (`development` / `production` / `test`) |
| `WORLD_FIND_API_BASE_URL` | Public base URL of backend |
| `CORS_ORIGIN` | Allowed CORS origins (e.g. `https://admin.worldfind.app`) |
| `FIREBASE_PROJECT_ID` | Firebase Project ID (REQUIRED in production) |
| `FIREBASE_CLIENT_EMAIL` | Firebase Admin Service Account Client Email (REQUIRED in production) |
| `FIREBASE_PRIVATE_KEY` | Firebase Admin Service Account Private Key (REQUIRED in production) |
| `ALIEXPRESS_APP_KEY` | App Key from AliExpress Open Platform Console |
| `ALIEXPRESS_APP_SECRET` | App Secret from AliExpress Open Platform Console |
| `ALIEXPRESS_API_BASE_URL` | Gateway URL (Default: `https://api-sg.aliexpress.com/sync`) |
| `ALIEXPRESS_CALLBACK_URL` | Registered HTTPS callback URL for OAuth |
| `ALIEXPRESS_APP_SIGNATURE` | Optional app_signature parameter if required |
| `TOKEN_ENCRYPTION_KEY` | 64-hex character or strong secret key for AES-256-GCM token encryption |
| `ADMIN_API_KEY` | API Key for protecting administrative endpoints |
| `DEFAULT_SHIP_TO_COUNTRY` | Default target country (`IN`) |
| `DEFAULT_TARGET_CURRENCY` | Default target currency (`INR`) |
| `DEFAULT_TARGET_LANGUAGE` | Default target language (`EN`) |

---

## 5. Local Setup & Commands

```bash
# 1. Install dependencies
npm install

# 2. Run local development server
npm run dev

# 3. Execute unit & integration tests
npm test

# 4. Build TypeScript for production
npm run build

# 5. Start production server
npm start
```

---

## 6. Firebase Admin Setup

1. Open your Firebase Console project.
2. Navigate to **Project Settings > Service Accounts**.
3. Click **Generate new private key**.
4. Set `FIREBASE_PROJECT_ID`, `FIREBASE_CLIENT_EMAIL`, and `FIREBASE_PRIVATE_KEY` in `.env`.
   - Ensure escaped newline characters `\n` in `FIREBASE_PRIVATE_KEY` are preserved in `.env`.

---

## 7. AliExpress App Console Setup Instructions

When filling out the AliExpress Open Platform **Fill App Information** form:

1. **App Name:** `WorldFind`
2. **Callback URL:** Enter your live deployed HTTPS backend callback URL:
   `https://<your-deployed-domain>.com/api/aliexpress/callback`
   *(Do NOT use localhost in production or fake domains like worldfind.com unless owned).*
3. **App Key & App Secret:** Once created, copy the generated App Key and App Secret into your `.env` variables (`ALIEXPRESS_APP_KEY`, `ALIEXPRESS_APP_SECRET`).

---

## 8. AliExpress OAuth 2.0 Flow

1. Open `GET /api/aliexpress/auth-url` to retrieve the authorization URL containing a cryptographically random `state`.
2. Navigate to the URL in browser and log in with your authorized AliExpress seller/affiliate account.
3. Authorize the WorldFind application.
4. AliExpress redirects to `GET /api/aliexpress/callback?code=...&state=...`.
5. Backend verifies state, consumes it (single-use), exchanges code for `access_token` and `refresh_token` using `/auth/token/create`.
6. Tokens are encrypted using AES-256-GCM with independent IVs/authTags and stored in Firestore (`aliExpressTokens/main_account`).

---

## 9. API Routes & Examples

### Health Check
`GET /health`
```json
{
  "success": true,
  "service": "WorldFind Backend",
  "status": "healthy",
  "timestamp": "2025-01-01T12:00:00.000Z"
}
```

### Search Products
`GET /api/products?keywords=wireless+earbuds&sort=SALE_PRICE_ASC&targetCurrency=INR`

**Sample Response:**
```json
{
  "success": true,
  "data": {
    "products": [
      {
        "id": "1005001234567890",
        "title": "TWS Wireless Bluetooth Earbuds Noise Reduction",
        "imageUrl": "https://ae01.alicdn.com/kf/S123456.jpg",
        "price": {
          "amount": 362.5,
          "currency": "INR",
          "formatted": "₹362.50"
        },
        "originalPrice": {
          "amount": 450.0,
          "currency": "INR",
          "formatted": "₹450.00"
        },
        "discountPercent": 19,
        "country": {
          "code": "IN",
          "name": "India"
        },
        "delivery": {
          "estimatedDays": 7,
          "shipToCountry": "IN"
        },
        "source": "aliexpress"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "pageSize": 20,
      "currentRecordCount": 20,
      "totalPages": 5,
      "totalCount": 100,
      "hasNextPage": true
    }
  }
}
```

### Get Product Detail
`GET /api/products/1005001234567890`

### Generate Affiliate Links
`POST /api/products/affiliate-link`

---

## 10. Security Rules

1. **Client Isolation:** Android client communicates ONLY with WorldFind Backend.
2. **Zero Secrets on Android:** Never embed `app_secret`, `access_token`, `refresh_token`, or Firebase Admin keys in the Android APK.
3. **Encrypted Storage:** Tokens are encrypted at rest with AES-256-GCM using independent IVs and authTags before saving to Firestore.
4. **Log Sanitization:** Sensitive keys are automatically redacted from winston loggers.
