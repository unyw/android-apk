#!/bin/sh

# Start debian proot sessions
# This script is responsible of configuring the emulation environment and launch
# the executable passed as first param.
# Example:  ./proot.sh "echo Hello, I'm inside Unyw!"


# Options
LD_PRELOAD_DIR="/ld_preload"


# Set Proot and Android linker flags
export LD_LIBRARY_PATH="$LD_LIBRARY_PATH:$INTERNAL_STORAGE/"
export PROOT_TMPDIR=`pwd`/prootdir/tmp
export PROOT_TMP_DIR=$PROOT_TMPDIR


# Extra bindings - bind fake version of process info and shmem if they don't exist
# On android >= Oreo, some files aren't available for security reasons/have changed
# location, so we need to remap them with proot mount -b option.
# Taken from https://github.com/CypherpunkArmory/UserLAnd-Assets-Support/blob/staging/assets/all/execInProot.sh
if [[ ! -r /dev/ashmem ]] ; then    # Check linux ashmem support
	EXTRA_BINDINGS="$EXTRA_BINDINGS -b $INTERNAL_STORAGE/prootdir/shm:/dev/ashmem"
fi
if [[ ! -r /dev/shm ]] ; then       # Check linux shm (shared memory) support
	EXTRA_BINDINGS="$EXTRA_BINDINGS -b $INTERNAL_STORAGE/prootdir/shm:/dev/shm"
fi
if [[ ! -r /proc/stat ]] ; then     # Check proc stat support
	numProc="$($INTERNAL_STORAGE/busybox grep processor /proc/cpuinfo)"
	numProc="${numProc: -1}"
	if [[ "$numProc" -le "3" ]] 2>/dev/null ; then
		EXTRA_BINDINGS="$EXTRA_BINDINGS -b $INTERNAL_STORAGE/prootdir/stat4:/proc/stat"
	else
		EXTRA_BINDINGS="$EXTRA_BINDINGS -b $INTERNAL_STORAGE/prootdir/stat8:/proc/stat"
	fi
fi
if [[ ! -r /proc/uptime ]] ; then
	EXTRA_BINDINGS="$EXTRA_BINDINGS -b $INTERNAL_STORAGE/prootdir/uptime:/proc/uptime"
fi
if [[ ! -r /proc/version ]] ; then  # Check linux version support
	currDate="$($INTERNAL_STORAGE/busybox date)"
	echo "Linux version $OS_VERSION (fake@unyw) #1 $currDate" > $INTERNAL_STORAGE/prootdir/version
	EXTRA_BINDINGS="$EXTRA_BINDINGS -b $INTERNAL_STORAGE/prootdir/version:/proc/version"
fi


# Launch debian system and set unyw environment
./proot -0 -L -l -H -p -r ./rootfs -w /                                  \
    -b /dev -b /proc -b /sys -b "$UNYW_STORAGE:/storage/unyw" \
    -b "$INTERNAL_STORAGE/storage:/storage/internal!" \
    -b "$EXTERNAL_STORAGE:/storage/external!" -b /proc/mounts:/etc/mtab $EXTRA_BINDINGS \
    /usr/bin/env PATH=/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin                   \
    UNYW_TOKEN_API="$UNYW_TOKEN_API" UNYW_TOKEN_VNC="$UNYW_TOKEN_VNC" UNYW_TOKEN_SSH="$UNYW_TOKEN_SSH" \
    SHELL="/bin/ash" LD_PRELOAD="$LD_PRELOAD_DIR/libdisableselinux.so" LIBGL_ALWAYS_SOFTWARE=1       \
    SCREEN_WIDTH="$SCREEN_WIDTH" SCREEN_HEIGHT="$SCREEN_HEIGHT" \
    DISPLAY=:3200 TMPDIR="/tmp" HOME="/root" PREFIX="/usr" LD_LIBRARY_PATH="/usr/lib"  sh -c "$1"
