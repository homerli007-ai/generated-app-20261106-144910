# Safe File Deleter

A small full-stack demo that deletes files from a sandboxed directory on the server, with strict path validation. The React frontend lets you submit one relative path per line and shows a per-path result table.

## Stack

Spring Boot 3.2 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/example/demo/
│       │   ├── DemoApplication.java
│       │   └── controller/ApiController.java
│       └── resources/application.properties
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── App.css
        └── main.jsx
```

## How to run

Backend (port 8080):

```
cd backend && mvn spring-boot:run
```

Frontend (port 5173, proxies `/api` to the backend):

```
cd frontend && npm install && npm run dev
```

Then open http://localhost:5173.

## API

Base path: `/api`

- `POST /api/delete` — delete one or more files under the allowed root.
  - Request body: `{ "paths": ["docs/old.txt", "tmp/scratch.log"] }`
  - Response body: `{ "results": [ { "path": "...", "deleted": true, "reason": "deleted" } ] }`
  - Each path is validated independently; failures are reported per entry rather than aborting the batch.

## Notes

- The allowed root is configured via `app.allowed-root` (default `./allowed_root`, resolved relative to the backend working directory). Paths are resolved against it and rejected if they escape it after normalization.
- Only regular files are deleted. Symlinks, directories, and missing paths are refused with a reason.
- There is no authentication or authorization — this is a demo and should not be exposed publicly.
- The frontend relies on the Vite dev proxy; for production you would serve the built `frontend/dist` from Spring Boot's static resources (or a reverse proxy) and adjust CORS/proxy accordingly.
- Deletions are permanent (no trash/undo).