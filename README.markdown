NOTICE
======

This project is obsolete and is unlikely to build/work on recent androids (4+
is not tested). Please see MagicTunnel instead:

http://www.magictunnel.net/download.php

I did not try it myself, but got a recommendation from a person which was fed
up trying to build AirportInternet and notified me.


Iodine front-end for Android
============================

What is this
------------

This is an [iodine][1] front-end for Android. Iodine lets you tunnel IPv4 data
through a DNS server. This can be usable in different situations where internet
access is firewalled, but DNS queries are allowed. Hence the name --
AirportInternet -- because in fact most "partially free" hotspots are in the
airports.

Features and principle of operation
-----------------------------------

Primary goal was to aggressively achieve stability and fault recovery. That
means if connection is closed by iodine or service is closed by Android,
AirportConnect attempts to recover and re-connect without user intervention.

Several configuration profiles are supported. User can set up different
profiles for different servers.

When connection is established, a service goes to background and is re-openable
using an Android notification. Connection status is seen real-time in the
notification.

Route configuration is *auto-magical*. When connected, iodine starts the
configuration script which configures traffic to go through iodine server. When
iodine socket is closed, routes are automatically released by kernel.\*

Requirements
------------

* tun.ko (available from "tun.ko installer")
* rooted phone (due to tun.ko and routing configuration)
* [slightly patched iodine][2] (necessary if you want to see console-like output).
  Iodine binary should be setuid-root, installation instructions below
* `routing.sh` (from this repository)

AirportConnect expects both `iodine` and `routing.sh` to be in
`/data/data/org.airportinternet/`. If paths are different, please adjust
constants in header of `ForkConnection.java`.

Design highlights
-----------------

`Connector.java` is the glue between the UI and `iodine` logic. It has these
abstract methods:

* `start(Setting setting)` (to start the connection)
* `stop` (to stop the connection)

And these callbacks:

* `sendLog(String)` (to send logging output from connector to the application)
* `connecting()` (notification that connection was initiated)
* `connected()` (notification that connection was successful)
* `disconnected()` (notification that connection was dropped)

`Connector.java` hides the complexity of managing UI states, notifications,
settings and makes it easy to write connector backends. `ForkConnector.java` is
the most straight-forward implementation that could possibly work. Like name
suggests, it forks-execs `iodine` C program with necessary parameters.

Having `Connector`, it will be relatively easy to add `NativeConnector`, which
would exploit `Android.net.VpnService` and communicate with the native Android
4 tun pseudo-device.

Current implementation is not intended to be for end-users due to too many
pre-requirements (rooted phone, tun.ko and setuid bit are only for developers
to ask). Due to upcoming better implementation, these probably will not be
changed (for example, we could work around the necessity of setuid bit,
however, it is better to put more effort to native implementation instead).

Possible problems
-----------------

For routing script invocation program uses `su`. I already noticed that `su`
invocations in android-x86 and in Nexus One differ dramatically. In android-x86
a shell script `routing.sh` with one parameter `127.16.0.0` must be executed
like this:

    $ su -c sh routing.sh 172.16.0.0

However, Nexus One expects everything that is after `-c` to be one argument:

    $ su -c "sh routing.sh 172.16.0.0"

This makes it necessary to implement various work-arounds and runtime checks of
`su` command. For now, I settled with Android-x86 version. However, users are
encouraged to find out the way it is done in their architecture and modify the
source accordingly.

This wasn't unexpected; shell script invocation from languages like C of Java
always been difficult. With native `Connector` implementation calling the shell
scripts will be just unnecessary.

Installation instructions
-------------------------

1. Build project: `android update project; ant release`.
2. Build iodine for android. `cd iodine; make cross-android-dist`
3. Install generated `AirportConnect.apk`
4. Copy files to the phone:

        $ adb push iodine/bin/armeabi/iodine /data/data/org.airportconnect/
        $ adb push routing.sh /data/data/org.airportconnect/
        $ adb shell
        # mount -o remount,suid /data # depending on phone, might be not necessary
        # chmod 4777 /data/data/org.airportconnect/iodine

Enjoy!

\* In some cases a static route to dns server via gateway might remain after
closing iodine. In 99.9% of the cases it doesn't change anything, since packets
to DNS server go through default gateway anyway.

Comments about C code
---------------------

As mentioned earlier, original `iodine` had to be patched. Here is the
motivation.

Warn, warnx, err, errx are BSD extensions to standard library which print
logging messages to standard error. They do not exist in Android, so had to be
re-implemented from scratch. In `iodine` implementation it used variadic
arguments (`va_start_`, `va_list`, etc). However, using these functions for
strings garbled the output. For that reason I reimplemented the functions above
using macros directly to `fprintf` and `exit` commands.

\newpage

Screenshots
-----------

![Settings window](report/settings.png)

![Error connecting](report/failure.png)

![Connected](report/connected.png)

![Notification](report/notification.png)

[1]: http://code.kryo.se/iodine/
[2]: https://github.com/Motiejus/iodine/commit/4601a23b31059290e30cae9996a1a833de9dbc3e
