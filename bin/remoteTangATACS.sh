#!/bin/sh
scp $2.lpn tang.ece.utah.edu:/tmp/.
ssh tang.ece.utah.edu "/home/tang/myers/BioSim/bin/atacs $1 /tmp/$2.lpn"
scp tang.ece.utah.edu:"atacs.log" .
scp tang.ece.utah.edu:/tmp/$2.prg .
ssh tang.ece.utah.edu "rm /tmp/$2.prg"
