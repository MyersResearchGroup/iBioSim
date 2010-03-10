#!/bin/sh
scp $3.lpn $1:/tmp/.
ssh $1 "atacs $2 /tmp/$3.lpn"
scp $1:"atacs.log" .
scp $1:/tmp/$3.prg .
ssh $1 "rm /tmp/$3.prg"
