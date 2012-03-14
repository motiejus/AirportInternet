#!/bin/sh

NS1=$(getprop net.dns1)
NS2=$(getprop net.dns2)
GW=$(ip ro | awk '/^default/{print $3}')

if [ "$1" != "indirect" ]; then
    ip route add $1 via $GW
fi

ip route add $NS1 via $GW
if [ -n '$NS2' ]; then
    ip route add $NS2 via $GW
fi

ip route add 0.0.0.0/1 dev dns0
ip route add 128.0.0.0/1 dev dns0

true
