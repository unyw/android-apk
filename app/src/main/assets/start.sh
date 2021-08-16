#!/bin/sh
# Start proot environment
./busybox rm -rf   "$INTERNAL_STORAGE/rootfs/etc/shadow+"
./busybox rm -rf   "$INTERNAL_STORAGE/rootfs/run"
./busybox mkdir -p "$INTERNAL_STORAGE/rootfs/run/unyw/dtach"
./busybox mkdir -p "$INTERNAL_STORAGE/rootfs/run/unyw/dtach-pids"
./busybox mkdir -p "$INTERNAL_STORAGE/storage"

./busybox echo "$UNYW_TOKEN_API" > "$INTERNAL_STORAGE/rootfs/tmp/UNYW_TOKEN_API"
./proot.sh "ash /usr/share/unyw-bridge-android/start.sh"
