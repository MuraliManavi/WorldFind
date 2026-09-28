while ($true) {
    ssh -o ServerAliveInterval=15 -o StrictHostKeyChecking=no -R 80:localhost:8080 nokey@localhost.run | Out-File -FilePath "C:\Users\mural\AndroidStudioProjects\WorldFind\WorldFind-Backend\localhostrun.log" -Encoding utf8
    Start-Sleep -Seconds 2
}
