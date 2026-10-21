#!/usr/bin/env bash
# UI smoke test on an Android emulator, run by CI after the APK is built.
#
# Usage: ui-smoke.sh APK
#
# It installs the APK and, for each Now Playing style under test, starts the app on a known set of
# settings and walks Home → Library → a song → Now Playing. What it saw is posted as check runs on
# the commit, because the API serves those where artifact downloads are blocked:
#   ui-shot: NAME   a screenshot, base64 JPEG in the output text
#   ui-stats        dumpsys gfxinfo summaries of every measured stretch, plus any crash found
#                   in logcat, and whether the emulator itself went down
# Nothing here fails the build on slow frames: the emulator renders in software, so the numbers
# are for comparing one build with another, not for an absolute verdict.
#
# Needs GH_TOKEN, GITHUB_REPOSITORY and GITHUB_SHA in the environment.

set -u

APK="$1"
PKG=com.maxrave.simpmusic.mod
ACTIVITY=com.maxrave.simpmusic.MainActivity
OUT=ui-out
# A video every region can play; with "watch video" off (the default) it plays as a song.
SONG=dQw4w9WgXcQ
EMULATOR_OPTIONS="-no-window -gpu swiftshader_indirect -no-snapshot -no-boot-anim -camera-back none"
STATS="$OUT/stats.txt"

mkdir -p "$OUT"
: > "$STATS"

log() { echo "[ui-smoke] $*"; }
note() { echo "$*" >> "$STATS"; log "$*"; }

post_check() {
    gh api "repos/$GITHUB_REPOSITORY/check-runs" -X POST \
        -f name="$1" -f head_sha="$GITHUB_SHA" -f status=completed -f conclusion=neutral \
        -f "output[title]=$1" -f "output[summary]=UI smoke test output" -F "output[text]=@$2" > /dev/null \
        || echo "::warning::could not post $1"
}

alive() { [ "$(timeout 10 adb get-state 2>/dev/null)" = "device" ]; }

wait_boot() {
    timeout 300 adb wait-for-device
    timeout 300 adb shell 'while [ "$(getprop sys.boot_completed)" != "1" ]; do sleep 1; done'
    adb root > /dev/null 2>&1
    sleep 3
    timeout 60 adb wait-for-device
}

# Brings the emulator back if it went down, so the next pass can still run and the action's own
# teardown does not wait for a device that is gone.
revive() {
    alive && return 0
    note "!! emulator went down; restarting it"
    pkill -f qemu-system > /dev/null 2>&1
    adb kill-server > /dev/null 2>&1
    sleep 2
    # shellcheck disable=SC2086
    nohup "$ANDROID_HOME/emulator/emulator" -avd test $EMULATOR_OPTIONS > "$OUT/emulator-restart.log" 2>&1 &
    adb start-server > /dev/null 2>&1
    wait_boot
}

shot() {
    alive || return 0
    adb exec-out screencap -p > "$OUT/$1.png" \
        && python3 scripts/ci/shot.py "$OUT/$1.png" "$OUT/$1.b64" \
        && post_check "ui-shot: $1" "$OUT/$1.b64"
}

open_link() {
    timeout 60 adb shell am start -W -a android.intent.action.VIEW -d "$1" "$PKG" > /dev/null
}

reset_frames() {
    timeout 20 adb shell dumpsys gfxinfo "$PKG" reset > /dev/null
}

# Appends the frame statistics gathered since the last reset under the heading $1.
frames() {
    if ! alive; then
        note "[$1] emulator down"
        return
    fi
    timeout 30 adb shell dumpsys gfxinfo "$PKG" > "$OUT/gfx-$1.txt"
    {
        echo "[$1]"
        grep -E "Total frames rendered|Janky frames|th percentile|Number Missed Vsync|Number Slow UI thread|Number Slow issue draw commands|Number Frame deadline missed|gpu percentile" "$OUT/gfx-$1.txt" | sed 's/^ *//'
    } >> "$STATS"
    log "frames $1 recorded"
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

# One pass through the app with the Now Playing style $1 and liquid glass $2 (TRUE/FALSE).
# $3 = "full" also visits Home and Library.
run_pass() {
    local style="$1" glass="$2" scope="$3"
    local tag
    tag=$(echo "$style-$glass" | tr '[:upper:]' '[:lower:]')
    revive
    note "== pass $tag"
    adb shell am force-stop "$PKG"
    # The log-in warning would otherwise sit over Home in every first screenshot.
    set_prefs "now_playing_style=$style" "liquid_glass=$glass" "should_show_log_in_required_alert=FALSE"
    adb logcat -c
    adb logcat -v threadtime > "$OUT/logcat-$tag.txt" 2>/dev/null &
    local logcat_pid=$!
    timeout 90 adb shell am start -W -n "$PKG/$ACTIVITY" > /dev/null
    sleep 25
    if [ "$scope" = "full" ]; then
        shot "home-$tag"
        open_link "simpmusic://library"
        sleep 8
        shot "library-$tag"
        reset_frames
        for _ in 1 2 3; do adb shell input swipe 540 1900 540 700 250; sleep 1; done
        shot "library-scrolled-$tag"
        for _ in 1 2 3; do adb shell input swipe 540 700 540 1900 250; sleep 1; done
        frames "library-scroll-$tag"
    fi

    open_link "simpmusic://watch?v=$SONG"
    sleep 25
    shot "playing-$tag"

    reset_frames
    open_link "simpmusic://player"
    sleep 4
    frames "np-open-$tag"
    if alive; then
        reset_frames
        sleep 12
        frames "np-idle-$tag"
        shot "nowplaying-$tag"
        adb shell input keyevent KEYCODE_BACK
        # The app behind the sheet holds still while it is open (LocalCoveredByNowPlaying): this
        # shows the mini player picking up again once the sheet has gone.
        sleep 4
        shot "closed-$tag"
    else
        note "[$tag] emulator went down while opening Now Playing"
    fi
    kill "$logcat_pid" > /dev/null 2>&1
    # What the app logged when something went wrong: Java crashes, native aborts, and HWUI or
    # RenderThread complaints (the emulator renders in software, so its GPU path is the weak spot).
    {
        echo "[logcat $tag: crashes and render errors]"
        grep -E "FATAL|AndroidRuntime|DEBUG   :|libc    :|Abort message|OpenGLRenderer|RenderThread|HWUI|SkiaVk|EGL_emulation" "$OUT/logcat-$tag.txt" | grep -E " E | F |FATAL|Abort" | tail -n 25
        echo "[logcat $tag: last lines]"
        tail -n 15 "$OUT/logcat-$tag.txt"
    } >> "$STATS"
}

log "waiting for the emulator"
wait_boot
adb shell wm size

log "installing $APK"
if ! adb install -r -g "$APK"; then
    echo "::error title=install::adb install failed"
    exit 1
fi

# Least likely to upset the software renderer first, so one failure does not hide the rest.
run_pass APPLE_MUSIC FALSE full
run_pass SPOTIFY FALSE short
run_pass APPLE_MUSIC_GLASS TRUE full

revive
post_check "ui-stats" "$STATS"
log "done"
cat "$STATS"
