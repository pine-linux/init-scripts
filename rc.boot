#! /bin/sh
. rc.lib
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
   smdev -df & mdev_pid=$!
else
   echo no supported mapper installed - hope you like ln commands 
fi

