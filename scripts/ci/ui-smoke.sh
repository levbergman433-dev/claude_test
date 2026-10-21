#!/usr/bin/env bash
# UI smoke test on an Android emulator, run by CI after the APK is built.
#
# Usage: ui-smoke.sh APK
#
# It installs the APK, starts it on a known set of settings, walks Home → Library → a song →
# Now Playing for each Now Playing style under test, and reports what it saw as notice
# annotations on the job: one screenshot per stop ("shot:NAME", a base64 JPEG) and the frame
# statistics of every measured stretch ("stats"). A crash in the app's logcat becomes an error
# annotation. Nothing here fails the build on slow frames: the emulator renders in software, so
# the numbers are for comparing one build with another, not for an absolute verdict.

set -u

APK="$1"
PKG=com.maxrave.simpmusic.mod
ACTIVITY=com.maxrave.simpmusic.MainActivity
OUT=ui-out
# A video every region can play; with "watch video" off (the default) it plays as a song.
SONG=dQw4w9WgXcQ
STATS=""

mkdir -p "$OUT"

log() { echo "[ui-smoke] $*"; }

# Escapes text for a workflow command: %, CR and LF.
escape() { sed -e 's/%/%25/g' -e 's/\r//g' | sed -e ':a;N;$!ba;s/\n/%0A/g'; }

shot() {
    adb exec-out screencap -p > "$OUT/$1.png" && python3 scripts/ci/shot.py "$OUT/$1.png" "$1"
}

open_link() {
    adb shell am start -W -a android.intent.action.VIEW -d "$1" "$PKG" > /dev/null
}

reset_frames() {
    adb shell dumpsys gfxinfo "$PKG" reset > /dev/null
}

# Appends the frame statistics gathered since the last reset under the heading $1.
frames() {
    adb shell dumpsys gfxinfo "$PKG" > "$OUT/gfx-$1.txt"
    local summary
    summary=$(grep -E "Total frames rendered|Janky frames|th percentile|Number Missed Vsync|Number Slow UI thread|Number Slow issue draw commands|Number Frame deadline missed|gpu percentile" "$OUT/gfx-$1.txt" | sed 's/^ *//')
    STATS+="[$1]"$'\n'"$summary"$'\n'
    log "frames $1:"
    echo "$summary"
}

# Replaces the app's settings with the given key=value strings. The app must not be running.
set_prefs() {
    python3 scripts/ci/prefs.py "$OUT/settings.preferences_pb" "$@"
    local data="/data/data/$PKG"
    local dir="$data/files/datastore"
    adb shell mkdir -p "$dir"
    adb push "$OUT/settings.preferences_pb" "$dir/settings.preferences_pb" > /dev/null
    local owner context
    owner=$(adb shell stat -c %u "$data" | tr -d '\r')
    context=$(adb shell ls -Zd "$data" | tr -d '\r' | awk '{print $1}')
    adb shell chown -R "$owner:$owner" "$data/files"
    adb shell chcon -R "$context" "$data/files"
}

# One pass through the app with the Now Playing style $1.
run_pass() {
    local style="$1"
    local tag
    tag=$(echo "$style" | tr '[:upper:]' '[:lower:]')
    log "pass: $style"
    adb shell am force-stop "$PKG"
    set_prefs "now_playing_style=$style" "liquid_glass=TRUE"
    adb shell am start -W -n "$PKG/$ACTIVITY" > /dev/null
    sleep 25
    if [ "$style" = "APPLE_MUSIC_GLASS" ]; then
        shot home

        open_link "simpmusic://library"
        sleep 8
        shot library
        reset_frames
        for _ in 1 2 3; do adb shell input swipe 540 1900 540 700 250; sleep 1; done
        shot library-scrolled
        for _ in 1 2 3; do adb shell input swipe 540 700 540 1900 250; sleep 1; done
        frames library-scroll
    fi

    open_link "simpmusic://watch?v=$SONG"
    sleep 25
    [ "$style" = "APPLE_MUSIC_GLASS" ] && shot playing

    reset_frames
    open_link "simpmusic://player"
    sleep 4
    frames "np-open-$tag"
    reset_frames
    sleep 12
    frames "np-idle-$tag"
    shot "nowplaying-$tag"
    adb shell input keyevent KEYCODE_BACK
    sleep 2
}

log "waiting for the emulator"
adb wait-for-device
adb shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 1; done'
adb root > /dev/null 2>&1
sleep 3
adb wait-for-device
adb shell wm size

log "installing $APK"
if ! adb install -r -g "$APK"; then
    echo "::error title=install::adb install failed"
    exit 1
fi

adb logcat -c
run_pass APPLE_MUSIC_GLASS
run_pass SPOTIFY

if ! adb shell pidof "$PKG" > /dev/null; then
    echo "::error title=process::the app is no longer running at the end of the test"
fi
crashes=$(adb logcat -d -b crash | tail -c 6000)
if [ -n "$crashes" ]; then
    echo "::error title=crash::$(printf '%s' "$crashes" | escape)"
fi
echo "::notice title=stats::$(printf '%s' "$STATS" | escape)"
log "done"
