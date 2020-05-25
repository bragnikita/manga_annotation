#!/usr/bin/env bash
set -euo pipefail

WORK_ID=${1:? "Id not specified"}
SERIES_NAME=${2:-${WORK_ID}}
URL="https://www.pixiv.net/ajax/illust/${WORK_ID}/pages?lang=en"
mkdir -p "${SERIES_NAME}"
curl "${URL}" | node pixiv_downloader.js "${WORK_ID}" "${SERIES_NAME}" | curl -K -