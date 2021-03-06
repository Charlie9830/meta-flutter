SUMMARY = "Castboard Performer"
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
	castboard-showcaller\
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
	
DEPENDS += "\
	wayland-protocols \
	wayland-native \
"

RDEPENDS_${PN} += "\
    xkeyboard-config \
    fontconfig \
    liberation-fonts \
    python3 \
"

CASTBOARD_PERFORMER_BRANCH ?= "master"
SRC_URI = "git://github.com/Charlie9830/castboard_performer.git;protocol=https;rev=${CASTBOARD_PERFORMER_REV};branch=${CASTBOARD_PERFORMER_BRANCH};destsuffix=git \
        file://cage@.service \
        file://cage \
        file://default.target \
        file://launch_castboard.py \
        file://castboard.conf \
		"

S = "${WORKDIR}"

TOOLCHAIN = "clang"

inherit cmake update-rc.d systemd useradd

SYSTEMD_PACKAGES = "${PN}"
INITSCRIPT_PACKAGES = "${PN}"
SYSTEMD_SERVICE_${PN} = "cage@.service"

# Create the 'cage' System user.
USERADD_PACKAGES = "${PN}"
USERADD_PARAM_${PN} = "--system -d /home/cage cage "

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
    # Castboard Showcaller
    #
    # We only create the web_app folder now. We don't populate it yet. This is because the castboard-performer pubspec.yaml only
    # includes a listing for the web_app folder. Not all of the specific items inside it.
    # If we all the subfiles and subdirectories of web_app now, when the app is compiled, it will ignore the directories
    # inside the web_app folder because it doesn't know about them. Therefore we create a blank web_app directory now, so that 
    # flutter won't complain that it can't find the asset.
    install -d ${S}/git/assets/web_app/
    
    # We then add in a temporary file to the web_app folder, this is because flutter will exclude blank directories from the
    # final build bundle.
    touch ${S}/git/assets/web_app/hold
}

do_compile() {
    export PATH=${STAGING_DIR_NATIVE}/usr/share/flutter-elinux/sdk/bin:$PATH

    cd ${S}/git
    
    rm -rf ./elinux
    flutter-elinux clean
    
    flutter-elinux create .
    flutter-elinux pub get
    
    # DRM Build
    # Using the DRM Build is still a bit painful right now. Unable to get it to start correctly on boot.
    # Only thing that works currently is setting a 20 second sleep in ExecStartPre which is pretty fragile.
    #flutter-elinux build elinux --target-arch=arm64 --target-sysroot=${STAGING_DIR_TARGET} --target-backend-type=gbm
    
    # Wayland Client Build
    flutter-elinux build elinux \
    --dart-define=ELINUX_IS_ELINUX=true \
    --dart-define=ELINUX_TMP_PATH=/tmp/ \
    --dart-define=ELINUX_HOME_PATH=/home/cage \
    --target-arch=arm64 \
    --target-sysroot=${STAGING_DIR_TARGET}
}

do_install() {
    #
    # Cage-Autorun
    #
    # Install our main Unit File.
    install -d ${D}${systemd_unitdir}/system/
    install -m 0644 ${S}/cage@.service ${D}${systemd_unitdir}/system

    # Install our PAM conf file.
    install -d ${D}${sysconfdir}/pam.d/
    install -m 0644 ${S}/cage ${D}${sysconfdir}/pam.d/

    # Install the launch_castboard python script
    install -d ${D}${bindir}
    install -m 754 -o cage -g cage ${S}/launch_castboard.py ${D}${bindir}/
    

    # Install the Application Configuration file and ensure the cage user owns it.
    install -d ${D}${sysconfdir}/castboard/
    install -m 666 -o cage -g cage ${WORKDIR}/castboard.conf ${D}${sysconfdir}/castboard/

    # Install a home directory for the cage user. Castboard will want to put it's logs and data files here.
    install -d ${D}/home/cage
    chown -R cage:cage ${D}/home/cage
    
    #
    # Castboard Performer
    #

    #
    # Flutter-elinux Layout
    #
    #     !!!  IMPORTANT   !!!
    # If you change this layout, ensure you also modify the update validation methods in Castboard. It validates
    # using the directory schema (bundle, data, executable) and executable name.
    install -d ${D}${datadir}/${PN}/
    cp -rTv ${S}/git/build/elinux/arm64/release/bundle/. ${D}${datadir}/${PN}/
    chmod -R 775 ${D}${datadir}/${PN}/*

    # Extract the versionCodename from the sourcecode.
    CASTBOARD_BUILD_CODENAME=$(grep -P -o "(?<=kVersionCodename = [\"|\'])[a-zA-Z]+" ${S}/git/lib/versionCodename.dart)

    # Write the code to the /codename
    echo $CASTBOARD_BUILD_CODENAME > ${D}${datadir}/${PN}/codename
    chmod 775 ${D}${datadir}/${PN}/codename

    # Rename exectuable.
    #
    #     !!!  IMPORTANT   !!!
    #
    # If you change the executable name, ensure you also update the validation methods in Castboard.
    # It validates using the directory schema (bundle, data, executable) and executable name.
    mv ${D}${datadir}/${PN}/git ${D}${datadir}/${PN}/performer

    # Install the web_app Assets.
    cp -r ${STAGING_DATADIR}/castboard-showcaller/web/* ${D}${datadir}/${PN}/data/flutter_assets/assets/web_app/
    chmod -R 775 ${D}${datadir}/${PN}/data/flutter_assets/assets/web_app/*

    # Remove the holding file we installed earlier.
    rm ${D}${datadir}/${PN}/data/flutter_assets/assets/web_app/hold

    # Ensure the Cage user is the owner of all installed files and directories. Although we set liberal file mode,
    # only the owner can do things such as change the Modified time of a file to specific timestamp. We do that
    # in order to adjust the File modified times of the web_app directory.
    chown cage:cage -R ${D}${datadir}/${PN}
}

FILES_${PN} = " \
	${datadir}/${PN}/* \
    ${libexecdir} \
    ${systemd_system_unitdir} \
    /home/cage \
    ${bindir}/launch_castboard.py \
    ${sysconfdir}/castboard/castboard.conf \
    ${sysconfdir}/pam.d/cage \
    ${sysconfdir}/castboard/castboard.conf \
"

CONFFILES_${PN} = " \
    ${sysconfdir}/pam.d/cage \
    ${sysconfdir}/castboard/castboard.conf \
"

INSANE_SKIP_${PN}_append = "already-stripped"


do_package_qa[noexec] = "1"
