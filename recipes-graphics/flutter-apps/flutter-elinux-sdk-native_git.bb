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
SRCREV = "${AUTOREV}"

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
