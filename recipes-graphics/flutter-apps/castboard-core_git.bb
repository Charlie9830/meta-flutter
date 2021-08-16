SUMMARY = "Castboard Core"
DESCRIPTION = "Core Dart Libraries for the Castboard software suite"
AUTHOR = "Charlie Hall"
HOMEPAGE = "https://github.com/charlie9830"
BUGTRACKER = "https://github.com/charlie9830"
SECTION = "graphics"
CVE_PRODUCT = ""

LICENSE = "CLOSED"

DEPENDS += "flutter-engine flutter-sdk-native unzip-native"

# Castboard Repository Revisions
CB_CORE_REV = "cfa7432742205c4f672981b6fb18a296c5e36053"

SRC_URI = "git://github.com/Charlie9830/castboard_core.git;protocol=https;lfs=0;branch=master;rev=${CB_CORE_REV};destsuffix=git"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${datadir}/${PN}/
    
    cp -rTv ${S}/. ${D}${datadir}/${PN}/
}

do_package_qa[noexec] = "1"
