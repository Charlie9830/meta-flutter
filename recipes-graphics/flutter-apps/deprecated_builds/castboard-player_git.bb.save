SUMMARY = "Castboard Player"
DESCRIPTION = "Player for the Castboard software suite"
AUTHOR = "Charlie Hall"
HOMEPAGE = "https://github.com/charlie9830"
BUGTRACKER = "https://github.com/charlie9830"
SECTION = "graphics"
CVE_PRODUCT = ""

LICENSE = "CLOSED"

DEPENDS += "flutter-engine flutter-sdk-native unzip-native castboard-core castboard-remote"

# Castboard Remote Repository Revision
CB_PLAYER_REV = "61ef2ed2d9a3ff0656e5cf3e39552da57d31bc13"

SRC_URI = "git://github.com/Charlie9830/castboard_player.git;protocol=https;rev=${CB_PLAYER_REV};branch=master;destsuffix=git"

S = "${WORKDIR}/git"

do_patch() {
    export CURL_CA_BUNDLE=${STAGING_DIR_NATIVE}/etc/ssl/certs/ca-certificates.crt
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/bin:$PATH
    export PUB_CACHE=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/.pub-cache

    FLUTTER_VER="$( flutter --version | head -n 1 | awk '{print $2}' )"
    echo "Flutter Version: ${FLUTTER_VER}"
}

do_patch[depends] += "flutter-sdk-native:do_populate_sysroot"

do_configure() {
    #
    # Engine SDK
    #
    rm -rf ${S}/engine_sdk
    unzip ${STAGING_DATADIR}/flutter/engine_sdk.zip -d ${S}/engine_sdk


    #
    # Castboard Core
    #
    install -d ${WORKDIR}/castboard_core/
    cp -r ${STAGING_DATADIR}/castboard-core/* ${WORKDIR}/castboard_core/

    #
    # Castboard Remote
    #
    # We only create the web_app folder now. We don't populate it yet. This is because the castboard-player pubspec.yaml only includes a listing for the web_app folder. Not all of the specific items inside it.
    # If we include it now. When the app is compiled, it will ignore the directories inside the web_app folder because it doesn't know about them. Therefore we create a blank web_app directory now, so that 
    # flutter won't complain that it can't find the asset. Then we will populate it in the install step.
    install -d ${S}/assets/web_app/
}

do_compile() {
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter/sdk/bin:$PATH

    ENGINE_SDK=${S}/engine_sdk/sdk

    cd ${S}

    flutter build bundle

    dart ${ENGINE_SDK}/frontend_server.dart.snapshot --aot --tfa --target=flutter --sdk-root ${ENGINE_SDK} --output-dill app.dill lib/main.dart 
    ${ENGINE_SDK}/clang_x64/gen_snapshot --deterministic --snapshot_kind=app-aot-elf --elf=app.so --strip app.dill
}

do_install() {

    #
    # Sony Layout
    #
    #install -d ${D}${datadir}/${PN}/sony
    #
    #install -d ${D}${datadir}/${PN}/sony/lib
    #install -m 644 ${S}/libapp.so ${D}${datadir}/${PN}/sony/lib
    #
    #install -d ${D}${datadir}/${PN}/sony/data
    #install -m 644 ${STAGING_DATADIR}/flutter/icudtl.dat ${D}${datadir}/${PN}/sony/data/
    #
    #install -d ${D}${datadir}/${PN}/sony/data/flutter_assets
    #cp -rTv ${S}/build/flutter_assets/. ${D}${datadir}/${PN}/sony/data/flutter_assets/


    #
    # Flutter-Pi Layout
    #
    install -d ${D}${datadir}/${PN}/
    
    install -d ${D}${datadir}/${PN}/flutter_assets/
    install -m 644 ${S}/app.so ${D}${datadir}/${PN}/flutter_assets/
    
    install -m 644 ${STAGING_DATADIR}/flutter/icudtl.dat ${D}${datadir}/${PN}/
    
    cp -r ${S}/build/flutter_assets/* ${D}${datadir}/${PN}/flutter_assets/

    # Install the web_app Assets.
    cp -r ${STAGING_DATADIR}/castboard-remote/web/* ${D}${datadir}/${PN}/flutter_assets/assets/web_app/
}

FILES_${PN} = "${datadir}/${PN}/*"

do_package_qa[noexec] = "1"
