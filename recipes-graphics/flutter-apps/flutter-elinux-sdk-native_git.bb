SUMMARY = "Flutter for Embedded Linux"
DESCRIPTION = "Sony flavour of the Flutter tools geared for Embedded Linux development"
AUTHOR = "Sony"
BUGTRACKER = "https://github.com/sony/flutter-elinux/issues"
SECTION = "graphics"
CVE_PRODUCT = ""

LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://LICENSE;md5=647d87ee0850d0ee715550d324dd447c"

DEPENDS += "curl-native unzip-native"

SRC_URI = "git://github.com/sony/flutter-elinux.git;branch=main \ 
	   file://ca-certificates.crt"

#
# Version Note
#
# We have to stay on 2.5.3 until the following issue is resolved.
# -	https://github.com/sony/flutter-elinux/issues/70

# Latest
#SRCREV = "${AUTOREV}"

# Version 2.8.0
#SRCREV = "0e4107e829eced475395dc4e11e4a76b49c5f30e"

# Version 2.5.3
SRCREV = "fad1d30ae7accef627a98df7d201a59a649f5c2a"

S = "${WORKDIR}/git"

inherit native

do_compile() {
    export CURL_CA_BUNDLE=${WORKDIR}/ca-certificates.crt

    export PATH=${S}/bin:$PATH
    export PUB_CACHE=${S}/.pub-cache

    flutter-elinux doctor
}

do_install() {
    install -d ${D}${datadir}/flutter-elinux/sdk
    cp -rTv ${S}/. ${D}${datadir}/flutter-elinux/sdk
}

FILES_${PN}-dev = "${datadir}/flutter-elinux/sdk/*"



INSANE_SKIP_${PN}-dev = "already-stripped"

BBCLASSEXTEND = "native nativesdk"

# vim:set ts=4 sw=4 sts=4 expandtab:
