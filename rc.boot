#! /bin/sh
# shellcheck disable=1090,1091
. /etc/rc.lib
echo "Starting pine..."
echo "   ___  _         ";
echo "  / _ \(_)__  ___ ";
echo " / ___/ / _ \/ -_)";
echo "/_/  /_/_//_/\__/ ";
echo "                  ";
echo "Mounting /proc /sys /and /the /gang"
mnt nosuid,noexec,nodev    proc     proc /proc
mnt nosuid,noexec,nodev    sysfs    sys  /sys
mnt mode=0755,nosuid,nodev tmpfs    run  /run
mnt mode=0755,nosuid       devtmpfs dev  /dev
mkdir -p /run/runit /run/user /run/lock \
   /run/log   /dev/pts  /dev/shm

mnt mode=0620,gid=5,nosuid,noexec devpts devpts /dev/pts
mnt mode=1777,nosuid,nodev        tmpfs  shm    /dev/shm

# udev created these for us, however other device managers
# don't. This is fine even when udev is in use.
{
   ln -s /proc/self/fd /dev/fd
   ln -s fd/0          /dev/stdin
   ln -s fd/1          /dev/stdout
   ln -s fd/2          /dev/stderr
} 2>/dev/null
if command -v smdev >/dev/null; then
   echo "Guess i'll start smdev..."
	smdev -s
else
   echo "no supported device node thing installed - hope you like ln commands"
fi

# this nice little bit from sabotage linux, thx guys :] 
# make /dev/root symlink in case kernel root bootparam was set
echo "YAY MOUNTPOINTS!!"


test -e /dev/root || {
	dv=$(sed -n 's,.*root=\(/dev/[sh]d[a-z][0-9]\).*,\1,p' < /proc/cmdline)
	test -n "$dv" || dv="$(sed -n 's,.*root=\(/dev/mapper/[_A-Za-z0-9]*\).*,\1,p' < /proc/cmdline)"
	test -n "$dv" || dv=$(findfs "$(sed -n 's,.*root=\(UUID=[-A-Za-z0-9]*\).*,\1,p' < /proc/cmdline)" 2>/dev/null)
	test -n "$dv" || dv=$(findfs "$(sed -n 's,.*root=\(LABEL=[-_A-Za-z0-9]*\).*,\1,p' < /proc/cmdline)" 2>/dev/null)
	test -n "$dv" && test -e "$dv" && ln -s "$dv" /dev/root
}


rw=true
rwtest=/tmp/rwtest.tmp
if touch "$rwtest" 2>/dev/null; then
	rm "$rwtest"
else
	rw=false
fi

$rw && mount -o remount,ro /
fsck -A -T -C -p
mkdir -p /dev/shm /dev/pts
$rw && mount -o remount,rw /

swapon -a

mount -a # mount stuff from /etc/fstab

echo "Networking!"
hostname "$(cat /etc/hostname)"
ifconfig lo up
echo "Hope you like the login screen"
respawn getty /dev/tty1 vt100 &
