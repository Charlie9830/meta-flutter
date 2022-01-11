SUMMARY = "Castboard Showcaller"
DESCRIPTION = "Remote Web Admin interface for the Castboard software suite"
AUTHOR = "Charlie Hall"
HOMEPAGE = "https://github.com/charlie9830"
BUGTRACKER = "https://github.com/charlie9830"
SECTION = "graphics"
CVE_PRODUCT = ""

LICENSE = "CLOSED"

DEPENDS += "flutter-elinux-sdk-native unzip-native castboard-core"

SRC_URI = "git://github.com/Charlie9830/castboard_showcaller.git;protocol=https;rev=${CASTBOARD_SHOWCALLER_REV};lfs=0;branch=master;destsuffix=git"


S = "${WORKDIR}/git"

do_patch() {
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter-elinux/sdk/bin:$PATH
    export PUB_CACHE=${STAGING_DIR_NATIVE}/usr/share/flutter-elinux/sdk/.pub-cache
}

do_patch[depends] += "flutter-elinux-sdk-native:do_populate_sysroot"

do_configure() {
    #
    # Castboard Core
    #
    install -d ${WORKDIR}/castboard_core/
    cp -r ${STAGING_DATADIR}/castboard-core/* ${WORKDIR}/castboard_core/
}

do_compile() {
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter-elinux/sdk/bin:$PATH

    cd ${S}

    flutter-elinux build web --pwa-strategy=none

    # Extract the canvaskit url it wants to use.
    FLUTTER_CANVASKIT_URL="$( grep canvaskit-wasm ${S}/build/web/main.dart.js | sed -e 's|.*https|https|' -e 's|/bin.*|/bin/|' | uniq )"

    # Download the Canvaskit engine.
    curl -k -o ${S}/canvaskit.js "${FLUTTER_CANVASKIT_URL}/canvaskit.js"
    curl -k -o ${S}/canvaskit.wasm "${FLUTTER_CANVASKIT_URL}/canvaskit.wasm"

    # Install engine into build directory.
    mv ${S}/canvaskit.js ${S}/build/web/
    mv ${S}/canvaskit.wasm ${S}/build/web/

    # Manually change references to the engine URL in the build output.
    sed -i -e "s!${FLUTTER_CANVASKIT_URL}!/!g" ${S}/build/web/main.dart.js

}

do_install() {
    install -d ${D}${datadir}/${PN}/web/
    cp -r ${S}/build/web/* ${D}${datadir}/${PN}/web/ 
}

do_package_qa[noexec] = "1"
