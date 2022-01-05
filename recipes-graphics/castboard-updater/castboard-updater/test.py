import dbus

unit_name = 'sshd.service'

bus = dbus.SystemBus()
systemd = bus.get_object(
    'org.freedesktop.systemd1',
    '/org/freedesktop/systemd1'
)

manager = dbus.Interface(
    systemd,
    'org.freedesktop.systemd1.Manager'
)

unit_state = manager.GetUnitFileState(unit_name)

print(unit_state)