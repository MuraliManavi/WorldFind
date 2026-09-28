@echo off
cd /d C:\Users\mural\AndroidStudioProjects\WorldFind\WorldFind-Backend
start /b node dist/server.js > server.log 2>&1
:loop
npx --yes localtunnel --port 8080 --subdomain worldfind-backend-app > localtunnel.log 2>&1
timeout /t 3
goto loop
