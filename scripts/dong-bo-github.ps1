param(
    [string]$CommitMessage = "feat: initialize CarTracker Android app with GPS tracking and Firebase sync"
)

$ErrorActionPreference = "Stop"

if (-not (Test-Path ".git")) {
    throw "Hãy chạy tệp này trong thư mục repository đã clone từ GitHub."
}

Write-Host "Repository:" (git remote get-url origin)
git add .
$pending = git status --porcelain
if ([string]::IsNullOrWhiteSpace($pending)) {
    Write-Host "Không có thay đổi mới để commit."
    exit 0
}

git commit -m $CommitMessage
git push origin main
Write-Host "Đã đẩy mã nguồn lên nhánh main."
