@echo off
setlocal

set "MAVEN_CMD="
for /f "delims=" %%F in ('dir /s /b "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.11\mvn.cmd" 2^>nul') do (
  set "MAVEN_CMD=%%F"
  goto :found
)

:found
if not defined MAVEN_CMD (
  echo Maven 3.9.11 was not found in %USERPROFILE%\.m2\wrapper\dists.
  echo Run the original JChatMind backend once, install Maven globally, or regenerate Maven Wrapper.
  exit /b 1
)

call "%MAVEN_CMD%" %*
