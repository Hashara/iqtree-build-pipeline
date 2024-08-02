#!bin/bash


### pre steps #####

module load openmpi/4.1.5 boost/1.84.0 eigen/3.3.7 llvm/17.0.1

export OMPI_CC=clang
export OMPI_CXX=clang++


export CC=clang 
export CXX=clang++


export LDFLAGS="-L/apps/llvm/17.0.1/lib"
export CPPFLAGS="-I/apps/llvm/17.0.1/lib/clang/17/include"


############

cmake -DCMAKE_CXX_FLAGS="$LDFLAGS $CPPFLAGS" \
-DCMAKE_C_COMPILER=mpicc -DCMAKE_CXX_COMPILER=mpicxx -DEIGEN3_INCLUDE_DIR=/apps/eigen/3.3.7/include/eigen3 \
-Donnxruntime_INCLUDE_DIRS="/scratch/dx61/sa0557/iqtree2/onnxruntime-linux-x64-1.17.3/include" -Donnxruntime_LIBRARIES="/scratch/dx61/sa0557/iqtree2/onnxruntime-linux-x64-1.17.3/lib/libonnxruntime.so" \
-DUSE_NN=ON ..
make -j
