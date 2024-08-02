This pipeline will build executable for the iqtree2

following directory will be created inside the ci-cd/build directory

1. build-mpi --> build the mpi version of iqtree2
2. build-wompi --> build the non-mpi + openmp version of iqtree2
3. build-nn --> build the non-mpi + openmp + NN version of iqtree2
4. build-nn-mpi --> build the mpi + NN version of iqtree2
4. build-gpu-nn --> build the non-mpi (openmp) + openmp + NN + GPU version of iqtree2
6. build-gpu-nn-mpi --> build the mpi + NN + GPU version of iqtree2