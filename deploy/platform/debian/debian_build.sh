#!/bin/bash
#
# platform/debian/build.sh
#
# Invoked by mdsplus/deploy/build.sh for debian platforms.
#
# Run docker image to build mdsplus
#
set -e
volume() {
    if [ ! -z "$1" ]
    then
	echo "-v $(realpath ${1}):${2}"
    fi
}

if [ "${RELEASE}" = "yes" ]
then
    mkdir -p ${RELEASEDIR}/${BRANCH}
    rm -Rf ${RELEASEDIR}/${BRANCH}/*
else
    echo RELEASEDIR=""
fi

if [ "${PUBLISH}" = "yes" ]
then
    mkdir -p ${PUBLISHDIR}
fi
set +e

spacedelim() {
    if [ ! -z "$1" ]
    then
	IFS=',' read -ra ARR <<< "$1"
	ans="${ARR[*]}"
    fi
    echo $ans
}

exitstatus=0
images=(${DOCKERIMAGE})
arches=($(spacedelim ${ARCH}))
idx=0
while [[ $idx -lt ${#images[*]} ]]
do
    image=${images[$idx]}
    arch=${arches[$idx]}
    echo "Building installers for ${arch} using ${image}"
    #
    # If there are both 32-bit and 64-bit packages for the platform
    # only build the deb's after both 32-bit and 64-bit builds are
    # complete. Likewise only publish the release once.
    #
    docker run -a stdout -a stderr --cidfile=${WORKSPACE}/${OS}_docker-cid \
       -u $(id -u):$(id -g) \
       -e "ARCH=${arch}" \
       -e "ARCHES=${ARCH}" \
       -e "BRANCH=$BRANCH" \
       -e "DISTNAME=$DISTNAME" \
       -e "OS=$OS" \
       -e "VERSION=$VERSION"  \
       -e "TEST=$TEST" \
       -e "TESTFORMAT=$TEST_FORMAT" \
       -e "mdsevent_port=$EVENT_PORT" \
       -e "RELEASE=$RELEASE" \
       -e "PUBLISH=$PUBLISH" \
       -e "SANITIZE=$SANITIZE" \
       -e "VALGRIND_TOOLS=$VALGRIND_TOOLS" \
       -e "UPDATEPKG=$UPDATEPKG" \
       -e "PLATFORM=$PLATFORM" \
       -v $(realpath ${SRCDIR}):/source \
       -v ${WORKSPACE}:/workspace \
       $(volume ${RELEASEDIR} /release) \
       $(volume ${PUBLISHDIR} /publish) \
       $(volume "$KEYS" /sign_keys) \
       ${image} /source/deploy/platform/${PLATFORM}/${PLATFORM}_docker_build.sh
    status=$?
    if [ -r ${WORKSPACE}/${OS}_docker-cid ]
    then
	sleep 3
	docker rm $(cat ${WORKSPACE}/${OS}_docker-cid)
	rm -f ${WORKSPACE}/${OS}_docker-cid
    fi
    if [ ! "$status" = "0" ]
    then
	echo "Docker ${PLATFORM}_docker_build.sh returned failure status from ${image}."
	exitstatus=$status
    fi
    let idx=idx+1
done
exit $exitstatus
