#!/usr/bin/env bash

set -euo pipefail

if [ "$#" -ne 2 ]; then
    echo "Usage: $0 <version> <install-dir>" >&2
    exit 1
fi

version="$1"
install_dir="$2"

if [ -x "${install_dir}/zig" ] && [ "$("${install_dir}/zig" version 2>/dev/null || true)" = "${version}" ]; then
    echo "Zig ${version} already installed at ${install_dir}"
    exit 0
fi

case "$(uname -s)" in
    Linux) os="linux" ;;
    Darwin) os="macos" ;;
    *) echo "Unsupported OS: $(uname -s)" >&2; exit 1 ;;
esac

case "$(uname -m)" in
    x86_64|amd64) arch="x86_64" ;;
    aarch64|arm64) arch="aarch64" ;;
    *) echo "Unsupported architecture: $(uname -m)" >&2; exit 1 ;;
esac

archive_name="zig-${arch}-${os}-${version}"
url="https://ziglang.org/download/${version}/${archive_name}.tar.xz"

mkdir -p "$(dirname "${install_dir}")"
tmp_dir="$(mktemp -d "${install_dir}.tmp.XXXXXX")"
trap 'rm -rf "${tmp_dir}"' EXIT

echo "Downloading ${url}"
curl -fsSL --retry 3 "${url}" | tar -xJ -C "${tmp_dir}"

rm -rf "${install_dir}"
mv "${tmp_dir}/${archive_name}" "${install_dir}"

echo "Installed Zig $("${install_dir}/zig" version) to ${install_dir}"
