#!/bin/bash


setValues() {
    targethost=""

    while getopts ":vtp" Option
    do
        case $Option in
            v) targethost="vagrant@filarkiv";;
            t) targethost="kssuadmin@kssufilarkiv.usrv.ubergenkom.no";;
            v) targethost="kssuadmin@kssufilarkiv.srv.bergenkom.no";;
            ?) usage; exit 0;;
        esac
    done

    if [ $# -eq 0 ]
    then
        usage
        exit 1
    fi

}

usage() {
    echo "Usage: $0 [-vtp] "
    echo
    echo "  -v    upload to vagrant"
    echo "  -t    upload to test"
    echo "  -p    upload to prod"
}

setValues $@


cd "$( dirname "${BASH_SOURCE[0]}" )"

projectversion=$(git describe --abbrev=0)
revision=$(git describe --long | sed "s/.*-\(.*\)-.*/\1/")
mkdir -p target

cp  target/svarut-sak-import-${projectversion}.zip 

scp svarut-sak-import/target/svarut-sak-import-dist.zip ${targethost}:/nfs/svarut-sak-import/releases/svarut-sak-import-${projectversion}.zip
ssh ${targethost} "(cd /nfs/svarut-sak-import/releases && sudo ln -sf svarut-sak-import-${projectversion}.zip svarut-sak-import-latest.zip)"
