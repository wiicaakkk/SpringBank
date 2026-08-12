#!/bin/bash
# Script praktis untuk menjalankan Core Banking Spring Boot
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR" || exit 1

if [ -f "$HOME/.sdkman/bin/sdkman-init.sh" ]; then
    source "$HOME/.sdkman/bin/sdkman-init.sh"
fi

echo "🚀 Menjalankan Core Banking Spring Boot System..."
./mvnw spring-boot:run
