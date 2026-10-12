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

# SHA-256 of each release archive, from https://ziglang.org/download/index.json.
# Add the entries for a new version when bumping zig in gradle/libs.versions.toml.
case "${archive_name}" in
    zig-x86_64-linux-0.17.0) sha256="1cbe9df9f27e6b78d14ccbca43b6703a404ef79ef1c463de901d7f088d4e2026" ;;
    zig-aarch64-linux-0.17.0) sha256="9e8d11661d4ae3bd57702a3832781e23ad151dde5798e16a5ccd503f65234ff8" ;;
    zig-x86_64-macos-0.17.0) sha256="4f9a1c5269aa17ebda5e6d3c2b89d6cbf36f7d2b22a0306e9ab98f25f95529c6" ;;
    zig-aarch64-macos-0.17.0) sha256="b607e9b9234790a008116ae5bdb71c6243b84b9fb42a53a9e70fde41c06c536a" ;;
    *) echo "No known SHA-256 for ${archive_name}, add it to $0" >&2; exit 1 ;;
esac

mkdir -p "$(dirname "${install_dir}")"
tmp_dir="$(mktemp -d "${install_dir}.tmp.XXXXXX")"
trap 'rm -rf "${tmp_dir}"' EXIT

echo "Downloading ${url}"
curl -fsSL --retry 3 -o "${tmp_dir}/${archive_name}.tar.xz" "${url}"

if command -v sha256sum >/dev/null 2>&1; then
    actual="$(sha256sum "${tmp_dir}/${archive_name}.tar.xz" | cut -d' ' -f1)"
else
    actual="$(shasum -a 256 "${tmp_dir}/${archive_name}.tar.xz" | cut -d' ' -f1)"
fi

if [ "${actual}" != "${sha256}" ]; then
    echo "SHA-256 mismatch for ${url}: expected ${sha256}, got ${actual}" >&2
    exit 1
fi

tar -xJf "${tmp_dir}/${archive_name}.tar.xz" -C "${tmp_dir}"

rm -rf "${install_dir}"
mv "${tmp_dir}/${archive_name}" "${install_dir}"

echo "Installed Zig $("${install_dir}/zig" version) to ${install_dir}"
