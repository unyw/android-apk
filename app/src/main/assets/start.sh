#!/bin/sh
# Start proot environment
./busybox rm -rf   "$INTERNAL_STORAGE/rootfs/etc/shadow+"
./busybox rm -rf   "$INTERNAL_STORAGE/rootfs/run"
./busybox mkdir -p "$INTERNAL_STORAGE/rootfs/run/unyw/dtach"
./busybox mkdir -p "$INTERNAL_STORAGE/rootfs/run/unyw/dtach-pids"
./busybox mkdir -p "$INTERNAL_STORAGE/storage"

LD_LIBRARY_PATH="$INTERNAL_STORAGE/pulse/libs" HOME="$INTERNAL_STORAGE/pulse/home" TMPDIR="$INTERNAL_STORAGE/pulse/tmp" $INTERNAL_STORAGE/pulse/pulseaudio -n -L "module-null-sink" -L "module-native-protocol-tcp port=12332 auth-anonymous=true" -L "module-simple-protocol-tcp record=true source=0 port=12333" &

./busybox echo "$UNYW_TOKEN_API" > "$INTERNAL_STORAGE/rootfs/tmp/UNYW_TOKEN_API"
./proot.sh "ash /usr/share/unyw-bridge-android/start.sh"
