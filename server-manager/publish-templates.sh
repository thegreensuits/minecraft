#!/bin/bash

GITHUB_OWNER="thegreensuits"
GITHUB_REPO="minecraft"

TEMPLATES_DIR="docker/templates"

TEMPLATES=(
    "proxy:minecraft-proxy"
    "hub:minecraft-hub"
)

build_and_publish() {
    local template=$1
    local image_name=$2

    completeDir="${TEMPLATES_DIR}/${template}"

    IFS=':' read -r template_dir image_tag <<<"$template"

    docker build \
        -t "ghcr.io/${GITHUB_OWNER}/${GITHUB_REPO}/${image_tag}:latest" \
        -f "${completeDir}/Dockerfile" \
        "${completeDir}"

    docker push "ghcr.io/${GITHUB_OWNER}/${GITHUB_REPO}/${image_tag}:latest"
}

main() {
    for template in "${TEMPLATES[@]}"; do
        build_and_publish "$template"
    done
}

main
