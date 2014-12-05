#!/bin/bash
cd "$( dirname "${BASH_SOURCE[0]}" )"
cd svarut-sak-import
mvn assembly:single
