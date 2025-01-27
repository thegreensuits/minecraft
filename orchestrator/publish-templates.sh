#!/bin/bash

GITHUB_OWNER="thegreensuits"
GITHUB_REPO="minecraft"

TEMPLATES_DIR="orchestrator/docker/templates"

TEMPLATES=(
    "proxy:minecraft-proxy"
    "hub:minecraft-hub"
    "survival:minecraft-survival"
)

build_and_publish() {
    local template=$1
    local image_name=$2

    IFS=':' read -r template_dir image_tag <<<"$template"

    completeDir="${TEMPLATES_DIR}/${template_dir}"

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
