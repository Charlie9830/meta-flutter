import sys
import dbus
import subprocess
import os.path
from os import listdir
import time

# Script prerequisites
# > appPath, updateSourcePath, updaterConfPath, unitName, rollbackPath must all be provided
# > updateSourcePath must exist and contain the unpacked application ie: data, bundle.
# > updaterConfPath/update_status must exist.

# Return Codes
# 2: Update Path does not exist or is not a directory.
# 3: Moving the current app to the recovery path failed with an exception.
# 4: Copying from the update directory to the app directory failed with an exception.
# 5: The updateSourcePath directory is empty.
# 6: The appPath does not exist 

# update_status file breadcrumb possible values
# none
# started
# failed
# success

appPath = sys.argv[1]
updateSourcePath = sys.argv[2]
updaterConfPath = sys.argv[3]
unitName = sys.argv[4]
rollbackDirPath = sys.argv[5]
outgoingCodename = sys.argv[6]
incomingCodename = sys.argv[7]

def startApp(manager, unitName):
    manager.StartUnit(dbus.String(unitName), dbus.String("replace")) 
    
def stopApp(manager, unitName):
    manager.StopUnit(dbus.String(unitName), dbus.String("ignore-dependencies"))

def failWithExitCode(code):
    print('An error occured. Exiting with code ' + str(code))
    sys.exit(code)

# Leaves a breadcrumb in the updaterConfPath/update_status file.
def leaveBreadcrumb(confPath, crumb):
    with open(os.path.join(confPath, "update_status"), "w+") as file:
        file.write(crumb)


print('Starting Castboard Update..')

print("appPath: " + appPath)
print("updateSourcePath: " + updateSourcePath)
print("updaterConfPath: " + updaterConfPath)
print("unitName: " + unitName)
print("rollbackDirPath: " + rollbackDirPath)
print("outgoingCodename: " + outgoingCodename)
print("incomingCodename: " + incomingCodename)

print('Waiting for Castboard to finish notifications..')
time.sleep(5)

print("Updating from version " + outgoingCodename + " to version " + incomingCodename)

# Check that we have everything we need.
print("Checking arguments...")

# Check that the updateSourcePath exists.
if not os.path.isdir(updateSourcePath):
    failWithExitCode(2)

# Check that the updateSourcePath actually has stuff in it.
if len(listdir(updateSourcePath)) == 0:
    failWithExitCode(5) 

# Check that the appPath exists.
if not os.path.isdir(appPath):
    failWithExitCode(6)

# Ensure the root rollback directory exists.
if not os.path.isdir(rollbackDirPath):
    subprocess.call("mkdir " + rollbackDirPath, shell=True)

# Ensure a directory for our outgoing codename exists.
if not os.path.isdir(os.path.join(rollbackDirPath, outgoingCodename)):
    subprocess.call("mkdir " + os.path.join(rollbackDirPath, outgoingCodename), shell=True)

# Ensure our rollback directory is empty.
subprocess.call("rm -rf " + os.path.join(rollbackDirPath, "*"), shell=True)

# Setup dbus interfaces to systemd
print("Setting up dbus connection to systemd...")
bus = dbus.SystemBus()
systemd = bus.get_object(
    'org.freedesktop.systemd1',
    '/org/freedesktop/systemd1'
)
manager = dbus.Interface(
    systemd,
    'org.freedesktop.systemd1.Manager'
)

# Leave a 'started' breadcrumb
print("Leaving a 'started' breadcrumb...")
leaveBreadcrumb(updaterConfPath, 'started')

# Shutdown the instance of castboard.
print("Shutting down any running instances of Castboard...")
stopApp(manager, unitName)

# Wait for shutdown to complete.
print('Waiting for running instance of Castboard to shutdown...')
time.sleep(5)

print('\n \n \n')
print("Oh dear, you have caught me in a state of undress! Avert your eyes this won't take long...")
time.sleep(5)
print('Any minute now...')
time.sleep(3)

# Move the contents of the App path to the recovery path.
try:
    print('Moving ' + appPath + ' to ' + os.path.join(rollbackDirPath))
    subprocess.call("mv -f " + os.path.join(appPath)+"*" + " " +rollbackDirPath, shell=True)
except subprocess.SubprocessError as err:
    # Something failed write it to the output, leave a breadcrumb and try to recover.
    leaveBreadcrumb(updaterConfPath, 'failed')
    print(f"Unexpected {err=}, {type(err)=}")
    startApp(manager, unitName)
    failWithExitCode(3)
try:
    # Copy the contents updatePath to the appPath. We can't copy the directory itself as we likely don't have
    # permissions to modify the /usr/share/ parent directory
    print('Copying ' + updateSourcePath + " to " + appPath)
    subprocess.call("cp -rf " + os.path.join(updateSourcePath)+"/*" + " " + appPath, shell=True)

    # Ensure the contents of the has the correct permissions.
    subprocess.call("chmod -R 777 " + os.path.join(appPath)+"/*", shell=True)
except subprocess.SubprocessError as err:
    # Something failed write it to the output, leave a breadcrumb and try to recover.
    leaveBreadcrumb(updaterConfPath, 'failed')
    print(f"Unexpected {err=}, {type(err)=}")
    startApp(manager, unitName)
    failWithExitCode(4)

# Success. Leave a breadcrumb and restart the app.
print('Leaving success breadcrumb')
leaveBreadcrumb(updaterConfPath, 'success')

print('All done!')
print('Starting up Castboard again')
startApp(manager, unitName)
