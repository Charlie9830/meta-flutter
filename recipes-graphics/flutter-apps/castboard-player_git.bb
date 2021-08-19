SUMMARY = "Castboard Player"
DESCRIPTION = "Player for the Castboard software suite built on flutter-elinux"
AUTHOR = "Charlie Hall"
HOMEPAGE = "https://github.com/charlie9830"
BUGTRACKER = "https://github.com/charlie9830"
SECTION = "graphics"
CVE_PRODUCT = ""

LICENSE = "CLOSED"

DEPENDS += "\
	flutter-elinux-sdk-native \
	unzip-native \
	castboard-core \
	castboard-remote\
	glib-2.0 \
	libinput \
	libxkbcommon \
	libdrm \
	virtual/libgles2 \
	virtual/egl \
	virtual/mesa \
	udev \
	systemd \
	"
	
# Likely temporary work around until offical DRM/GBM support comes online upstream from flutter-elinux.
RDEPENDS_${PN} += "\
	wayland-protocols \
	xkeyboard-config \
	"

# Castboard Remote Repository Revision
CB_PLAYER_REV = "61ef2ed2d9a3ff0656e5cf3e39552da57d31bc13"

SRC_URI = "git://github.com/Charlie9830/castboard_player.git;protocol=https;rev=${CB_PLAYER_REV};branch=master;destsuffix=git"

S = "${WORKDIR}/git"

TOOLCHAIN = "clang"

inherit cmake

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

    #
    # Castboard Remote
    #
    # We only create the web_app folder now. We don't populate it yet. This is because the castboard-player pubspec.yaml only includes a listing for the web_app folder. Not all of the specific items inside it.
    # If we include it now. When the app is compiled, it will ignore the directories inside the web_app folder because it doesn't know about them. Therefore we create a blank web_app directory now, so that 
    # flutter won't complain that it can't find the asset. Then we will populate it in the install step.
    install -d ${S}/assets/web_app/
}

do_compile() {
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter-elinux/sdk/bin:$PATH

    cd ${S}
    
    flutter-elinux create .
    flutter-elinux pub get
    flutter-elinux build elinux --target-arch=arm64 --target-sysroot=${STAGING_DIR_TARGET} --target-backend-type=gbm
}

do_install() {
    #
    # Flutter-elinux Layout
    #
    install -d ${D}${datadir}/${PN}/
    cp -rTv ${S}/build/elinux/arm64/release/bundle/. ${D}${datadir}/${PN}/

    # Rename exectuable to something sane because flutter-elinux uses the parent dir name, which in the case of this
    # recipe is "git"
    mv ${D}${datadir}/${PN}/git ${D}${datadir}/${PN}/player

    # Install the web_app Assets.
    cp -r ${STAGING_DATADIR}/castboard-remote/web/* ${D}${datadir}/${PN}/data/flutter_assets/assets/web_app/
}

FILES_${PN} = "${datadir}/${PN}/*"

do_package_qa[noexec] = "1"
