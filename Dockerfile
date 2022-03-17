FROM ubuntu:18.04

LABEL base_image="ubuntu:18.04"
LABEL version="1.0.0"
LABEL software="bioSimAPI"
LABEL software.version="1.0.0"
LABEL about.summary="API for SynBioHub/SBOLCanvas communication with iBioSim"
LABEL about.home="https://github.com/MyersResearchGroup/iBioSim"
LABEL about.documentation="https://github.com/MyersResearchGroup/iBioSim"
LABEL about.license_file="https://github.com/MyersResearchGroup/iBioSim/blob/master/LICENSE.txt"
LABEL about.license="Apache-2.0"
LABEL about.tags="kinetic modeling,dynamical simulation,systems biology,biochemical networks,SBML,SED-ML,COMBINE,OMEX,BioSimulators"
LABEL maintainer="Chris Myers <chris.myers@colorado.edu>"

# Install requirements
RUN apt-get update --fix-missing \
	&& DEBIAN_FRONTEND=noninteractive \
	&& apt-get install -y maven \
	&& apt-get install python3.7 -y \
	&& apt-get install python3-pip -y \
	&& apt install openjdk-8-jdk -y 

# Build iBioSim
WORKDIR /iBioSim
COPY . .
RUN mvn package -Dmaven.javadoc.skip=true
WORKDIR /

RUN apt-get -y install build-essential \
	&& apt-get -y install dos2unix \
	&& apt-get -y install libxml2-dev

# Build reb2sac
WORKDIR iBioSim/Dependencies
RUN chmod +x newbuild.sh \
	&& dos2unix newbuild.sh \
	&& sh newbuild.sh
WORKDIR /

ENV LC_ALL=C.UTF-8
ENV LANG=C.UTF-8
