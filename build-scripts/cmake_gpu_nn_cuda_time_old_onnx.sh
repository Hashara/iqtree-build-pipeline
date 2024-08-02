#!bin/bash


### pre steps #####

module load openmpi/4.1.5 boost/1.84.0 eigen/3.3.7 llvm/17.0.1 cuda/11.4.1 cudnn/8.2.2-cuda11.4

export OMPI_CC=clang
export OMPI_CXX=clang++


export CC=clang 
export CXX=clang++


export LDFLAGS="-L/apps/llvm/17.0.1/lib"
export CPPFLAGS="-I/apps/llvm/17.0.1/lib/clang/17/include"


############

onnx_dir="onnxruntime-linux-x64-gpu-1.12.1"
cuda_dir="/apps/cuda/11.4.1"


cmake -DCMAKE_CXX_FLAGS="$LDFLAGS $CPPFLAGS" \
-DCMAKE_C_COMPILER=mpicc -DCMAKE_CXX_COMPILER=mpicxx -DEIGEN3_INCLUDE_DIR=/apps/eigen/3.3.7/include/eigen3 \
-Donnxruntime_INCLUDE_DIRS="/scratch/dx61/sa0557/iqtree2/${onnx_dir}/include" -Donnxruntime_LIBRARIES="/scratch/dx61/sa0557/iqtree2/${onnx_dir}/lib/libonnxruntime.so" \
-Dcuda_INCLUDE_DIRS="${cuda_dir}/include" -Dcuda_LIBRARIES="${cuda_dir}/lib64" \
-DUSE_OLD_NN=ON \
-DUSE_CUDA=ON ..
make -j
