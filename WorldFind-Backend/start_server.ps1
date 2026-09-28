while ($true) {
    node dist/server.js | Out-File -FilePath "C:\Users\mural\AndroidStudioProjects\WorldFind\WorldFind-Backend\server.log" -Encoding utf8
    Start-Sleep -Seconds 2
}
