import re
import subprocess
import sys

# Reads the Application Configuration (/etc/castboard/castboard.conf) to determine the value of the "deviceRotation" property.
# Then prepares the correct args to pass to Cage.
# Finally it runs cage with those arguments (if any) and points it to Castboard.

# Command Input arguments
performerLocation = sys.argv[1] # TODO: You never actually use this.
confLocation = sys.argv[2]

def extractValue(input):
    return input.split("=")[1].strip() 

file = open(confLocation, 'r')
Lines = file.readlines()
 
cageArgs = ""

for line in Lines:
    deviceRotationMatch = re.search('deviceRotation=\d', line)
    if deviceRotationMatch != None:
        # Found the matching line. Extra the value and set cageArgs accordingly
        value = extractValue(deviceRotationMatch.string)
        if value == '90':
            cageArgs += "-r"
        
        if value == '180':
            cageArgs += "-r -r"

        if value == "270":
            cageArgs += "-r -r -r"

# If subprocess.call has empty args, it freaks out cage. So if we have no -r args to provide, we have to make sure
# we don't leave an empty string in the args paramter.
if cageArgs == "":
    subprocess.call(["/usr/bin/cage", "/usr/share/castboard-performer/performer"])
else:
    subprocess.call(["/usr/bin/cage", cageArgs, "/usr/share/castboard-performer/performer"])



    
    


