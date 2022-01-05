DESCRIPTON = "Castboard Updater"
LICENSE = "CLOSED"
PR = "r0"

SRC_URI = " \ 
    file://update.py \
    file://castboard-updater.service \
    file://args.env \
    file://update_status \
    " 

S = "${WORKDIR}"

RDEPENDS_${PN} += "\
    python3 \
"

inherit systemd

SYSTEMD_PACKAGES = "${PN}"

SYSTEMD_SERVICE_${PN} = "castboard-updater.service"
SYSTEMD_AUTO_ENABLE_${PN} = "disable"

do_install () {
    # Install our update script. Install it with permissive permissions so that castboard can swap in a new script prior
    # to calling it if needed.
    install -d -m 777 ${D}${datadir}/castboard-updater/
    install -m 777 ${S}/update.py ${D}${datadir}/castboard-updater/

    # Install our environment configuration file. Castboard will write to this before calling update as a method to 
    # pass the required arguments.
    install -d -m 777 ${D}${sysconfdir}/castboard-updater/
    install -m 777 ${S}/args.env ${D}${sysconfdir}/castboard-updater/

    # Install our update_status file. We will write to this on successful update,
    # castboard will read it to determine if it has just been updated and reset the value
    # if it has been.
    install -m 777 ${S}/update_status ${D}${sysconfdir}/castboard-updater/

    # Install our main Unit File.
    install -d ${D}${systemd_unitdir}/system/
    install -m 0644 ${S}/castboard-updater.service ${D}${systemd_unitdir}/system
}

FILES_${PN} += "${datadir}/castboard-updater/ \
                ${systemd_system_unitdir} \
                ${sysconfdir}/castboard-updater/args.env \
"

CONFFILES_${PN} = " \
    ${sysconfdir}/castboard-updater/args.env \
"

