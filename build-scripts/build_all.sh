#!bin/bash

cd build-mpi
bash ../../build-scripts/build.sh

cd ../build-wompi
bash ../../build-scripts/build.sh

cd ../build-nn
bash ../../build-scripts/build.sh
