#!/bin/bash

BUILD_DIR=$PWD
ROOT=/iBioSim

tar -xf libSBML-5.15.2-core-plus-packages-src.tar.gz
tar -xf gsl-latest.tar.gz
tar -xf GeneNet.tar.gz
tar -xf reb2sac.tar.gz

# Install gsl
cd ${BUILD_DIR}/gsl-2.4
./configure --prefix=${ROOT} --quiet
make --quiet
make install --quiet

# Install libsbml
cd ${BUILD_DIR}/libSBML-5.15.2-Source
./configure --prefix=${ROOT} --with-java --enable-comp --quiet
make --quiet; make --quiet
make install --quiet

# Install GeneNet
cd ${BUILD_DIR}/GeneNet
make --quiet
cp bin/GeneNet ${ROOT}/bin/GeneNet.linux64
cp ${ROOT}/bin/GeneNet.linux64 ${ROOT}/bin/GeneNet


# Install reb2sac
cd ${BUILD_DIR}/reb2sac
CFLAGS="-I${ROOT}/include -O0 -g3 -DNAME_FOR_ID" LDFLAGS="-L${ROOT}/lib -lm" ./configure --prefix=${ROOT}/ && \
    env WANT_AUTOCONF_2_5="1" WANT_AUTOMAKE_1_6="1" make -k -j1 install
cp ${ROOT}/bin/reb2sac ${ROOT}/bin/reb2sac.linux64
